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
import io.github.asm0dey.service.*
import io.ktor.http.*
import io.ktor.http.ContentType.Text.Html
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
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

private val String.encoded get() = this.replace("+", "%2b")


class Api(application: Application) : AbstractDIController(application) {
    private val info by instance<InfoService>()
    override fun Route.getRoutes() {
        route("/api") {
            get {
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    navTile("New books", "Recent publications from this catalog", "/api/new")
                    navTile("Books by series", "Authors by first letters", "/api/series/browse")
                    navTile("Books by author", "Series by first letters", "/api/author/c")
                    navTile("Genres", "Books by genres", "/api/genre")
                }
                return@get smartHtml(call, x, breadCrumbs("Library" to "/api"))

            }
            get("/search") {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val searchTerm = URLDecoder.decode(call.request.queryParameters["search"]!!, UTF_8)
                val (books, _, total) = info.searchBookByText(searchTerm, page - 1)
                val imageTypes = info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, imageTypes, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "Search: $searchTerm" to "/api/search?search=${searchTerm.encoded}",
                    )
                )
                val z = pagination(page, total, "/api/search?search=${searchTerm.encoded}")
                return@get smartHtml(call, x, y, z)
            }
            get("/new") {
                val books = info.latestBooks().map { BookWithInfo(it) }
                val imageTypes = info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, imageTypes, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "New" to "/api/new",
                    )
                )
                return@get smartHtml(call, x, y)

            }
            get("/author/c/{name?}") {
                val nameStart = URLDecoder.decode(call.parameters["name"] ?: "", UTF_8)
                val trim = nameStart.length < 5
                val items = info.authorNameStarts(nameStart, trim)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for ((name, countOrId) in items) {
                        if (trim) navTile(name, "$countOrId items inside", "/api/author/c/${name.encoded}")
                        else navTile(name, "Books by $name", "/api/author/browse/$countOrId")
                    }
                }
                val y = breadCrumbs(
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
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    if (inseries > 0)
                        navTile(
                            "By series",
                            "All books by $authorName in series",
                            "/api/author/browse/$authorId/series"
                        )
                    if (inseries > 0 && out > 0)
                        navTile(
                            "Out of series",
                            "All books by $authorName out of series",
                            "/api/author/browse/$authorId/out"
                        )
                    navTile(
                        "All books",
                        "All books by $authorName alphabetically",
                        "/api/author/browse/$authorId/all"
                    )

                }
                val y = breadCrumbs(
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
                    val x = createHTML(false).div("tile is-parent columns is-multiline") {
                        for ((id, genre, count) in genres) {
                            navTile(genre, "$count items", "/api/genre/$id")
                        }
                    }
                    val y = breadCrumbs("Library" to "/api", "By Authors" to "/api/genre")
                    return@get smartHtml(call, x, y)
                }
                route("/{id}") {
                    get {
                        val genreId = call.parameters["id"]?.toLong()!!
                        val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                        val x = createHTML(false).div("tile is-parent columns is-multiline") {
                            navTile("Authors", "Books in genre by author", "/api/genre/$genreId/author")
                            navTile("All", "All books in genre", "/api/genre/$genreId/all")
                        }
                        val y = breadCrumbs(
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
                        val x = createHTML(false).div("tile is-parent columns is-multiline") {
                            for (book in books) {
                                bookTile(book, images, descriptions)
                            }
                        }
                        val y = breadCrumbs(
                            "Library" to "/api",
                            "By Genre" to "/api/genre",
                            genreName to "/api/genre/$genreId",
                            "All books" to "/api/genre/$genreId/all",
                        )
                        val z = pagination(page, total, "/api/genre/$genreId/all")
                        return@get smartHtml(call, x, y, z)
                    }
                    route("/author") {
                        get {
                            val genreId = call.parameters["id"]?.toLong()!!
                            val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                            val genreAuthors = info.genreAuthors(genreId)
                            val x = createHTML(false).div("tile is-parent columns is-multiline") {
                                for ((id, name) in genreAuthors) {
                                    navTile(name, "Books in $genreName by $name", "/api/genre/$genreId/author/$id")
                                }
                            }
                            val y = breadCrumbs(
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
                            val x = createHTML(false).div("tile is-parent columns is-multiline") {
                                for (book in books) {
                                    bookTile(book, imageTypes, descriptions)
                                }
                            }
                            val y = breadCrumbs(
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
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, imageTypes, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        authorName to "/api/author/browse/$authorId",
                        "All books" to "/api/author/browse/$authorId/all"
                    )
                )
                val z = pagination(page, total, "/api/author/browse/$authorId/all")
                return@get smartHtml(call, x, y, z)
            }
            get("/author/browse/{id}/out") {
                val authorId = call.parameters["id"]!!.toLong()
                val books = info.booksWithoutSeriesByAuthorId(authorId)
                val authorName = info.authorName(authorId)

                val imageTypes = info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, imageTypes, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
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

                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for ((name, pair) in namesWithDates) {
                        navTile(name, "${pair.second} books", "/api/author/browse/$authorId/series/${name.encoded}")
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        authorName to "/api/author/browse/$authorId",
                        "Series" to path
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("/author/browse/{id}/series/{name}") {
                val authorId = call.parameters["id"]!!.toLong()
                val seriesName = URLDecoder.decode(call.parameters["name"]!!, UTF_8)
                val books = info.booksBySeriesAndAuthor(seriesName, authorId)
                val path = call.request.path()
                val imageTypes = info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val authorName = info.authorName(authorId)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (bookWithInfo in books) {
                        bookTile(bookWithInfo, imageTypes, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        authorName to "/api/author/browse/$authorId",
                        seriesName to path,
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("series/browse/{name?}") {
                val nameStart = (call.parameters["name"] ?: "").decoded
                val trim = nameStart.length < 5
                val series = info.seriesNameStarts(nameStart, trim)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for ((name, countOrId, complete) in series) {
                        if (!complete)
                            navTile(name, "$countOrId items inside", "/api/series/browse/${name.encoded}")
                        else
                            navTile(name, "$countOrId items inside", "/api/series/item/${name.encoded}")
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Series" to "/api/series/browse",
                        if (trim && nameStart.isNotBlank()) nameStart to "/api/series/browse/${nameStart.encoded}" else null
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("series/item/{name}") {
                val name = call.parameters["name"]!!.decoded
                val authorFilter = call.parameters["author"]?.toLongOrNull()
                val sorting = call.request.queryParameters["sort"] ?: "num"
                val books = info.booksBySeriesName(name)
                val sorted = when (sorting) {
                    "num" -> books.sortedBy { it.book.sequenceNumber ?: Int.MAX_VALUE }
                    "name" -> books.sortedBy { it.book.name!! }
                    else -> books
                }
                val filtered =
                    authorFilter?.let { aId -> sorted.filter { it.authors.any { it.id == aId } } }?.toList()
                        ?: sorted.toList()
                val imageTypes = info.imageTypes(filtered)
                val shortDescriptions = info.shortDescriptions(filtered)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (bookWithInfo in filtered) bookTile(bookWithInfo, imageTypes, shortDescriptions)
                }
                val y =
                    breadCrumbs(
                        "Library" to "/api",
                        "Series" to "/api/series/browse",
                        name to "/api/series/item/${name.encoded}"
                    )
                return@get smartHtml(call, x, y)
            }
            get("/book/{id}/info") {
                val bookId = call.parameters["id"]!!.toLong()
                val book = info.bookInfo(bookId)
                val descrHtml = bookDescriptionsLonger(listOf(bookId to book.book))[bookId]
                val hasImage = info.imageTypes(listOf(book))[bookId] != null
                val x = modalContent(book, hasImage, descrHtml)
                call.respondText(x, Html)
            }
            get("/book/{id}/image") {
                val bookId = call.parameters["id"]!!.toLong()
                val x = modalImageContent("/opds/image/$bookId")
                call.respondText(x, Html)
            }
        }

    }

    private fun modalImageContent(imageUrl: String): String {
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

    private fun DIV.navTile(title: String, subtitle: String, href: String) {
        div("tile is-parent is-4 is-clickable") {
            layoutUpdateAttributes(href)
            article("tile box is-child") {
                p("title") { +title }
                p("subtitle") { +subtitle }
            }
        }
    }

    private suspend fun smartHtml(
        call: ApplicationCall,
        content: String,
        breadcrumbs: String,
        pagination: String = pagination(1, 1, "")
    ) =
        if (call.request.headers["HX-Request"] == "true") call.respondText(
            content + breadcrumbs + pagination,
            Html
        )
        else call.respondHtml { fullHtml(breadcrumbs, content, pagination) }

    private fun DIV.bookTile(
        bookWithInfo: BookWithInfo,
        images: Map<Long, String?>,
        descriptionsShort: Map<Long, String?>
    ) {
        div("tile is-parent is-3") {
            div("tile is-child") {
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
                                    attributes["hx-target"] = "#modal-cont"
                                    attributes["_"] = "on htmx:afterOnLoad wait 10ms then add .is-active to #modal"
                                    img(src = "/opds/image/${bookWithInfo.id}") {
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
    }

    private fun breadCrumbs(vararg items: Pair<String, String>) = breadCrumbs(items.toList())

    private fun breadCrumbs(items: List<Pair<String, String>>) = createHTML(false).div {
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

    private fun pagination(curPage: Int, total: Int, base: String) = createHTML(false).div {
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
                        attributes["hx-target"] = "#layout"
                        attributes["hx-push-url"] = "true"
                    }
                    +"Next page"
                }
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

    private fun HTMLTag.layoutUpdateAttributes(href: String) {
        attributes["hx-trigger"] = "click"
        attributes["hx-get"] = href
        attributes["hx-target"] = "#layout"
        attributes["hx-push-url"] = "true"
    }

    private fun modalContent(
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
            link(rel = "stylesheet", href = "/webjars/bulma/css/bulma.min.css")
            link(rel = "stylesheet", href = "/webjars/font-awesome/css/all.min.css")
            script(src = "/webjars/htmx.org/dist/htmx.min.js") {
                defer = true
            }
            script(src = "/webjars/hyperscript.org/dist/_hyperscript.min.js") {
                defer = true
            }
        }
        body {
            nav(classes = "navbar is-black") {
                div("container") {
                    div("navbar-brand") {
                        a("navbar-item brand-text") {
                            attributes["hx-get"] = "/api"
                            attributes["hx-target"] = "#layout"
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
                div("tile is-ancestor column is-12") {
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
        }
    }
}
