package io.github.asm0dey.controllers

import io.github.asm0dey.epubConverterAccessible
import io.github.asm0dey.service.*
import io.ktor.http.*
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
import java.nio.charset.StandardCharsets
import kotlin.math.abs
import kotlin.math.min

class Simple(app: Application) : AbstractDIController(app) {
    private val info by instance<InfoService>()
    override fun Route.getRoutes() {
        route("/simple") {
            get {
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    navTile("New books", "Recent publications from this catalog", "/simple/new")
                    navTile("Books by series", "Authors by first letters", "/simple/series/browse")
                    navTile("Books by author", "Series by first letters", "/simple/author/c")
                    navTile("Genres", "Books by genres", "/simple/genre")
                }
                return@get call.respondHtml { fullHtml(breadCrumbs("Library" to "/simple"), x, pagination(1, 1, "")) }

            }
            get("/search") {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val searchTerm = URLDecoder.decode(call.request.queryParameters["search"]!!, StandardCharsets.UTF_8)
                val (books, _, total) = info.searchBookByText(searchTerm, page - 1)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/simple",
                        "Search: $searchTerm" to "/simple/search?search=${searchTerm.encoded}",
                    )
                )
                val z = pagination(page, total, "/simple/search?search=${searchTerm.encoded}")
                return@get call.respondHtml { fullHtml(y, x, z) }
            }
            get("/new") {
                val books = info.latestBooks().map { BookWithInfo(it) }
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/simple",
                        "New" to "/simple/new",
                    )
                )
                return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }

            }
            get("/author/c/{name?}") {
                val nameStart = URLDecoder.decode(call.parameters["name"] ?: "", StandardCharsets.UTF_8)
                val trim = nameStart.length < 5
                val items = info.authorNameStarts(nameStart, trim)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for ((name, countOrId) in items) {
                        if (trim) navTile(name, "$countOrId items inside", "/simple/author/c/${name.encoded}")
                        else navTile(name, "Books by $name", "/simple/author/browse/$countOrId")
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/simple",
                        "By Authors" to "/simple/author/c",
                        if (trim && nameStart.isNotBlank()) nameStart to "/simple/author/c/${nameStart.encoded}" else null
                    )
                )
                return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
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
                            "/simple/author/browse/$authorId/series"
                        )
                    if (inseries > 0 && out > 0)
                        navTile(
                            "Out of series",
                            "All books by $authorName out of series",
                            "/simple/author/browse/$authorId/out"
                        )
                    navTile(
                        "All books",
                        "All books by $authorName alphabetically",
                        "/simple/author/browse/$authorId/all"
                    )

                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/simple",
                        "By Authors" to "/simple/author/c",
                        authorName to "/simple/author/browse/$authorId"
                    )
                )
                return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
            }
            route("/genre") {
                get {
                    val genres = info.genres()
                    val x = createHTML(false).div("tile is-parent columns is-multiline") {
                        for ((id, genre, count) in genres) {
                            navTile(genre, "$count items", "/simple/genre/$id")
                        }
                    }
                    val y = breadCrumbs("Library" to "/simple", "By Authors" to "/simple/genre")
                    return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
                }
                route("/{id}") {
                    get {
                        val genreId = call.parameters["id"]?.toLong()!!
                        val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                        val x = createHTML(false).div("tile is-parent columns is-multiline") {
                            navTile("Authors", "Books in genre by author", "/simple/genre/$genreId/author")
                            navTile("All", "All books in genre", "/simple/genre/$genreId/all")
                        }
                        val y = breadCrumbs(
                            "Library" to "/simple",
                            "By Genre" to "/simple/genre",
                            genreName to "/simple/genre/$genreId"
                        )
                        return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
                    }
                    get("/all") {
                        val genreId = call.parameters["id"]?.toLong()!!
                        val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                        val (total, books) = info.booksInGenre(genreId, page - 1)
                        val descriptions = info.shortDescriptions(books)
                        val x = createHTML(false).div("tile is-parent columns is-multiline") {
                            for (book in books) {
                                bookTile(book, descriptions)
                            }
                        }
                        val y = breadCrumbs(
                            "Library" to "/simple",
                            "By Genre" to "/simple/genre",
                            genreName to "/simple/genre/$genreId",
                            "All books" to "/simple/genre/$genreId/all",
                        )
                        val z = pagination(page, total, "/simple/genre/$genreId/all")
                        return@get call.respondHtml { fullHtml(y, x, z) }
                    }
                    route("/author") {
                        get {
                            val genreId = call.parameters["id"]?.toLong()!!
                            val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                            val genreAuthors = info.genreAuthors(genreId)
                            val x = createHTML(false).div("tile is-parent columns is-multiline") {
                                for ((id, name) in genreAuthors) {
                                    navTile(name, "Books in $genreName by $name", "/simple/genre/$genreId/author/$id")
                                }
                            }
                            val y = breadCrumbs(
                                "Library" to "/simple",
                                "By Genre" to "/simple/genre",
                                genreName to "/simple/genre/$genreId",
                                "By Author" to "/simple/genre/$genreId/author",
                            )
                            return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
                        }
                        get("/{aid}") {
                            val genreId = call.parameters["id"]?.toLong()!!
                            val genreName = info.genreName(genreId) ?: return@get call.respond(HttpStatusCode.NotFound)
                            val authorId = call.parameters["aid"]?.toLong()!!
                            val (authorName, books) = info.booksByGenreAndAuthor(genreId, authorId)
                            val descriptions = info.shortDescriptions(books)
                            val x = createHTML(false).div("tile is-parent columns is-multiline") {
                                for (book in books) {
                                    bookTile(book, descriptions)
                                }
                            }
                            val y = breadCrumbs(
                                "Library" to "/simple",
                                "By Genre" to "/simple/genre",
                                genreName to "/simple/genre/$genreId",
                                "By Author" to "/simple/genre/$genreId/author",
                                authorName to "/simple/genre/$genreId/author/$authorId",
                            )
                            return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
                        }
                    }
                }
            }
            get("/author/browse/{id}/all") {
                val authorId = call.parameters["id"]!!.toLong()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val (total, books) = info.allBooksByAuthor(authorId, page - 1)
                val authorName = info.authorName(authorId)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/simple",
                        "By Authors" to "/simple/author/c",
                        authorName to "/simple/author/browse/$authorId",
                        "All books" to "/simple/author/browse/$authorId/all"
                    )
                )
                val z = pagination(page, total, "/simple/author/browse/$authorId/all")
                return@get call.respondHtml { fullHtml(y, x, z) }
            }
            get("/author/browse/{id}/out") {
                val authorId = call.parameters["id"]!!.toLong()
                val books = info.booksWithoutSeriesByAuthorId(authorId)
                val authorName = info.authorName(authorId)

                info.imageTypes(books)
                val shortDescriptions = info.shortDescriptions(books)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/simple",
                        "By Authors" to "/simple/author/c",
                        authorName to "/simple/author/browse/$authorId",
                        "Out of series" to "/simple/author/browse/$authorId/out"
                    )
                )
                return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
            }
            get("/author/browse/{id}/series") {
                val authorId = call.parameters["id"]!!.toLong()
                val path = call.request.path()
                val namesWithDates = info.seriesByAuthorId(authorId)
                val authorName = info.authorName(authorId)

                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for ((name, pair) in namesWithDates) {
                        navTile(
                            name.second,
                            "${pair.second} books",
                            "/simple/author/browse/$authorId/series/${name.first}"
                        )
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/simple",
                        "By Authors" to "/simple/author/c",
                        authorName to "/simple/author/browse/$authorId",
                        "Series" to path
                    )
                )
                return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
            }
            get("/author/browse/{id}/series/{seqId}") {
                val authorId = call.parameters["id"]!!.toLong()
                val seqId = call.parameters["seqId"]!!.toLong()
                val books = info.booksBySeriesAndAuthor(seqId, authorId)
                val path = call.request.path()
                val shortDescriptions = info.shortDescriptions(books)
                val authorName = info.authorName(authorId)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (bookWithInfo in books) {
                        bookTile(bookWithInfo, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/simple",
                        "By Authors" to "/simple/author/c",
                        authorName to "/simple/author/browse/$authorId",
                        books.firstOrNull()?.sequence?.to(path),
                    )
                )
                return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
            }
            get("series/browse/{name?}") {
                val nameStart = (call.parameters["name"] ?: "").decoded
                val trim = nameStart.length < 5
                val series = info.seriesNameStarts(nameStart, trim)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for ((name, countOrId, complete, seqid) in series) {
                        if (!complete)
                            navTile(name, "$countOrId items inside", "/simple/series/browse/${name.encoded}")
                        else
                            navTile(name, "$countOrId items inside", "/simple/series/item/$seqid")
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/simple",
                        "By Series" to "/simple/series/browse",
                        if (trim && nameStart.isNotBlank()) nameStart to "/simple/series/browse/${nameStart.encoded}" else null
                    )
                )
                return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
            }
            get("series/item/{seqId}") {
                val seqId = call.parameters["seqId"]!!.toInt()
                val authorFilter = call.parameters["author"]?.toLongOrNull()
                val sorting = call.request.queryParameters["sort"] ?: "num"
                val books = info.booksBySeriesId(seqId)
                val sorted = when (sorting) {
                    "num" -> books.sortedBy { it.book.sequenceNumber ?: Long.MAX_VALUE }
                    "name" -> books.sortedBy { it.book.name }
                    else -> books
                }
                val filtered =
                    authorFilter?.let { aId -> sorted.filter { it.authors.any { it.id == aId } } }?.toList()
                        ?: sorted.toList()
                val shortDescriptions = info.shortDescriptions(filtered)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (bookWithInfo in filtered) bookTile(bookWithInfo, shortDescriptions)
                }
                val y =
                    breadCrumbs(
                        "Library" to "/simple",
                        "Series" to "/simple/series/browse",
                        books.first().sequence!! to "/simple/series/item/$seqId"
                    )
                return@get call.respondHtml { fullHtml(y, x, pagination(1, 1, "")) }
            }
            get("/book/{id}/info") {
                val bookId = call.parameters["id"]!!.toLong()
                val book = info.bookInfo(bookId)
                val descrHtml = bookDescriptionsLonger(listOf(bookId to book.book))[bookId]
                val x = modalContent(book, descrHtml)
                call.respondText(x, ContentType.Text.Html)
            }
            get("/book/{id}/image") {
                val bookId = call.parameters["id"]!!.toLong()
                val x = modalImageContent("/opds/image/$bookId")
                call.respondText(x, ContentType.Text.Html)
            }
        }
    }

    private fun DIV.navTile(title: String, subtitle: String, href: String) {
        div("tile is-parent is-4 is-clickable") {
            a(href = href) {
                article("tile box is-child") {
                    p("title") { +title }
                    p("subtitle") { +subtitle }
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
//            script(src = "/webjars/htmx.org/dist/htmx.min.js") {
//                defer = true
//            }
            script(src = "/webjars/hyperscript.org/dist/_hyperscript.min.js") {
                defer = true
            }
        }
        body {
            nav(classes = "navbar is-black") {
                div("container") {
                    div("navbar-brand") {
                        a(href = "/simple", classes = "navbar-item brand-text") { text("Library") }
                    }
                }
            }
            div("container") {
                div("section") {
                    attributes["hx-boost"] = "true"
                    div("field has-addons") {
                        form(action = "/simple/search") {
                            div("control has-icons-left is-expanded") {
                                input(InputType.text, name = "search", classes = "input") {
                                    id = "search"
                                }
                                span("icon is-small is-left") {
                                    i("fas fa-search")
                                }
                            }
                            div("control") {
                                button(classes = "button", type = ButtonType.submit) {
                                    +"Search"
                                }
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
                        href = base.withParam("page=${curPage - 1}")
                    }
                    +"Previous page"
                }
                a {
                    classes = setOfNotNull("pagination-next", if (curPage == last) "is-disabled" else null)
                    if (curPage != last) {
                        href = base.withParam("page=${curPage + 1}")
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
                                a(
                                    href = base.withParam("page=$page"),
                                    classes = setOfNotNull(
                                        "pagination-link",
                                        if (curPage == page) "is-current" else null
                                    ).joinToString(", ")
                                ) {
                                    +(page.toString())
                                }
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
                    a(href) {
                        +name
                    }
                }
            }
        }
    }

    private fun DIV.bookTile(
        bookWithInfo: BookWithInfo,
        descriptionsShort: Map<Long, String?>
    ) {
        div("tile is-parent is-4") {
            div("tile is-child") {
                div("card") {
                    div("card-header") {
                        p("card-header-title") {
                            +bookWithInfo.book.name
                        }
                    }
/*
                    if (images[bookWithInfo.id] != null) {
                        div("card-image") {
                            figure("image") {
                                img(src = "/opds/image/${bookWithInfo.id}") {
                                    attributes["loading"] = "lazy"
                                    style = "width: 200px"
                                }
                            }
                        }
                    }
*/
                    div("card-content") {
                        div("content") {
                            text((descriptionsShort[bookWithInfo.id]?.let {
                                it.substring(0 until min(it.length, 200))
                            }?.plus('…') ?: ""))
                        }
                    }
                    footer("card-footer mb-0 pb-0 is-align-items-self-end") {
                        a("/simple/book/${bookWithInfo.id}/info", classes = "card-footer-item") {
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

    private fun modalContent(
        book: BookWithInfo,
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
/*
                        if (hasImage) {
                            figure("media-left") {
                                p("image") {
                                    img(src = "/opds/image/${book.id}") {
                                        attributes["style"] = "width: 172px"
                                    }
                                }
                            }
                        }
*/
                        div("media-content") {
                            div("content") {
                                p {
                                    div("tags") {
                                        for (author in book.authors) {
                                            a("/simple/author/browse/${author.id}") {
                                                attributes["_"] = closeModalScript
                                                span("tag is-rounded is-normal is-link is-medium is-light") {
                                                    +author.buildName()
                                                }
                                            }
                                        }
                                    }
                                    div("tags") {
                                        for (genre in book.genres) {
                                            a("/simple/genre/${genre.second}") {
                                                attributes["_"] = closeModalScript
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

}
