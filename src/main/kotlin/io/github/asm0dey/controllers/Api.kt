package io.github.asm0dey.controllers

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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.math.min

class Api(application: Application) : AbstractDIController(application) {
    private val info by instance<InfoService>()
    override fun Route.getRoutes() {
        route("/api") {
            get {
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    navTile("New books", "Recent publications from this catalog", "/api/new")
                    navTile("Books by series", "Authors by first letters", "/api/series/browse")
                    navTile("Books by author", "Series by first letters", "/api/author/c")
                }
                return@get smartHtml(call, x, breadCrumbs("Library" to "/api"))

            }
            get("/search") {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val searchTerm = URLDecoder.decode(call.request.queryParameters["search"]!!, StandardCharsets.UTF_8)
                val (books, hasMore) = info.searchBookByText(searchTerm, page)
                val imageTypes = info.getImageTypes(books)
                val shortDescriptions = info.getShortDescriptions(books)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, imageTypes, shortDescriptions)
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "Search: $searchTerm" to "/api/search?search=$searchTerm",
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("/new") {
                val books = info.latestBooks().map { BookWithInfo(it) }
                val imageTypes = info.getImageTypes(books)
                val shortDescriptions = info.getShortDescriptions(books)
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
                val nameStart = URLDecoder.decode(call.parameters["name"] ?: "", StandardCharsets.UTF_8)
                val trim = nameStart.length < 5
                val items = info.authorNameStarts(nameStart, trim)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for ((name, countOrId) in items) {
                        if (trim) navTile(name, "$countOrId items inside", "/api/author/c/$name")
                        else navTile(name, "Books by $name", "/api/author/browse/$countOrId")
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Authors" to "/api/author/c",
                        if (trim && nameStart.isNotBlank()) nameStart to "/api/author/c/$nameStart" else null
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
            get("/author/browse/{id}/all") {
                val authorId = call.parameters["id"]!!.toLong()
                val books = info.allBooksByAuthor(authorId)
                val authorName = info.authorName(authorId)
                val imageTypes = info.getImageTypes(books)
                val shortDescriptions = info.getShortDescriptions(books)
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
                return@get smartHtml(call, x, y)
            }
            get("/author/browse/{id}/out") {
                val authorId = call.parameters["id"]!!.toLong()
                val books = info.booksWithoutSeriesByAuthorId(authorId)
                val authorName = info.authorName(authorId)

                val imageTypes = info.getImageTypes(books)
                val shortDescriptions = info.getShortDescriptions(books)
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
                        navTile(name, "${pair.second} books", "/api/author/browse/$authorId/series/$name")
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
                val seriesName = URLDecoder.decode(call.parameters["name"]!!, StandardCharsets.UTF_8)
                val books = info.booksBySeriesAndAuthor(seriesName, authorId)
                val path = call.request.path()
                val imageTypes = info.getImageTypes(books)
                val shortDescriptions = info.getShortDescriptions(books)
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
                            navTile(name, "$countOrId items inside", "/api/series/browse/$name")
                        else
                            navTile(name, "$countOrId items inside", "/api/series/item/$name")
                    }
                }
                val y = breadCrumbs(
                    listOfNotNull(
                        "Library" to "/api",
                        "By Series" to "/api/series/browse",
                        if (trim && nameStart.isNotBlank()) nameStart to "/api/series/browse/$nameStart" else null
                    )
                )
                return@get smartHtml(call, x, y)
            }
            get("series/item/{name}") {
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
                val imageTypes = info.getImageTypes(filtered)
                val shortDescriptions = info.getShortDescriptions(filtered)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (bookWithInfo in filtered) bookTile(bookWithInfo, imageTypes, shortDescriptions)
                }
                val y =
                    breadCrumbs("Library" to "/api", "Series" to "/api/series/browse", name to "/api/series/item/$name")
                return@get smartHtml(call, x, y)
            }
            get("/book/{id}/info") {
                val bookId = call.parameters["id"]!!.toLong()
                val book = info.bookInfo(bookId)
                val descrHtml = bookDescriptionsLonger(listOf(bookId to book.book))[bookId]
                val hasImage = info.imageTypes(listOf(bookId to book.book.path))[bookId] != null
                val x = modalContent(book, hasImage, descrHtml)
                call.respondText(x, ContentType.Text.Html)
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

    private suspend fun smartHtml(call: ApplicationCall, content: String, breadcrumbs: String) =
        if (call.request.headers["HX-Request"] == "true") call.respondText(content + breadcrumbs, ContentType.Text.Html)
        else call.respondHtml {
            fullHtml(breadcrumbs, content)
        }

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
                            figure("image is-2by3") {
                                img(src = "/opds/image/${bookWithInfo.id}")
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
                        a("/opds/book/${bookWithInfo.id}/download", classes = "card-footer-item") { +"Download" }
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
                                            span("tag is-rounded is-normal is-info is-light") {
                                                +genre
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

    private fun HTML.fullHtml(breadcrumbs: String, content: String) {
        head {
            meta(charset = "utf-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1")
            title("Asm0dey's library")
            link(rel = "stylesheet", href = "/webjars/bulma/css/bulma.min.css")
            link(rel = "stylesheet", href = "/webjars/font-awesome/css/all.min.css")
            script(src = "/webjars/htmx.org/dist/htmx.min.js") {
                defer = true
            }
            script(src = "/webjars/hyperscript.org/dist/_hyperscript_web.min.js") {
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
            }
            div {
                id = "modal-cont"
            }
        }
    }
}