package io.github.asm0dey.controllers

import io.github.asm0dey.model.Entry
import io.github.asm0dey.plugins.dtf
import io.github.asm0dey.service.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.jte.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.controller.AbstractDIController
import org.redundent.kotlin.xml.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZonedDateTime


private val atom = Namespace("", "http://www.w3.org/2005/Atom")
private val thread = Namespace("thr", "http://purl.org/syndication/thread/1.0")
private val opds = Namespace("opds", "http://opds-spec.org/2010/catalog")
private val opensearch = Namespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/")
private val dcterms = Namespace("dcterms", "http://purl.org/dc/terms/")

class OpdsBuilder(application: Application) : AbstractDIController(application) {
    private val info by instance<InfoService>()
    override fun Route.getRoutes() {
        route("/opdsb") {
            get {
                call.feedTemplate(ContentType.parse(NAVIGATION_TYPE)) {
                    navagation(
                        "root",
                        "Asm0dey's books",
                        ZonedDateTime.now(),
                        call.request.path(),
                        listOf(
                            Entry(
                                "New books",
                                "new",
                                "Recent publications from this catalog",
                                ZonedDateTime.now(),
                                Entry.Link(
                                    "http://opds-spec.org/sort/new",
                                    "/opds/new",
                                    ACQUISITION_TYPE
                                )
                            ),
                            Entry(
                                "Books by authors",
                                "authors",
                                "Authors by first letters",
                                ZonedDateTime.now(),
                                Entry.Link(
                                    "category",
                                    "/opds/author/c/",
                                    NAVIGATION_TYPE
                                )
                            ),
                            Entry(
                                "Books by series",
                                "series",
                                "Series by first letters",
                                ZonedDateTime.now(),
                                Entry.Link(
                                    "category",
                                    "/opds/series/browse/",
                                    NAVIGATION_TYPE
                                )
                            ),
                        )
                    )

                }
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
                    call.respond(
                        bookJte(
                            books(
                                books,
                                call.request.uri,
                                "Search results for \"$searchTerm\"",
                                "search:$searchTerm",
                                ZonedDateTime.now(),
                                searchPaginationLinks(hasMore, searchTerm, page)
                            )
                        )
                    )
                }
            }
            get("/new") {
                val books = info.latestBooks().map { BookWithInfo(it) }
                call.feedTemplate(ContentType.parse(NAVIGATION_TYPE)) {
                    bookTemplate(
                        books,
                        info.shortDescriptions(books),
                        info.imageTypes(books),
                        call.request.path(),
                        "Latest books",
                        "new",
                        books.maxOf { it.book.added }.z
                    )
                }
            }
            get("/image/{id}") {
                val (binary, data) = info.imageByBookId(call.parameters["id"]!!.toLong())
                call.respondBytes(data, ContentType.parse(binary.contentType))
            }
            route("/book") {
                get("/{id}/info") {
                    val bookId = call.parameters["id"]!!.toLong()
                    val book = info.bookInfo(bookId)
                    call.respond(
                        JteContent(
                            "entry.kte",
                            params = mapOf(
                                "book" to book,
                                "imageType" to info.imageTypes(listOf(book))[book.id],
                                "summary" to info.shortDescriptions(listOf(book))[book.id],
                                "content" to bookDescriptionsLonger(listOf(book.id to book.book))[book.id],
                            ),
                            contentType = ContentType.parse("application/atom+xml;type=entry;profile=opds-catalog")
                        )
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
            }
            route("/author") {
                get("/c/{name?}") {
                    val path = call.request.path()
                    val nameStart = URLDecoder.decode(call.parameters["name"] ?: "", StandardCharsets.UTF_8)
                    val trim = nameStart.length < 5
                    val items = info.authorNameStarts(nameStart, trim)
                    call.respond(
                        navJte(
                            navigation(
                                path,
                                "authors:start_with:$nameStart",
                                if (nameStart.isBlank()) "First character of all authors" else "Authors starting with $nameStart",
                                entries = authorStartEntries(items, trim, nameStart)
                            )
                        )
                    )
                }
                route("/browse") {
                    get("/{id}") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val path = call.request.path()
                        val authorName = info.authorName(authorId)
                        val (inseries, out) = info.seriesNumberByAuthor(authorId)
                        val latestAuthorUpdate = info.latestAuthorUpdate(authorId)
                        call.respond(
                            navJte(
                                navigation(
                                    path, "author:$authorId", authorName, latestAuthorUpdate,
                                    authorEntries(inseries, authorId, authorName, out, latestAuthorUpdate)
                                )
                            )
                        )
                    }
                    get("/{id}/all") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val path = call.request.path()
                        val books = info.allBooksByAuthor(authorId)
                        val authorName = info.authorName(authorId)
                        call.respond(
                            bookJte(books(books, path, "All books by $authorName", "author:$authorId:all"))
                        )
                    }
                    get("/{id}/series") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val path = call.request.path()
                        val namesWithDates = info.seriesByAuthorId(authorId)
                        val authorName = info.authorName(authorId)
                        call.respond(
                            navJte(
                                navigation(
                                    path,
                                    "author:$authorId:series",
                                    "Series of $authorName",
                                    namesWithDates.values.maxOf { it.first }.z,
                                    authorSeries(namesWithDates, authorId)
                                )
                            )
                        )
                    }
                    get("/{id}/series/{name}") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val seriesName = URLDecoder.decode(call.parameters["name"]!!, StandardCharsets.UTF_8)
                        val result = info.booksBySeriesAndAuthor(seriesName, authorId)
                        val path = call.request.path()
                        call.respond(
                            bookJte(
                                books(result, path, seriesName, "author:$authorId:series:$seriesName")
                            )
                        )
                    }
                    get("/{id}/out") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val path = call.request.path()
                        val books = info.booksWithoutSeriesByAuthorId(authorId)
                        val authorName = info.authorName(authorId)
                        call.respond(
                            bookJte(
                                books(
                                    books,
                                    path,
                                    "Books without series by $authorName",
                                    "author:$authorId:series:out"
                                )
                            )
                        )
                    }
                }
            }
            route("/series/") {
                get("/browse/{name?}") {
                    val path = call.request.path()
                    val nameStart = (call.parameters["name"] ?: "").decoded
                    val trim = nameStart.length < 5
                    val series = info.seriesNameStarts(nameStart, trim)
                    call.respond(
                        navJte(
                            navigation(
                                path,
                                "series:start:$nameStart",
                                if (nameStart.isEmpty()) "First letter of all series"
                                else "Series starting with $nameStart",
                                entries = series.map { (name, count, complete) ->
                                    Entry(
                                        name,
                                        "series:start:$nameStart:$name",
                                        "${if (!complete) "Series starting with $name: " else ""}$count books",
                                        ZonedDateTime.now(),
                                        Entry.Link(
                                            "subsection",
                                            if (complete) "/opds/series/item/${name.encoded}" else "/opds/series/browse/${name.encoded}",
                                            NAVIGATION_TYPE,
                                            count.toLong()
                                        )
                                    )
                                }
                            )
                        )
                    )
                }
                get("/item/{name}") {
                    val path = call.request.path()
                    val name = call.parameters["name"]!!.decoded
                    val authorFilter = call.parameters["author"]?.toLongOrNull()
                    val sorting = call.request.queryParameters["sort"] ?: "num"
                    val books = info.booksBySeriesName(name)
                    val allAuthors = books.flatMap { it.authors.map { it.buildName() to it.id } }.toSet()
                    val sorted = when (sorting) {
                        "num" -> books.sortedBy { it.book.sequenceNumber ?: Int.MAX_VALUE }
                        "name" -> books.sortedBy { it.book.name!! }
                        else -> books
                    }
                    val filtered =
                        authorFilter?.let { aId -> sorted.filter { it.authors.any { it.id == aId } } }?.toList()
                            ?: sorted.toList()
                    call.respond(
                        bookJte(
                            books(
                                filtered,
                                path,
                                name,
                                "series:$name",
                                sorted.maxOf { it.book.added }.z,
                                listOfNotNull(
                                    Entry.Link(
                                        "http://opds-spec.org/facet",
                                        "$path?sort=num" + if (authorFilter != null) "&author=$authorFilter" else "",
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
                                            title = "Only books in $name by $authorName",
                                            facetGroup = "Author",
                                            activeFacet = id == authorFilter
                                        )

                                    }
                                } else listOf()
                            )
                        )
                    )
                }
            }
        }
    }


    private fun searchPaginationLinks(
        hasMore: Boolean,
        searchTerm: String,
        page: Int,
    ) = listOfNotNull(
        if (hasMore) {
            Entry.Link("next", "/opds/search/${searchTerm.encoded}?page=${page + 1}", ACQUISITION_TYPE)
        } else null,
        if (page > 0) {
            Entry.Link("previous", "/opds/search/${searchTerm.encoded}?page=${page - 1}", ACQUISITION_TYPE)
        } else null,
        if (page > 0) {
            Entry.Link("first", "/opds/search/${searchTerm.encoded}", ACQUISITION_TYPE)
        } else null,
    )

    private val String.encoded: String get() = URLEncoder.encode(this, StandardCharsets.UTF_8)
    private fun books(
        books: List<BookWithInfo>,
        path: String,
        title: String,
        id: String,
        updated: ZonedDateTime = books.maxOf { it.book.added }.z,
        additionalLinks: List<Entry.Link> = listOf(),
    ) = mapOf(
        "books" to books,
        "bookDescriptions" to info.shortDescriptions(books),
        "imageTypes" to info.imageTypes(books),
        "path" to path,
        "title" to title,
        "feedId" to id,
        "feedUpdated" to updated,
        "additionalLinks" to additionalLinks
    )

    private fun navJte(params: Map<String, Any>) = JteContent(
        "navigation.kte", params, contentType = ContentType.parse(NAVIGATION_TYPE)
    )

    private fun bookJte(params: Map<String, Any>): JteContent = JteContent(
        "books.kte", params, contentType = ContentType.parse(ACQUISITION_TYPE)
    )

    private fun navigation(
        path: String,
        id: String,
        title: String,
        updated: ZonedDateTime = ZonedDateTime.now(),
        entries: List<Entry>,
        additionalLinks: List<Entry.Link> = listOf(),
    ) = mapOf(
        "feedId" to id,
        "feedTitle" to title,
        "feedUpdated" to updated,
        "path" to path,
        "entries" to entries,
        "additionalLinks" to additionalLinks
    )

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

    private fun authorSeries(namesWithDates: Map<String, Pair<LocalDateTime, Int>>, authorId: Long) =
        namesWithDates.map { (name, dateAndCount) ->
            Entry(
                name,
                listOf(
                    Entry.Link(
                        "subsection",
                        "/opds/author/browse/$authorId/series/${name.encoded}",
                        ACQUISITION_TYPE,
                        dateAndCount.second.toLong()
                    )
                ),
                "author:$authorId:series:$name",
                "$name: $dateAndCount books",
                dateAndCount.first.z
            )
        }

}

private fun Node.navagation(
    feedId: String,
    feedTitle: String,
    feedUpdated: ZonedDateTime,
    path: String,
    entries: List<Entry>,
    additionalLinks: List<Entry.Link> = listOf()
) {
    "link" {
        attribute("type", NAVIGATION_TYPE)
        attribute("rel", "self")
        attribute("href", path)
    }
    "id" { -feedId }
    "title" { -feedTitle }
    "updated" { -dtf.format(feedUpdated) }
    for (additionalLink in additionalLinks) {
        link(additionalLink)
    }
    for (entry in entries) {
        "entry" {
            "title" { -entry.title }
            for (link in entry.links) {
                link(link)
            }
            "id" { -entry.id }
            if (entry.summary != null) {
                "content"("type" to "text") { -entry.summary }
            }
            "updated" { -dtf.format(entry.updated) }
        }
    }
}

private fun Node.bookTemplate(
    books: List<BookWithInfo>,
    bookDescriptions: Map<Long, String?>,
    imageTypes: Map<Long, String?>,
    path: String,
    title: String,
    feedId: String,
    feedUpdated: java.time.temporal.TemporalAccessor,
    additionalLinks: List<Entry.Link> = listOf()
) {
    "link" {
        attribute("type", ACQUISITION_TYPE)
        attribute("rel", "self")
        attribute("href", path)
    }
    "id" { -feedId }
    "title" { -title }
    "updated" { -dtf.format(feedUpdated) }
    for (additionalLink in additionalLinks) {
        link(additionalLink)
    }
    for (book in books) {
        "entry" {
            "title" { -book.book.name }
            "id" { -"book${book.id}" }
            for (author in book.authors) {
                "author" {
                    "name" { -author.buildName() }
                    "uri" { "/opds/author/browse/${author.id}" }
                }
            }
            "published" { -dtf.format(ZonedDateTime.now()) }
            "updated" { -dtf.format(feedUpdated) }
            if (book.book.lang != null)
                "language"(dcterms) { -book.book.lang }
            if (book.book.date != null)
                "date"(dcterms) { -book.book.date }
            for (genre in book.genres) {
                "category"("term" to genre, "label" to genre)
            }
            imageTypes[book.id]?.let {
                link(
                    Entry.Link(
                        "http://opds-spec.org/image",
                        "/opds/image/$it",
                        it
                    )
                )

            }
            link(
                Entry.Link(
                    "alternate",
                    "/opds/book/${book.id}/info",
                    "application/atom+xml;type=entry;profile=opds-catalog",
                    title = "Full entry for ${book.book.name}"
                )
            )
            link(
                Entry.Link(
                    "http://opds-spec.org/acquisition/open-access",
                    "/opds/book/${book.id}/download",
                    "application/fb2+zip",
                )
            )
            bookDescriptions[book.id]?.let {
                "summary"("type" to "text") { -it }
            }
        }
    }
}

fun Node.link(entryLink: Entry.Link) {
    "link" {
        attribute("rel", entryLink.rel)
        attribute("href", entryLink.href)
        attribute("type", entryLink.type)
        if (entryLink.count != null)
            attribute("count", entryLink.count, thread)
        if (entryLink.title != null)
            attribute("count", entryLink.title)
        if (entryLink.facetGroup != null)
            attribute("facetGroup", entryLink.facetGroup, opds)
        if (entryLink.activeFacet != null)
            attribute("activeFacet", entryLink.activeFacet, opds)
    }
}

private suspend fun ApplicationCall.feedTemplate(contentType: ContentType, function: Node.() -> Unit) {
    respondXml(contentType) {
        xmlns = atom.value
        namespace(thread)
        namespace(opds)
        namespace(opensearch)
        namespace(dcterms)
        "author" {
            "name" { -"Pasha Finkelshteyn" }
            "uri" { -"https://github.com/asm0dey/opdsKo" }
        }
        "link" {
            attribute("type", NAVIGATION_TYPE)
            attribute("rel", "start")
            attribute("href", "/opds")
        }
        "link" {
            attribute("type", "application/opensearchdescription+xml")
            attribute("rel", "starsearch")
            attribute("href", "/opds/search")
        }
        "link" {
            attribute("type", "application/atom+xml")
            attribute("rel", "Search")
            attribute("href", "/opds/search/{searchTerms}")
            attribute("title", "Search")
        }
        function()
    }
}

private suspend fun ApplicationCall.respondXml(contentType: ContentType, function: Node.() -> Unit) {
    respondText(contentType) {
        xml("feed", "utf-8", XmlVersion.V10, null, function).toString(
            PrintOptions(
                pretty = true,
                singleLineTextElements = true,
                useSelfClosingTags = true,
                useCharacterReference = true,
                indent = "  "
            )
        )
    }
}

private fun Node.atomAttribute(name: String, value: String) {
    attribute(name, value)
}
