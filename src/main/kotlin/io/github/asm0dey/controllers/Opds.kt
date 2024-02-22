/*
 * opdsko
 * Copyright (C) 2022  asm0dey
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.asm0dey.controllers

import io.github.asm0dey.epubConverterAccessible
import io.github.asm0dey.model.Entry
import io.github.asm0dey.service.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.kodein.di.instance
import org.kodein.di.ktor.controller.AbstractDIController
import org.redundent.kotlin.xml.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

const val NAVIGATION_TYPE = "application/atom+xml;profile=opds-catalog;kind=navigation;charset=utf-8"
const val ACQUISITION_TYPE = "application/atom+xml;profile=opds-catalog;kind=acquisition;charset=utf-8"

class Opds(application: Application) : AbstractDIController(application) {
    private val info by instance<InfoService>()
    override fun Route.getRoutes() {
        route("/opds") {
            get {
                call.respondText(rootXml(), ContentType.parse(NAVIGATION_TYPE))
            }
            route("/search") {
                get {
                    call.respondText(
                        """<?xml version="1.0" encoding="UTF-8"?>
<OpenSearchDescription xmlns="https://a9.com/-/spec/opensearch/1.1/">
  <ShortName>Feedbooks</ShortName>
  <Description>Search on Feedbooks</Description>
  <InputEncoding>UTF-8</InputEncoding>
  <OutputEncoding>UTF-8</OutputEncoding>
  <Url type="application/atom+xml" template="/search/{searchTerms}"/>
  <Url type="application/atom+xml;profile=opds-catalog;kind=acquisition" template="/search/{searchTerms}"/>
  <Query role="example" searchTerms="robot"/>
</OpenSearchDescription>""", ContentType.parse("application/opensearchdescription+xml")
                    )
                }
                get("/{searchTerm}") {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    val searchTerm = URLDecoder.decode(call.parameters["searchTerm"]!!, StandardCharsets.UTF_8)
                    val (books, hasMore) = info.searchBookByText(searchTerm, page)
                    val xml = bookXml(
                        books,
                        call.request.uri,
                        "Search results for \"$searchTerm\"",
                        "search:$searchTerm",
                        ZonedDateTime.now(),
                        searchPaginationLinks(hasMore, page, "/opds/search/${searchTerm.encoded}")
                    )
                    call.respondText(xml, ContentType.parse(ACQUISITION_TYPE))
                }
            }
            get("/new") {
                val books = info.latestBooks().map { BookWithInfo(it) }

                call.respondText(
                    bookXml(
                        books,
                        call.request.path(),
                        "Latest books",
                        "new",
                        books.mapNotNull { it.book.added }.maxOrNull()?.z ?: ZonedDateTime.now()
                    ),
                    ContentType.parse(ACQUISITION_TYPE)
                )
            }
            get("/image/{id}") {
                val (binary, data) = info.imageByBookId(call.parameters["id"]!!.toLong())
                call.respondBytes(data, ContentType.parse(binary.contentType!!))
            }
            route("/book") {
                get("/{id}/info") {
                    val bookId = call.parameters["id"]!!.toLong()
                    val book = info.bookInfo(bookId)
                    call.respondText(
                        bookInfo(book),
                        ContentType.parse("application/atom+xml;type=entry;profile=opds-catalog")
                    )
                }
                get("/{id}/download") {
                    val bookId = call.parameters["id"]!!.toLong()
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(
                            ContentDisposition.Parameters.FileName,
                            "$bookId.fb2.zip"
                        ).toString()
                    )
                    val bytes = info.zippedBook(bookId)
                    call.respondBytes(bytes, ContentType.parse("application/fb+zip"))
                }
                get("/{id}/download/epub") {
                    if (!epubConverterAccessible) {
                        call.respond(HttpStatusCode.FailedDependency, "Platform is not supported for converting files")
                        return@get
                    }
                    val bookId = call.parameters["id"]!!.toLong()
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(
                            ContentDisposition.Parameters.FileName,
                            "$bookId.epub"
                        ).toString()
                    )
                    val bytes = info.toEpub(bookId)
                    call.respondBytes(bytes, ContentType.parse("application/epub+zip"))
                }
            }
            route("/author") {
                get("/c/{name?}") {
                    val path = call.request.path()
                    val nameStart = URLDecoder.decode(call.parameters["name"] ?: "", StandardCharsets.UTF_8)
                    val trim = nameStart.length < 5
                    val items = info.authorNameStarts(nameStart, trim)
                    call.respondText(
                        navXml(
                            path,
                            "authors:start_with:$nameStart",
                            if (nameStart.isBlank()) "First character of all authors" else "Authors starting with $nameStart",
                            entries = authorStartEntries(items, trim, nameStart)
                        ), ContentType.parse(NAVIGATION_TYPE)
                    )
                }
                route("/browse") {
                    get("/{id}") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val path = call.request.path()
                        val authorName = info.authorName(authorId)
                        val (inseries, out) = info.seriesNumberByAuthor(authorId)
                        val latestAuthorUpdate = info.latestAuthorUpdate(authorId)
                        call.respondText(
                            navXml(
                                path, "author:$authorId", authorName, latestAuthorUpdate,
                                authorEntries(inseries, authorId, authorName, out, latestAuthorUpdate)
                            ), ContentType.parse(NAVIGATION_TYPE)
                        )
                    }
                    get("/{id}/all") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val path = call.request.path()
                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                        val (total, books) = info.allBooksByAuthor(authorId, page)
                        val authorName = info.authorName(authorId)

                        val xml = bookXml(
                            books,
                            path,
                            "All books by $authorName",
                            "author:$authorId:all",
                            additionalLinks = searchPaginationLinks(
                                (page + 1) * 50 < total,
                                page,
                                "/opds/author/browse/$authorId/all"
                            )
                        )
                        call.respondText(xml, ContentType.parse(ACQUISITION_TYPE))
                    }
                    get("/{id}/series") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val path = call.request.path()
                        val namesWithDates = info.seriesByAuthorId(authorId)
                        val authorName = info.authorName(authorId)
                        call.respondText(
                            navXml(
                                path,
                                "author:$authorId:series",
                                "Series of $authorName",
                                namesWithDates.values.maxOf { it.first }.z,
                                authorSeries(namesWithDates, authorId)
                            ), ContentType.parse(NAVIGATION_TYPE)
                        )
                    }
                    get("/{id}/series/{seqId}") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val seqId = call.parameters["seqId"]!!.toLong()
                        val result = info.booksBySeriesAndAuthor(seqId, authorId)
                        val path = call.request.path()
                        val seqName = result.first().sequence
                        val xml = bookXml(
                            result,
                            path,
                            seqName!!,
                            "author:$authorId:series:$seqName"
                        )
                        call.respondText(xml, ContentType.parse(ACQUISITION_TYPE))

                    }
                    get("/{id}/out") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val path = call.request.path()
                        val books = info.booksWithoutSeriesByAuthorId(authorId)
                        val authorName = info.authorName(authorId)
                        val xml = bookXml(
                            books,
                            path,
                            "Books without series by $authorName",
                            "author:$authorId:series:out"
                        )
                        call.respondText(xml, ContentType.parse(ACQUISITION_TYPE))
                    }
                }
            }
            route("/series/") {
                get("/browse/{name?}") {
                    val path = call.request.path()
                    val nameStart = (call.parameters["name"] ?: "").decoded
                    val trim = nameStart.length < 5
                    val series = info.seriesNameStarts(nameStart, trim)
                    call.respondText(navXml(
                        path,
                        "series:start:$nameStart",
                        if (nameStart.isEmpty()) "First letter of all series"
                        else "Series starting with $nameStart",
                        entries = series.map { (name, count, complete, seqId) ->
                            Entry(
                                name,
                                "series:start:$nameStart:$name",
                                "${if (!complete) "Series starting with $name: " else ""}$count books",
                                ZonedDateTime.now(),
                                Entry.Link(
                                    "subsection",
                                    if (complete) "/opds/series/item/${seqId}" else "/opds/series/browse/${name.encoded}",
                                    NAVIGATION_TYPE,
                                    count.toLong()
                                )
                            )
                        }
                    ), ContentType.parse(NAVIGATION_TYPE))
                }
                get("/item/{seqId}") {
                    val path = call.request.path()
                    val seqId = call.parameters["seqId"]!!.toInt()
                    val authorFilter = call.parameters["author"]?.toLongOrNull()
                    val sorting = call.request.queryParameters["sort"] ?: "num"
                    val books = info.booksBySeriesId(seqId)
                    val allAuthors = books.flatMap { it.authors.map { it.buildName() to it.id } }.toSet()
                    val sorted = when (sorting) {
                        "num" -> books.sortedBy { it.book.sequenceNumber ?: Long.MAX_VALUE }
                        "name" -> books.sortedBy { it.book.name }
                        else -> books
                    }
                    val filtered =
                        authorFilter?.let { aId -> sorted.filter { it.authors.any { it.id == aId } } }?.toList()
                            ?: sorted.toList()
                    val xml = bookXml(
                        filtered,
                        path,
                        books.first().sequence!!,
                        "series:$seqId",
                        sorted.mapNotNull { it.book.added }.max().z,
                        listOfNotNull(
                            Entry.Link(
                                "http://opds-spec.org/facet",
                                "$path?sort=num${if (authorFilter != null) "&author=$authorFilter" else ""}",
                                ACQUISITION_TYPE,
                                title = "Order by number in series",
                                facetGroup = "Sorting",
                                activeFacet = sorting == "num"
                            ),
                            Entry.Link(
                                "http://opds-spec.org/facet",
                                "$path?sort=name" + if (authorFilter != null) "&author=$authorFilter" else "",
                                ACQUISITION_TYPE,
                                title = "Order by name of book",
                                facetGroup = "Sorting",
                                activeFacet = sorting == "name"
                            )
                        ) + if (allAuthors.size > 1) {
                            allAuthors.map { (authorName, id) ->
                                Entry.Link(
                                    "http://opds-spec.org/facet",
                                    "$path?sort=$sorting&author=$id",
                                    ACQUISITION_TYPE,
                                    title = "Only books in ${books.first().sequence} by $authorName",
                                    facetGroup = "Author",
                                    activeFacet = id == authorFilter
                                )

                            }
                        } else listOf()

                    )
                    call.respondText(xml, ContentType.parse(ACQUISITION_TYPE))
                }
            }
        }
    }

    private fun PipelineContext<Unit, ApplicationCall>.bookInfo(book: BookWithInfo) =
        entryXml(
            book,
            info.imageTypes(listOf(book))[book.id],
            bookDescriptionsLonger(listOf(book.id to book.book), "/opds/series/item", htmx = false)[book.id],
            info.shortDescriptions(listOf(book))[book.id],
            call.request.path()
        )

    private fun rootXml() = xml("feed", version = XmlVersion.V10, namespace = root) {
        "id"(root) { -"root" }
        "title"(root) { -"Asm0dey's books" }
        "updated"(root) { -"2022-06-21T12:14:28.209+03:00" }
        "author"(root) {
            "name"(root) { -"Pasha Finkelshteyn" }
            "uri"(root) { -"https://github.com/asm0dey/opdsKo" }
        }
        commonLinks("/opds")
        "entry"(root) {
            "title"(root) { -"New books" }
            "link"(root) {
                attributes(
                    "type" to "application/atom+xml;profile=opds-catalog;kind=acquisition",
                    "rel" to "http://opds-spec.org/sort/new",
                    "href" to "/opds/new"
                )
            }
            "id"(root) { -"new" }
            "summary"(root) { -"Recent publications from this catalog" }
            "updated"(root) { -ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()) }
        }
        "entry"(root) {
            "title"(root) { -"Books by authors" }
            "link"(root) {
                attributes(
                    "type" to "application/atom+xml;profile=opds-catalog;kind=navigation",
                    "rel" to "category",
                    "href" to "/opds/author/c/"
                )
            }
            "id"(root) { -"authors" }
            "summary"(root) { -"Authors by first letters" }
            "updated"(root) { -ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()) }
        }
        "entry"(root) {
            "title"(root) { -"Books by series" }
            "link"(root) {
                attributes(
                    "type" to "application/atom+xml;profile=opds-catalog;kind=navigation",
                    "rel" to "category",
                    "href" to "/opds/series/browse/"
                )
            }
            "id"(root) { -"series" }
            "summary"(root) { -"Series by first letters" }
            "updated"(root) { -ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()) }
        }
    }.toString(PrintOptions(singleLineTextElements = true))

    private fun navXml(
        path: String,
        id: String,
        title: String,
        updated: ZonedDateTime = ZonedDateTime.now(),
        entries: List<Entry>,
        additionalLinks: List<Entry.Link> = listOf(),
    ): String {
        return xml("feed", version = XmlVersion.V10, namespace = root) {
            namespace(thr)
            namespace(opds)
            namespace(opensearch)
            "id"(root) { -id }
            "title"(root) { -title }
            "updated"(root) { -formatDate(updated) }
            "author"(root) {
                "name"(root) { -"Pasha Finkelshteyn" }
                "uri"(root) { -"https://github.com/asm0dey/opdsKo" }
            }
            commonLinks(path)
            for (additionalLink in additionalLinks) {
                additionalLink(additionalLink)
            }
            for (entry in entries) {
                "entry"(root) {
                    "title"(root) { -entry.title }
                    for (link in entry.links) {
                        additionalLink(link)
                    }
                    "id"(root) { -entry.id }
                    if (entry.summary != null) {
                        "content"(root, Attribute("type", "text")) { -entry.summary }
                    }
                    "updated"(root) { -formatDate(entry.updated) }
                }
            }

        }.toString(PrintOptions(singleLineTextElements = true))
    }

    private val root = Namespace("http://www.w3.org/2005/Atom")
    private val dcterms = Namespace("dcterms", "http://purl.org/dc/terms/")
    private val thr = Namespace("thr", "http://purl.org/syndication/thread/1.0")
    private val opds = Namespace("opds", "http://opds-spec.org/2010/catalog")
    private val opensearch = Namespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/")


    private fun bookXml(
        books: List<BookWithInfo>,
        path: String,
        title: String,
        id: String,
        updated: ZonedDateTime = books.mapNotNull { it.book.added }.max().z,
        additionalLinks: List<Entry.Link> = listOf()
    ): String {
        val shortDescriptions = info.shortDescriptions(books)
        val imageTypes = info.imageTypes(books)
        return xml("feed", version = XmlVersion.V10, namespace = root) {
            namespace(dcterms)
            namespace(thr)
            namespace(opds)
            namespace(opensearch)
            "id"(root) { -id }
            "title"(root) { -title }
            "updated"(root) {
                -formatDate(updated)
            }
            "author"(root) {
                "name"(root) { -"Pasha Finkelshteyn" }
                "uri"(root) { -"https://github.com/asm0dey/opdsKo" }
            }
            commonLinks(path)
            additionalLinks.forEach { additionalLink(it) }
            for (book in books) {
                renderBook(
                    book,
                    imageTypes,
                    shortDescriptions,
                    formatDate(books.mapNotNull { it.book.added }.max().z)
                )
            }
        }
            .toString(
                PrintOptions(
                    pretty = true,
                    singleLineTextElements = true,
                )
            )
    }

    private fun Node.commonLinks(path: String) {
        "link"(root) {
            attributes(
                "type" to "application/atom+xml;profile=opds-catalog;kind=navigation",
                "rel" to "start",
                "href" to "/opds"
            )
        }
        "link"(root) {
            attributes(
                "type" to "application/atom+xml;profile=opds-catalog;kind=acquisition",
                "rel" to "self",
                "href" to path
            )
        }
        "link"(root) {
            attributes(
                "type" to "application/opensearchdescription+xml",
                "rel" to "search",
                "href" to "/opds/search"
            )
        }
        "link"(root) {
            attributes(
                "type" to "application/atom+xml",
                "title" to "Search",
                "href" to "/opds/search/{searchTerms}",
                "rel" to "search"
            )
        }
    }

    private fun Node.renderBook(
        book: BookWithInfo,
        imageTypes: Map<Long, String?>,
        shortDescriptions: Map<Long, String>,
        updated: String
    ) {
        "entry"(root) {
            "title"(root) { -book.book.name }
            "id"(root) { -"book:${book.id}" }
            for (author in book.authors) {
                "author"(root) {
                    "name"(root) { -author.buildName() }
                    "uri"(root) { -"/opds/author/browse/${author.id}" }
                }
            }
            "published"(root) { -formatDate(ZonedDateTime.now()) }
            "updated"(root) { -updated }
            if (!book.book.lang.isNullOrBlank()) {
                "language"(dcterms) { -book.book.lang!! }
            }
            if (book.book.date != null) {
                "date"(dcterms) { -book.book.date.toString() }
            }
            for (genre in book.genres) {
                "category"(root) {
                    attributes("term" to genre, "label" to genre)
                }
            }
            val imageType = imageTypes[book.id]
            if (imageType != null) {
                "link"(root) {
                    attributes(
                        "type" to imageType,
                        "rel" to "http://opds-spec.org/image",
                        "href" to "/opds/image/${book.id}"
                    )
                }
            }
            "link"(root) {
                attributes(
                    "type" to "application/atom+xml;type=entry;profile=opds-catalog",
                    "rel" to "alternate",
                    "href" to "/opds/book/${book.id}/info",
                    "title" to "Full entry for ${book.book.name}",
                )
            }
            "link"(root) {
                attributes(
                    "type" to "application/fb2+zip",
                    "rel" to "http://opds-spec.org/acquisition/open-access",
                    "href" to "/opds/book/${book.id}/download",
                    "title" to "fb2"
                )
            }
            if (epubConverterAccessible)
                "link"(root) {
                    attributes(
                        "type" to "application/epub+zip",
                        "rel" to "http://opds-spec.org/acquisition/open-access",
                        "href" to "/opds/book/${book.id}/download/epub",
                        "title" to "epub"
                    )
                }
            val desc = shortDescriptions[book.id]
            if (desc != null) {
                "summary"(root, Attribute("type", "text")) { -desc }
            }
        }

    }

    private fun Node.additionalLink(link: Entry.Link) {
        "link"(root) {
            attribute("type", link.type)
            attribute("rel", link.rel)
            attribute("href", link.href)
            link.count?.let { attribute("count", it, thr) }
            link.title?.let { attribute("title", it) }
            link.facetGroup?.let { attribute("facetGroupe", it, opds) }
            link.activeFacet?.let { attribute("activeFacet", it, opds) }
        }
    }

    private fun searchPaginationLinks(
        hasMore: Boolean,
        page: Int,
        paginationBase: String,
    ) = listOfNotNull(
        if (hasMore) {
            Entry.Link("next", "$paginationBase?page=${page + 1}", ACQUISITION_TYPE)
        } else null,
        if (page > 0) {
            Entry.Link("previous", "$paginationBase?page=${page - 1}", ACQUISITION_TYPE)
        } else null,
        if (page > 0) {
            Entry.Link("first", paginationBase, ACQUISITION_TYPE)
        } else null,
    )

    private val String.encoded: String get() = this.encodeURLPath()

    private fun authorStartEntries(items: List<Pair<String, Long>>, trim: Boolean, nameStart: String?) =
        items.map { (name, countOrId) ->
            Entry(
                name,
                listOf(
                    Entry.Link(
                        "subsection",
                        if (trim) "/opds/author/c/${name.encoded}"
                        else "/opds/author/browse/${countOrId}",
                        NAVIGATION_TYPE, if (trim) countOrId else null,
                    )
                ),
                "authors:start_with:$nameStart", name + if (trim) ": $countOrId books" else "", ZonedDateTime.now()
            )
        }

    private fun authorEntries(
        inseries: Int,
        authorId: Long,
        authorName: String,
        out: Int,
        latestAuthorUpdate: ZonedDateTime,
    ) = listOfNotNull(
        if (inseries > 0) Entry(
            "By series",
            listOf(Entry.Link("subsection", "/opds/author/browse/$authorId/series", NAVIGATION_TYPE)),
            "author:$authorId:series", "All books by $authorName by series", ZonedDateTime.now(),
        ) else null,
        if (out > 0 && inseries > 0) Entry(
            "Out of series",
            listOf(Entry.Link("subsection", "/opds/author/browse/$authorId/out", NAVIGATION_TYPE)),
            "author:$authorId:out", "All books by $authorName", ZonedDateTime.now(),
        ) else null,
        Entry(
            "All books",
            listOf(Entry.Link("subsection", "/opds/author/browse/$authorId/all", NAVIGATION_TYPE)),
            "author:$authorId:all", "All books by $authorName", latestAuthorUpdate,
        ),
    )

    private fun authorSeries(namesWithDates: Map<Pair<Int, String>, Pair<OffsetDateTime, Int>>, authorId: Long) =
        namesWithDates.map { (name, dateAndCount) ->
            Entry(
                name.second,
                listOf(
                    Entry.Link(
                        "subsection",
                        "/opds/author/browse/$authorId/series/${name.first}",
                        ACQUISITION_TYPE,
                        dateAndCount.second.toLong()
                    )
                ),
                "author:$authorId:series:${name.second}",
                "${name.second}: $dateAndCount books",
                dateAndCount.first.z
            )
        }

    private fun entryXml(
        book: BookWithInfo,
        imageType: String?,
        content: String?,
        summary: String?,
        path: String
    ): String {
        return xml("entry", version = XmlVersion.V10, namespace = root) {
            namespace(dcterms)
//            namespace(thr)
//            namespace(opds)
//            namespace(opensearch)
            "title"(root) { -book.book.name }
            "id"(root) { -"book:info:${book.id}" }
            "link"(root) {
                attributes(
                    "rel" to "self",
                    "type" to "application/atom+xml;type=entry;profile=opds-catalog",
                    "href" to path
                )
            }
            for (author in book.authors) {
                "author"(root) {
                    "name"(root) { -author.buildName() }
                    "uri"(root) { -"/opds/author/browse/${author.id}" }
                }
            }
            "published"(root) { -formatDate(ZonedDateTime.now()) }
            "updated"(root) { -formatDate(book.book.added!!) }
            if (!book.book.lang.isNullOrBlank()) "language"(dcterms) { -book.book.lang!! }
            if (book.book.date != null) "language"(dcterms) { -book.book.date.toString() }
            for (genre in book.genres) {
                "category"(root, Attribute("term", genre), Attribute("label", genre))
            }
            if (imageType != null) {
                "link"(
                    root,
                    Attribute("type", imageType),
                    Attribute("rel", "http://opds-spec.org/image"),
                    Attribute("href", "/opds/image/${book.id}")
                )
            }
            "link"(root) {
                attributes(
                    "type" to "application/fb2+zip",
                    "rel" to "http://opds-spec.org/acquisition/open-access",
                    "href" to "/opds/book/${book.id}/download",
                    "title" to "fb2"
                )
            }
            if (epubConverterAccessible)
                "link"(root) {
                    attributes(
                        "type" to "application/epub+zip",
                        "rel" to "http://opds-spec.org/acquisition/open-access",
                        "href" to "/opds/book/${book.id}/download/epub",
                        "title" to "epub"
                    )
                }
            if (summary != null) {
                "summary"(root, Attribute("type", "text")) { -summary }
                "content"(root, Attribute("type", "html")) {
                    if (content != null) cdata(content)
                }
            }
        }
            .toString(PrintOptions(singleLineTextElements = true))
    }

}
