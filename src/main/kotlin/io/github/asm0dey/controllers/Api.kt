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
@file:Suppress("FunctionName")

package io.github.asm0dey.controllers

import io.github.asm0dey.epubConverterAccessible
import io.github.asm0dey.service.*
import io.ktor.http.*
import io.ktor.http.ContentType.Text.Html
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.kodein.di.instance
import org.kodein.di.ktor.controller.AbstractDIController
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.min

val String.encoded get() = this.encodeURLPath()


class Api(application: Application) : AbstractDIController(application) {
    private val info by instance<InfoService>()
    override fun Route.getRoutes() {
        route("/api")
            get {
                val x = createHTML(false).div("grid") {
                    NavTile("New books", "Recent publications from this catalog", "/api/new")
                    NavTile("Books by series", "Authors by first letters", "/api/series/browse")
                    NavTile("Books by author", "Series by first letters", "/api/author/c")
                    NavTile("Genres", "Books by genres", "/api/genre")
                }
                return@get smartHtml(call, x, BreadCrumbs("Library" to "/api"))

            }
            get("/search") {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val searchTerm = URLDecoder.decode(call.request.queryParameters["search"]!!, UTF_8)
                val (books, _, total) = info.searchBookByText(searchTerm, page - 1)
                val imageTypes = info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("grid") {
                    for (book in books) {
                        BookTile(book, imageTypes, shortDescriptions)
                    }
                }
                val y = BreadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "Search: $searchTerm" to "/api/search?search=${searchTerm.encoded}",
                    )
                )
                val z = Pagination(page, total, "/api/search?search=${searchTerm.encoded}")
                return@get smartHtml(call, x, y, z)
            }
            get("/new/{page?}") {
                val page = call.parameters["page"]?.toInt() ?: 1
                val books = info.latestBooks(page - 1).map { BookWithInfo(it) }
                val imageTypes = info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("grid") {
                    for (book in books) {
                        BookTile(book, imageTypes, shortDescriptions)
                    }
                }
                val y = BreadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "New" to "/api/new",
                    )
                )
                return@get smartHtml(call, x, y, Pagination(page, Int.MAX_VALUE, "/api/new"))

            }
            get("/author/c/{name?}") {
                val nameStart = URLDecoder.decode(call.parameters["name"] ?: "", UTF_8)
                val trim = nameStart.length < 5
                val items = info.authorNameStarts(nameStart, trim)
                val x = createHTML(false).div("grid") {
                    for ((name, countOrId) in items) {
                        if (trim) NavTile(name, "$countOrId items inside", "/api/author/c/${name.encoded}")
                        else NavTile(name, "Books by $name", "/api/author/browse/$countOrId")
                    }
                }
                val y = BreadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        if (trim && nameStart.isNotBlank()) nameStart to "/api/author/c/${nameStart.encoded}" else null
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("/author/browse/{id}") {
                val authorId = call.parameters["id"]!!.toLong()
                val (inseries, out) = info.seriesNumberByAuthor(authorId)
                val authorName = info.authorName(authorId)
                val x = createHTML(false).div("grid") {
                    if (inseries > 0)
                        NavTile(
                            "By series",
                            "All books by $authorName in series",
                            "/api/author/browse/$authorId/series"
                        )
                    if (inseries > 0 && out > 0)
                        NavTile(
                            "Out of series",
                            "All books by $authorName out of series",
                            "/api/author/browse/$authorId/out"
                        )
                    NavTile(
                        "All books",
                        "All books by $authorName alphabetically",
                        "/api/author/browse/$authorId/all"
                    )

                }
                val y = BreadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        authorName to "/api/author/browse/$authorId"
                    )
                )
                return@get smartHtml(call, x, y)
            }
            route("/genre") {
                get {
                    val genres = info.genres()
                    val x = createHTML(false).div("grid") {
                        for ((id, genre, count) in genres) {
                            NavTile(genre, "$count items", "/api/genre/$id")
                        }
                    }
                    val y = BreadCrumbs("Library" to "/api", "By Authors" to "/api/genre")
                    return@get smartHtml(call, x, y)
                }
                route("/{id}") {
                    get {
                        val genreId = call.parameters["id"]?.toLong()!!
                        val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                        val x = createHTML(false).div("grid") {
                            NavTile("Authors", "Books in genre by author", "/api/genre/$genreId/author")
                            NavTile("All", "All books in genre", "/api/genre/$genreId/all")
                        }
                        val y = BreadCrumbs(
                            "Library" to "/api",
                            "By Genre" to "/api/genre",
                            genreName to "/api/genre/$genreId"
                        )
                        return@get smartHtml(call, x, y)
                    }
                    get("/all") {
                        val genreId = call.parameters["id"]?.toLong()!!
                        val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                        val (total, books) = info.booksInGenre(genreId, page - 1)
                        val images = info.imageTypes(books)
                        val descriptions = info.shortDescriptions(books)
                        val x = createHTML(false).div("grid") {
                            for (book in books) {
                                BookTile(book, images, descriptions)
                            }
                        }
                        val y = BreadCrumbs(
                            "Library" to "/api",
                            "By Genre" to "/api/genre",
                            genreName to "/api/genre/$genreId",
                            "All books" to "/api/genre/$genreId/all",
                        )
                        val z = Pagination(page, total, "/api/genre/$genreId/all")
                        return@get smartHtml(call, x, y, z)
                    }
                    route("/author") {
                        get {
                            val genreId = call.parameters["id"]?.toLong()!!
                            val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                            val genreAuthors = info.genreAuthors(genreId)
                            val x = createHTML(false).div("grid") {
                                for ((id, name) in genreAuthors) {
                                    NavTile(name, "Books in $genreName by $name", "/api/genre/$genreId/author/$id")
                                }
                            }
                            val y = BreadCrumbs(
                                "Library" to "/api",
                                "By Genre" to "/api/genre",
                                genreName to "/api/genre/$genreId",
                                "By Author" to "/api/genre/$genreId/author",
                            )
                            return@get smartHtml(call, x, y)
                        }
                        get("/{aid}") {
                            val genreId = call.parameters["id"]?.toLong()!!
                            val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                            val authorId = call.parameters["aid"]?.toLong()!!
                            val (authorName, books) = info.booksByGenreAndAuthor(genreId, authorId)
                            val imageTypes = info.imageTypes(books)
                            val descriptions = info.shortDescriptions(books)
                            val x = createHTML(false).div("grid") {
                                for (book in books) {
                                    BookTile(book, imageTypes, descriptions)
                                }
                            }
                            val y = BreadCrumbs(
                                "Library" to "/api",
                                "By Genre" to "/api/genre",
                                genreName to "/api/genre/$genreId",
                                "By Author" to "/api/genre/$genreId/author",
                                authorName to "/api/genre/$genreId/author/$authorId",
                            )
                            return@get smartHtml(call, x, y)
                        }
                    }
                }
            }
            get("/author/browse/{id}/all") {
                val authorId = call.parameters["id"]!!.toLong()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val (total, books) = info.allBooksByAuthor(authorId, page - 1)
                val authorName = info.authorName(authorId)
                val imageTypes = info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("grid") {
                    for (book in books) {
                        BookTile(book, imageTypes, shortDescriptions)
                    }
                }
                val y = BreadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        authorName to "/api/author/browse/$authorId",
                        "All books" to "/api/author/browse/$authorId/all"
                    )
                )
                val z = Pagination(page, total, "/api/author/browse/$authorId/all")
                return@get smartHtml(call, x, y, z)
            }
            get("/author/browse/{id}/out") {
                val authorId = call.parameters["id"]!!.toLong()
                val books = info.booksWithoutSeriesByAuthorId(authorId)
                val authorName = info.authorName(authorId)

                val imageTypes = info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("grid") {
                    for (book in books) {
                        BookTile(book, imageTypes, shortDescriptions)
                    }
                }
                val y = BreadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        authorName to "/api/author/browse/$authorId",
                        "Out of series" to "/api/author/browse/$authorId/out"
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("/author/browse/{id}/series") {
                val authorId = call.parameters["id"]!!.toLong()
                val path = call.request.path()
                val namesWithDates = info.seriesByAuthorId(authorId)
                val authorName = info.authorName(authorId)

                val x = createHTML(false).div("grid") {
                    for ((name, pair) in namesWithDates) {
                        NavTile(
                            name.second,
                            "${pair.second} books",
                            "/api/author/browse/$authorId/series/${name.first}"
                        )
                    }
                }
                val y = BreadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        authorName to "/api/author/browse/$authorId",
                        "Series" to path
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("/author/browse/{id}/series/{seqId}") {
                val authorId = call.parameters["id"]!!.toLong()
                val seqId = call.parameters["seqId"]!!.toLong()
                val books = info.booksBySeriesAndAuthor(seqId, authorId)
                val path = call.request.path()
                val imageTypes = info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val authorName = info.authorName(authorId)
                val x = createHTML(false).div("grid") {
                    for (bookWithInfo in books) {
                        BookTile(bookWithInfo, imageTypes, shortDescriptions)
                    }
                }
                val y = BreadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        authorName to "/api/author/browse/$authorId",
                        books.firstOrNull()?.sequence?.to(path),
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("series/browse/{name?}") {
                val nameStart = (call.parameters["name"] ?: "").decoded
                val trim = nameStart.length < 5
                val series = info.seriesNameStarts(nameStart, trim)
                val x = createHTML(false).div("grid") {
                    for ((name, countOrId, complete, seqid) in series) {
                        if (!complete)
                            NavTile(name, "$countOrId items inside", "/api/series/browse/${name.encoded}")
                        else
                            NavTile(name, "$countOrId items inside", "/api/series/item/$seqid")
                    }
                }
                val y = BreadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Series" to "/api/series/browse",
                        if (trim && nameStart.isNotBlank()) nameStart to "/api/series/browse/${nameStart.encoded}" else null
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("series/item/{seqId}") {
                val seqId = call.parameters["seqId"]!!.toInt()
                val authorFilter = call.parameters["author"]?.toLongOrNull()
                val sorting = call.request.queryParameters["sort"] ?: "num"
                val books = info.booksBySeriesId(seqId)
                val sorted = when (sorting) {
                    "num" -> books.sortedBy { it.book.sequenceNumber?.toLong() ?: Long.MAX_VALUE }
                    "name" -> books.sortedBy { it.book.name }
                    else -> books
                }
                val filtered =
                    authorFilter?.let { aId -> sorted.filter { it.authors.any { it.id == aId } } }?.toList()
                        ?: sorted.toList()
                val imageTypes = info.imageTypes(filtered)
                val shortDescriptions = info.shortDescriptions(filtered)
                val x = createHTML(false).div("grid") {
                    for (bookWithInfo in filtered) BookTile(bookWithInfo, imageTypes, shortDescriptions)
                }
                val y =
                    BreadCrumbs(
                        "Library" to "/api",
                        "Series" to "/api/series/browse",
                        books.first().sequence!! to "/api/series/item/$seqId"
                    )
                return@get smartHtml(call, x, y)
            }
            get("/book/{id}/info") {
                val bookId = call.parameters["id"]!!.toLong()
                val book = info.bookInfo(bookId)
                val descrHtml = bookDescriptionsLonger(listOf(bookId to book.book))[bookId]
                val hasImage = info.imageTypes(listOf(book))[bookId] != null
                call.respondText(Modal(book, hasImage, descrHtml), Html)
            }
            get("/book/{id}/image") {
                val bookId = call.parameters["id"]!!.toLong()
                call.respondText(ModalImage("/opds/image/$bookId"), Html)
            }
        }

    }

    private fun ModalImage(imageUrl: String): String {
        val closeModalScript = "on click take .is-active from #modal wait 200ms then remove #modal"
        return createHTML(false).div("modal") {
            id = "modal"
            div("modal-background") {
                attributes["_"] = closeModalScript
            }
            div("modal-content is-clipped") {
                p("image") {
                    img(src = imageUrl)
                }
            }
            button(classes = "modal-close is-large") {
                attributes["aria-label"] = "close"
                attributes["_"] = closeModalScript
            }
        }
    }


    private suspend fun smartHtml(
        call: ApplicationCall,
        content: String,
        breadcrumbs: String,
        pagination: String = Pagination(1, 1, "")
    ) =
        if (call.request.headers["HX-Request"] == "true") call.respondText(
            content + breadcrumbs + pagination,
            Html
        )
        else call.respondHtml { fullHtml(breadcrumbs, content, pagination) }

    private fun DIV.BookTile(
        bookWithInfo: BookWithInfo,
        images: Map<Long, String?>,
        descriptionsShort: Map<Long, String?>
    ) {
        div("cell") {
            div("card") {
                div("card-header") {
                    p("card-header-title") {
                        +bookWithInfo.book.name
                    }
                }
                if (images[bookWithInfo.id] != null) {
                    div("card-image") {
                        figure("image") {
                            a {
                                attributes["hx-get"] = "/api/book/${bookWithInfo.id}/image"
                                attributes["hx-swap"] = "innerHTML show:.input:top"
                                attributes["hx-target"] = "#modal-cont"
                                attributes["_"] = "on htmx:afterOnLoad wait 10ms then add .is-active to #modal"
                                img(src = "/opds/imag       e/${bookWithInfo.id}") {
                                    attributes["loading"] = "lazy"
                                }
                            }
                        }
                    }
                }
                div("card-content") {
                    div("content") {
                        text((descriptionsShort[bookWithInfo.id]?.let {
                            it.substring(0 until min(it.length, 200))
                        }?.plus('…') ?: ""))
                    }
                }
                footer("card-footer mb-0 pb-0 is-align-items-self-end") {
                    a(classes = "card-footer-item") {
                        attributes["hx-get"] = "/api/book/${bookWithInfo.id}/info"
                        attributes["hx-target"] = "#modal-cont"
                        attributes["_"] = "on htmx:afterOnLoad wait 10ms then add .is-active to #modal"
                        +"Info"
                    }
                    if (!epubConverterAccessible)
                        a("/opds/book/${bookWithInfo.id}/download", classes = "card-footer-item") { +"Download" }
                    else {
                        a("/opds/book/${bookWithInfo.id}/download", classes = "card-footer-item") { +"fb2" }
                        a("/opds/book/${bookWithInfo.id}/download/epub", classes = "card-footer-item") { +"epub" }
                    }
                }
            }
        }
    }

    private fun BreadCrumbs(vararg items: Pair<String, String>) = BreadCrumbs(items.toList())

    private fun BreadCrumbs(items: List<Pair<String, String>>) = createHTML(false).div {
        attributes["hx-swap-oob"] = "innerHTML:.breadcrumb"
        ul {
            items.forEachIndexed { index, pair ->
                val (name, href) = pair
                li(if (index == items.size - 1) "is-active" else null) {
                    a {
                        layoutUpdateAttributes(href)
                        +name
                    }
                }
            }
        }
    }

    private fun Pagination(curPage: Int, total: Int, base: String) = createHTML(false).div {
        fun String.withParam(param: String) = if (URI(this).query == null) "${this}?$param" else "${this}&$param"
        val last = total / 50 + 1
        attributes["hx-swap-oob"] = "innerHTML:.navv"
        if (curPage == 1 && total / 50 + 1 == 1) div()
        else
            nav {
                classes = setOf("pagination", "is-centered")
                role = "navigation"
                a {
                    classes = setOfNotNull("pagination-previous", if (curPage == 1) "is-disabled" else null)
                    if (curPage != 1) {
                        attributes["hx-trigger"] = "click"
                        attributes["hx-get"] = base.withParam("page=${curPage - 1}")
                        attributes["hx-swap"] = "innerHTML show:.input:top"
                        attributes["hx-target"] = "#layout"
                        attributes["hx-push-url"] = "true"
                    }
                    +"Previous page"
                }
                a {
                    classes = setOfNotNull("pagination-next", if (curPage == last) "is-disabled" else null)
                    if (curPage != last) {
                        attributes["hx-trigger"] = "click"
                        attributes["hx-get"] = base.withParam("page=${curPage + 1}")
                        attributes["hx-swap"] = "innerHTML show:.input:top"
                        attributes["hx-target"] = "#layout"
                        attributes["hx-push-url"] = "true"
                    }
                    +"Next page"
                }
                if (total != Int.MAX_VALUE) {
                    ul("pagination-list") {
                        val pageToDraw = (1..last).map {
                            it to (it == 1 || it == last || abs(curPage - it) <= 1)
                        }
                        val realToDraw = pageToDraw.fold(listOf<Pair<Int, Boolean>>()) { a, b ->
                            if (b.second) a + b
                            else if (a.last().second) a + (-1 to false)
                            else a
                        }
                        for ((page, draw) in realToDraw) {
                            if (!draw) {
                                li {
                                    span {
                                        classes = setOf("pagination-ellipsis")
                                        +"…"
                                    }
                                }
                            } else {
                                li {
                                    a {
                                        classes =
                                            setOfNotNull("pagination-link", if (curPage == page) "is-current" else null)
                                        attributes["hx-trigger"] = "click"
                                        attributes["hx-get"] = base.withParam("page=$page")
                                        attributes["hx-swap"] = "innerHTML show:.input:top"
                                        attributes["hx-target"] = "#layout"
                                        attributes["hx-push-url"] = "true"
                                        +(page.toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun HTMLTag.layoutUpdateAttributes(href: String) {
        attributes["hx-trigger"] = "click"
        attributes["hx-get"] = href
        attributes["hx-swap"] = "innerHTML show:.input:top"
        attributes["hx-target"] = "#layout"
        attributes["hx-push-url"] = "true"
    }

    private fun Modal(
        book: BookWithInfo,
        hasImage: Boolean,
        descrHtml: String?
    ): String {
        val closeModalScript = "on click take .is-active from #modal wait 200ms then remove #modal"
        return createHTML(false).div("modal") {
            id = "modal"
            div("modal-background") {
                attributes["_"] = closeModalScript
            }
            div("modal-card") {
                header("modal-card-head") {
                    p("modal-card-title") {
                        +book.book.name
                    }
                    button(classes = "delete") {
                        attributes["aria-label"] = "close"
                        attributes["_"] = closeModalScript
                    }
                }
                section("modal-card-body") {
                    article("media") {
                        if (hasImage) {
                            figure("media-left") {
                                p("image") {
                                    img(src = "/opds/image/${book.id}") {
                                        attributes["style"] = "width: 172px"
                                    }
                                }
                            }
                        }
                        div("media-content") {
                            div("content") {
                                p {
                                    div("tags") {
                                        for (author in book.authors) {
                                            a {
                                                attributes["_"] = closeModalScript
                                                layoutUpdateAttributes("/api/author/browse/${author.id}")
                                                span("tag is-rounded is-normal is-link is-medium is-light") {
                                                    +author.buildName()
                                                }
                                            }
                                        }
                                    }
                                    div("tags") {
                                        for (genre in book.genres) {
                                            a {
                                                attributes["_"] = closeModalScript
                                                layoutUpdateAttributes("/api/genre/${genre.second}")
                                                span("tag is-rounded is-normal is-info is-light") {
                                                    +genre.first
                                                }
                                            }
                                        }
                                    }
                                    unsafe {
                                        +(descrHtml ?: "")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun HTML.fullHtml(breadcrumbs: String, content: String, pagination: String = "") {
        head {
            meta(charset = "utf-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1")
            title("Asm0dey's library")
//            link(rel = "stylesheet", href = "/webjars/bulma/css/bulma.min.css")
//            link(rel = "stylesheet", href = "/webjars/font-awesome/css/all.min.css")
            link(rel="stylesheet", href="https://cdn.jsdelivr.net/npm/bulma@1.0.1/css/bulma.min.css")
            link(rel="stylesheet", href="https://cdn.jsdelivr.net/npm/font-awesome/css/font-awesome.min.css")

            link(href = "/apple-touch-icon.png", rel = "apple-touch-icon") {
                sizes = "180x180"
            }
            link(href = "/favicon-32x32.png", rel = "icon", type = "image/png") {
                sizes = "32x32"
            }
            link(href = "/favicon-16x16.png", rel = "icon", type = "image/png") {
                sizes = "16x16"
            }
            link(rel = "manifest", href = "/site.webmanifest")
            link(rel = "mask-icon", href = "/safari-pinned-tab.svg") {
                attributes["color"] = "#5bbad5"
            }
            meta(name = "msapplication-TileColor", content = "#2b5797")
            meta(name = "theme-color", content = "#ffffff")
        }
        body {
            nav(classes = "navbar") {
                div("container") {
                    div("navbar-brand") {
                        a(classes = "navbar-item brand-text", href = "/api") {
                            attributes["hx-get"] = "/api"
                            attributes["hx-swap"] = "innerHTML show:.input:top"
                            attributes["hx-target"] = "#layout"
                            img(alt = "Logo", src = "/logo.png")
                            +Entities.nbsp
                            +"Asm0dey's library"
                        }
                    }
                }
            }
            div("container") {
                div("section") {
                    attributes["hx-boost"] = "true"
                    div("field has-addons") {
                        div("control has-icons-left is-expanded") {
                            input(InputType.text, name = "search", classes = "input") {
                                attributes["placeholder"] = "Search"
                                attributes["hx-trigger"] = "keyup[keyCode === 13]"
                                attributes["hx-get"] = "/api/search"
                                attributes["hx-swap"] = "innerHTML show:.input:top"
                                attributes["hx-target"] = "#layout"
                            }
                            span("icon is-small is-left") {
                                i("fas fa-search")
                            }
                        }
                        div("control") {
                            button(classes = "button") {
                                attributes["hx-include"] = "[name='search']"
                                attributes["hx-get"] = "/api/search"
                                attributes["hx-swap"] = "innerHTML show:.input:top"
                                attributes["hx-target"] = "#layout"
                                +"Search"
                            }
                        }
                    }
                }
                nav("breadcrumb column is-12") {
                    attributes["aria-label"] = "breadcrumbs"
                    unsafe {
                        +breadcrumbs
                    }
                }
                div("fixed-grid column has-3-cols has-1-cols-mobile") {
                    id = "layout"
                    unsafe {
                        +content
                    }
                }
                div(classes = "navv") {
                    unsafe {
                        +pagination
                    }
                }
            }
            div {
                id = "modal-cont"
            }
            script(src = "https://cdn.jsdelivr.net/npm/htmx.org/dist/htmx.min.js") {}
            script(src = "https://unpkg.com/hyperscript.org@0.9.12") {}
/*
            script(type = "module"){
                unsafe {
                    raw("import hyperscript from 'https://cdn.jsdelivr.net/npm/hyperscript/+esm'")
                }
            }
*/
        }
    }

    fun DIV.NavTile(title: String, subtitle: String, href: String) {
        div("cell is-clickable") {
            layoutUpdateAttributes(href)
            article("box") {
                p("title") { +title }
                p("subtitle") { +subtitle }
            }
        }
    }
}
