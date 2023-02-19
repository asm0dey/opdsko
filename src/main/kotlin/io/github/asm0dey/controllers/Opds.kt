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
import io.ktor.server.jte.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.controller.AbstractDIController
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZonedDateTime

const val NAVIGATION_TYPE = "application/atom+xml;profile=opds-catalog;kind=navigation;charset=utf-8"
const val ACQUISITION_TYPE = "application/atom+xml;profile=opds-catalog;kind=acquisition;charset=utf-8"

class Opds(application: Application) : AbstractDIController(application) {
    private val info by instance<InfoService>()
    override fun Route.getRoutes() {
        route("/opds") {
            get {
                call.respond(
                    JteContent(
                        "root.kte",
                        params = mapOf(),
                        contentType = ContentType.parse(NAVIGATION_TYPE)
                    )
                )
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
                call.respond(
                    bookJte(books(books, call.request.path(), "Latest books", "new"))
                )
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
                                "hasEpub" to epubConverterAccessible,
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
        "additionalLinks" to additionalLinks,
        "hasEpub" to epubConverterAccessible,
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