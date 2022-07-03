@file:Suppress("HttpUrlsUsage")

package io.github.asm0dey.plugins

import com.kursx.parser.fb2.Binary
import com.kursx.parser.fb2.Element
import com.kursx.parser.fb2.FictionBook
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.asm0dey.genreNames
import io.github.asm0dey.model.Entry
import io.github.asm0dey.model.Entry.Link
import io.github.asm0dey.opdsko.jooq.Tables.*
import io.github.asm0dey.opdsko.jooq.tables.Book
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IAuthor
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IBook
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author
import io.github.asm0dey.startScanTask
import io.ktor.http.ContentDisposition.Companion.Attachment
import io.ktor.http.ContentDisposition.Parameters.FileName
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Text.Html
import io.ktor.http.HttpHeaders.ContentDisposition
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.jte.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.html.InputType.text
import kotlinx.html.stream.createHTML
import org.apache.commons.codec.binary.Base64
import org.jooq.*
import org.jooq.conf.Settings
import org.jooq.impl.DSL.*
import org.jooq.impl.DefaultExecuteListener
import org.jooq.impl.DefaultExecuteListenerProvider
import org.tinylog.kotlin.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import java.text.StringCharacterIterator
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author as AuthorPojo
import io.github.asm0dey.opdsko.jooq.tables.pojos.Book as BookPojo

val dtf: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
const val NAVIGATION_TYPE = "application/atom+xml;profile=opds-catalog;kind=navigation;charset=utf-8"
const val ACQUISITION_TYPE = "application/atom+xml;profile=opds-catalog;kind=acquisition;charset=utf-8"

fun Application.routes() {
    routing {
        static("/") {
            staticBasePackage = "static"
            resources(".")
            defaultResource("index.html")
        }
        post("/scan") {
            this@routes.startScanTask()
        }
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
                val searchTerm = URLDecoder.decode(call.request.queryParameters["search"]!!, UTF_8)
                val (books, hasMore) = searchBookByText(searchTerm, page)
                val imageTypes = books.imageTypes
                val shortDescriptions = books.shortDescriptions
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
                val books = latestBooks().map { BookWithInfo(it) }
                val images = books.imageTypes
                val descriptions = books.shortDescriptions
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, images, descriptions)
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
                val items = authorNameStarts(nameStart, trim)
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
                val (inseries, out) = hasSeries(authorId)
                val authorName = authorName(authorId)
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
                val books = getBookWithInfo()
                    .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
                    .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
                    .orderBy(BOOK.NAME)
                    .fetch { BookWithInfo(it) }
                val authorName = authorName(authorId)
                val images = books.imageTypes
                val descriptions = books.shortDescriptions
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, images, descriptions)
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
                val books = booksWithoutSeriesByAuthorId(authorId)
                val authorName = authorName(authorId)

                val images = books.imageTypes
                val pathsByIds = books.map { it.id to it.book }
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (book in books) {
                        bookTile(book, images, bookDescriptionsShorter(pathsByIds))
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
                val namesWithDates = seriesByAuthorId(authorId)
                val authorName = authorName(authorId)

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
                val seriesName = URLDecoder.decode(call.parameters["name"]!!, UTF_8)
                val result = booksBySeriesAndAuthor(seriesName, authorId)
                val path = call.request.path()
                val images = result.imageTypes
                val descriptions = result.shortDescriptions
                val authorName = authorName(authorId)
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (bookWithInfo in result) {
                        bookTile(bookWithInfo, images, descriptions)
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
                val series = seriesNameStarts(nameStart, trim)
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
                val books = getBookWithInfo()
                    .where(BOOK.SEQUENCE.eq(name))
                    .fetch { BookWithInfo(it) }
                    .asSequence()
                val allAuthors = books.flatMap { it.authors.map { it.buildName() to it.id } }.toSet()
                val sorted = when (sorting) {
                    "num" -> books.sortedBy { it.book.sequenceNumber ?: Int.MAX_VALUE }
                    "name" -> books.sortedBy { it.book.name!! }
                    else -> books
                }
                val filtered =
                    authorFilter?.let { aId -> sorted.filter { it.authors.any { it.id == aId } } }?.toList()
                        ?: sorted.toList()
                val images = filtered.imageTypes
                val descriptionsShort = filtered.shortDescriptions
                val x = createHTML(false).div("tile is-parent columns is-multiline") {
                    for (bookWithInfo in filtered) bookTile(bookWithInfo, images, descriptionsShort)
                }
                val y =
                    breadCrumbs("Library" to "/api", "Series" to "/api/series/browse", name to "/api/series/item/$name")
                return@get smartHtml(call, x, y)
            }
            get("/book/{id}/info") {
                val bookId = call.parameters["id"]!!.toLong()
                val book = BookWithInfo(
                    getBookWithInfo()
                        .where(BOOK.ID.eq(bookId))
                        .fetchSingle()
                )
                val descrHtml = bookDescriptionsLonger(listOf(bookId to book.book))[bookId]
                val hasImage = imageTypes(listOf(bookId to book.book.path))[bookId] != null
                val x = modalContent(book, hasImage, descrHtml)
                call.respondText(x, Html)
            }
        }

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
                    val searchTerm = URLDecoder.decode(call.parameters["searchTerm"]!!, UTF_8)
                    val (books, hasMore) = searchBookByText(searchTerm, page)
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
                val books = latestBooks().map { BookWithInfo(it) }
                call.respond(
                    bookJte(books(books, call.request.path(), "Latest books", "new"))
                )
            }
            get("/image/{id}") {
                val (binary, data) = imageByBookId(call.parameters["id"]!!.toLong())
                call.respondBytes(data, ContentType.parse(binary.contentType))
            }
            route("/book") {
                get("/{id}/info") {
                    val bookId = call.parameters["id"]!!.toLong()
                    val book = BookWithInfo(
                        getBookWithInfo()
                            .where(BOOK.ID.eq(bookId))
                            .fetchSingle()
                    )
                    call.respond(
                        JteContent(
                            "entry.kte",
                            params = mapOf(
                                "book" to book,
                                "imageType" to imageTypes(listOf(book.id to book.book.path))[book.id],
                                "summary" to bookDescriptionsShorter(listOf(book.id to book.book))[book.id],
                                "content" to bookDescriptionsLonger(listOf(book.id to book.book))[book.id],
                            ),
                            contentType = ContentType.parse("application/atom+xml;type=entry;profile=opds-catalog")
                        )
                    )
                }
                get("/{id}/download") {
                    val bookId = call.parameters["id"]!!.toLong()
                    call.response.header(
                        ContentDisposition,
                        Attachment.withParameter(FileName, "$bookId.fb2.zip").toString()
                    )
                    val bytes = ByteArrayOutputStream().use { baos ->
                        ZipOutputStream(baos).use {
                            it.apply {
                                val path = bookPath(bookId).await().single().value1()
                                putNextEntry(ZipEntry("$bookId.fb2"))
                                withContext(Dispatchers.IO) {
                                    write(File(path).readBytes())
                                }
                                closeEntry()
                            }
                        }
                        baos.toByteArray()
                    }
                    call.respondBytes(bytes, ContentType.parse("application/fb+zip"))

                }
            }
            route("/author") {
                get("/c/{name?}") {
                    val path = call.request.path()
                    val nameStart = URLDecoder.decode(call.parameters["name"] ?: "", UTF_8)
                    val trim = nameStart.length < 5
                    val items = authorNameStarts(nameStart, trim)
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
                        val authorName = authorName(authorId)
                        val (inseries, out) = hasSeries(authorId)
                        val latestAuthorUpdate = latestAuthorUpdate(authorId).z
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
                        val books = getBookWithInfo()
                            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
                            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
                            .orderBy(BOOK.NAME)
                            .fetch { BookWithInfo(it) }
                        val authorName = authorName(authorId)
                        call.respond(
                            bookJte(books(books, path, "All books by $authorName", "author:$authorId:all"))
                        )
                    }
                    get("/{id}/series") {
                        val authorId = call.parameters["id"]!!.toLong()
                        val path = call.request.path()
                        val namesWithDates = seriesByAuthorId(authorId)
                        val authorName = authorName(authorId)
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
                        val seriesName = URLDecoder.decode(call.parameters["name"]!!, UTF_8)
                        val result = booksBySeriesAndAuthor(seriesName, authorId)
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
                        val books = booksWithoutSeriesByAuthorId(authorId)
                        val authorName = authorName(authorId)
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
                    val series = seriesNameStarts(nameStart, trim)
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
                                        Link(
                                            "subsection",
                                            if (complete) "/opds/series/item/${name.encoded}" else "/opds/series/browse/${name.encoded}",
                                            NAVIGATION_TYPE,
                                            count.toLong()
                                        )
                                    )
                                },
                            )
                        )
                    )
                }
                get("/item/{name}") {
                    val path = call.request.path()
                    val name = call.parameters["name"]!!.decoded
                    val authorFilter = call.parameters["author"]?.toLongOrNull()
                    val sorting = call.request.queryParameters["sort"] ?: "num"
                    val books = getBookWithInfo()
                        .where(BOOK.SEQUENCE.eq(name))
                        .fetch { BookWithInfo(it) }
                        .asSequence()
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
                                    Link(
                                        "http://opds-spec.org/facet",
                                        "$path?sort=num" + if (authorFilter != null) "&author=$authorFilter" else "",
                                        ACQUISITION_TYPE,
                                        title = "Order by number in series",
                                        facetGroup = "Sorting",
                                        activeFacet = sorting == "num"
                                    ),
                                    Link(
                                        "http://opds-spec.org/facet",
                                        "$path?sort=name" + if (authorFilter != null) "&author=$authorFilter" else "",
                                        ACQUISITION_TYPE,
                                        title = "Order by name of book",
                                        facetGroup = "Sorting",
                                        activeFacet = sorting == "name"
                                    )
                                ) + if (allAuthors.size > 1) {
                                    allAuthors.map { (authorName, id) ->
                                        Link(
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
}

private suspend fun smartHtml(call: ApplicationCall, content: String, breadcrumbs: String) =
    if (call.request.headers["HX-Request"] == "true") call.respondText(content + breadcrumbs, Html)
    else call.respondText(renderFullHtml(content, breadcrumbs), Html)


fun renderFullHtml(content: String, breadcrumbs: String): String {
    return createHTML(false).html {
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
                            input(text, name = "search", classes = "input") {
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

fun breadCrumbs(vararg items: Pair<String, String>) = breadCrumbs(items.toList())

fun breadCrumbs(items: List<Pair<String, String>>) = createHTML(false).div {
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

private fun DIV.navTile(title: String, subtitle: String, href: String) {
    div("tile is-parent is-4 is-clickable") {
        layoutUpdateAttributes(href)
        article("tile box is-child") {
            p("title") { +title }
            p("subtitle") { +subtitle }
        }
    }
}

private fun HTMLTag.layoutUpdateAttributes(href: String) {
    attributes["hx-trigger"] = "click"
    attributes["hx-get"] = href
    attributes["hx-target"] = "#layout"
    attributes["hx-push-url"] = "true"
}


private val String.decoded: String get() = URLDecoder.decode(this, UTF_8)


private fun navJte(params: Map<String, Any>) = JteContent(
    "navigation.kte", params, contentType = ContentType.parse(NAVIGATION_TYPE)
)

fun bookJte(params: Map<String, Any>): JteContent = JteContent(
    "books.kte", params, contentType = ContentType.parse(ACQUISITION_TYPE)
)

private fun authorSeries(namesWithDates: Map<String, Pair<LocalDateTime, Int>>, authorId: Long) =
    namesWithDates.map { (name, dateAndCount) ->
        Entry(
            name,
            listOf(
                Link(
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

private fun authorStartEntries(items: List<Pair<String, Long>>, trim: Boolean, nameStart: String?) =
    items.map { (name, countOrId) ->
        Entry(
            name,
            listOf(
                Link(
                    "subsection",
                    if (trim) "/opds/author/c/${name.encoded}"
                    else "/opds/author/browse/${countOrId}",
                    NAVIGATION_TYPE, if (trim) countOrId else null,
                )
            ),
            "authors:start_with:$nameStart", name + if (trim) ": $countOrId books" else "", ZonedDateTime.now()
        )
    }

private val String.encoded: String get() = URLEncoder.encode(this, UTF_8)

private fun authorEntries(
    inseries: Int,
    authorId: Long,
    authorName: String,
    out: Int,
    latestAuthorUpdate: ZonedDateTime,
) = listOfNotNull(
    if (inseries > 0) Entry(
        "By series",
        listOf(Link("subsection", "/opds/author/browse/$authorId/series", NAVIGATION_TYPE)),
        "author:$authorId:series", "All books by $authorName by series", ZonedDateTime.now(),
    ) else null,
    if (out > 0 && inseries > 0) Entry(
        "Out of series",
        listOf(Link("subsection", "/opds/author/browse/$authorId/out", NAVIGATION_TYPE)),
        "author:$authorId:out", "All books by $authorName", ZonedDateTime.now(),
    ) else null,
    Entry(
        "All books",
        listOf(Link("subsection", "/opds/author/browse/$authorId/all", NAVIGATION_TYPE)),
        "author:$authorId:all", "All books by $authorName", latestAuthorUpdate,
    ),
)

private fun navigation(
    path: String,
    id: String,
    title: String,
    updated: ZonedDateTime = ZonedDateTime.now(),
    entries: List<Entry>,
    additionalLinks: List<Link> = listOf(),
) = mapOf(
    "feedId" to id,
    "feedTitle" to title,
    "feedUpdated" to updated,
    "path" to path,
    "entries" to entries,
    "additionalLinks" to additionalLinks
)

private fun searchPaginationLinks(
    hasMore: Boolean,
    searchTerm: String,
    page: Int,
) = listOfNotNull(
    if (hasMore) {
        Link("next", "/opds/search/${searchTerm.encoded}?page=${page + 1}", ACQUISITION_TYPE)
    } else null,
    if (page > 0) {
        Link("previous", "/opds/search/${searchTerm.encoded}?page=${page - 1}", ACQUISITION_TYPE)
    } else null,
    if (page > 0) {
        Link("first", "/opds/search/${searchTerm.encoded}", ACQUISITION_TYPE)
    } else null,
)

private fun books(
    books: List<BookWithInfo>,
    path: String,
    title: String,
    id: String,
    updated: ZonedDateTime = books.maxOf { it.book.added }.z,
    additionalLinks: List<Link> = listOf(),
) = mapOf(
    "books" to books,
    "bookDescriptions" to books.shortDescriptions,
    "imageTypes" to books.imageTypes,
    "path" to path,
    "title" to title,
    "feedId" to id,
    "feedUpdated" to updated,
    "additionalLinks" to additionalLinks
)

private val List<BookWithInfo>.shortDescriptions get() = bookDescriptionsShorter(map { it.id to it.book })

private val List<BookWithInfo>.imageTypes get() = imageTypes(map { it.id to it.book.path })

private fun booksWithoutSeriesByAuthorId(authorId: Long): MutableList<BookWithInfo> {
    return getBookWithInfo()
        .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
        .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNull)
        .orderBy(BOOK.NAME)
        .fetch { BookWithInfo(it) }
}

val LocalDateTime.z: ZonedDateTime
    get() = ZonedDateTime.of(this, ZoneId.systemDefault())

private fun booksBySeriesAndAuthor(
    seriesName: String,
    authorId: Long,
): List<BookWithInfo> = getBookWithInfo()
    .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
    .where(BOOK.SEQUENCE.eq(seriesName), BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
    .orderBy(BOOK.SEQUENCE_NUMBER.asc().nullsLast(), BOOK.NAME)
    .fetch { BookWithInfo(it) }

private fun authorNameStarts(prefix: String, trim: Boolean = true): List<Pair<String, Long>> {
    val primaryNamesAreNulls = AUTHOR.LAST_NAME.isNull
        .and(AUTHOR.MIDDLE_NAME.isNull)
        .and(AUTHOR.FIRST_NAME.isNull)
    val fullName = trim(
        concat(
            if_(AUTHOR.LAST_NAME.isNotNull, AUTHOR.LAST_NAME.concat(" "), ""),
            if_(AUTHOR.FIRST_NAME.isNotNull, AUTHOR.FIRST_NAME.concat(" "), ""),
            if_(AUTHOR.MIDDLE_NAME.isNotNull, AUTHOR.MIDDLE_NAME.concat(" "), ""),
            if_(
                AUTHOR.NICKNAME.isNull, "", concat(
                    if_(primaryNamesAreNulls, "", "("),
                    AUTHOR.NICKNAME,
                    if_(primaryNamesAreNulls, "", ")"),
                )
            )
        )
    )

    val toSelect = (
            if (trim) substring(fullName, 1, prefix.length + 1)
            else fullName).`as`("term")
    val second = (if (trim) count(AUTHOR.ID).cast(Long::class.java) else AUTHOR.ID).`as`("number")
    return create.selectDistinct(
        toSelect, second
    )
        .from(AUTHOR)
        .where(toSelect.isNotNull, toSelect.ne(""), toSelect.startsWith(prefix))
        .groupBy(toSelect)
        .orderBy(toSelect)
        .fetch { it[toSelect] to it[second] }
}

private fun seriesNameStarts(
    prefix: String,
    trim: Boolean = true,
): List<Triple<String, Int, Boolean>> {
    val fst = (
            if (trim) substring(BOOK.SEQUENCE, 1, prefix.length + 1)
            else BOOK.SEQUENCE
            )
        .`as`("term")
    val snd = count(BOOK.ID).`as`("cnt")
    val third = if (trim) field(length(BOOK.SEQUENCE).eq(prefix.length)) else value(true)
    return create
        .selectDistinct(fst, snd, third)
        .from(BOOK)
        .where(fst.isNotNull, trim(fst).ne(""), fst.startsWith(prefix))
        .groupBy(fst)
        .orderBy(fst)
        .fetch { Triple(it[fst], it[snd], it[third]) }
}

@Suppress("SpellCheckingInspection")
const val OPDSKO_JDBC = "jdbc:sqlite:opds.db"

var ds = run {
    val config = HikariConfig()
    config.poolName = "opdsko pool"
    config.jdbcUrl = OPDSKO_JDBC
    config.connectionTestQuery = "SELECT 1"
    config.maxLifetime = 60000
    config.idleTimeout = 45000
    config.maximumPoolSize = 3
    HikariDataSource(config)
}

val create = using(ds, SQLDialect.SQLITE).apply {
    settings().apply {
        isExecuteLogging = false
    }
    configuration().set(DefaultExecuteListenerProvider(object : DefaultExecuteListener() {
        override fun executeStart(ctx: ExecuteContext) {
            val create =
                using(ctx.dialect(), Settings().withRenderFormatted(false))
            if (ctx.query() != null) {
                Logger.tag("JOOQ").info(create.renderInlined(ctx.query()))
            }
        }
    }))
}

private fun seriesByAuthorId(authorId: Long): Map<String, Pair<LocalDateTime, Int>> {
    val latestBookInSeq = max(BOOK.ADDED)
    val booksInSeries = count(BOOK.ID)
    return create.select(BOOK.SEQUENCE, latestBookInSeq, booksInSeries)
        .from(BOOK)
        .innerJoin(BOOK_AUTHOR).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
        .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNotNull)
        .groupBy(BOOK.SEQUENCE)
        .orderBy(BOOK.SEQUENCE, latestBookInSeq.desc())
        .fetch {
            it[BOOK.SEQUENCE] to (it[latestBookInSeq] to it[booksInSeries])
        }
        .toMap()
}

val bookAuthors = multiset(
    selectDistinct(AUTHOR.asterisk())
        .from(AUTHOR)
        .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.AUTHOR_ID.eq(AUTHOR.ID))
        .where(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
).`as`("authors").convertFrom { it.into(AuthorPojo::class.java) }

val bookGenres = multiset(
    selectDistinct(GENRE.NAME)
        .from(GENRE)
        .innerJoin(BOOK_GENRE).on(BOOK_GENRE.GENRE_ID.eq(GENRE.ID))
        .where(BOOK_GENRE.BOOK_ID.eq(BOOK.ID))
).`as`("genres").convertFrom { it.toList() }

private fun Result<Record1<String>>.toList(): List<String> =
    collect(Records.intoList())

val bookById = run {
    val bookAlias = Book("b")
    multiset(selectFrom(bookAlias).where(bookAlias.ID.eq(BOOK.ID)))
        .`as`("book")
        .convertFrom { it.into(BookPojo::class.java) }

}

private fun getBookWithInfo(): SelectJoinStep<Record4<Long, List<BookPojo>, List<Author>, List<String>>> = create
    .selectDistinct(
        BOOK.ID,
        bookById,
        bookAuthors,
        bookGenres,
    )
    .from(BOOK)


private fun hasSeries(authorId: Long): Pair<Int, Int> {
    val notnull = count(BOOK.ID).filterWhere(BOOK.SEQUENCE.isNotNull).`as`("x")
    val isnull = count(BOOK.ID).filterWhere(BOOK.SEQUENCE.isNull).`as`("y")
    return create
        .select(
            notnull,
            isnull,
        )
        .from(BOOK_AUTHOR)
        .innerJoin(BOOK).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
        .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
        .limit(1)
        .fetchSingle { it[notnull] to it[isnull] }
}

private fun latestAuthorUpdate(authorId: Long): LocalDateTime {
    return create
        .select(BOOK.ADDED)
        .from(BOOK)
        .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
        .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
        .orderBy(BOOK.ADDED.desc())
        .limit(1)
        .fetchSingle { it[BOOK.ADDED] }
}


private fun bookPath(bookId: Long) =
    create.select(BOOK.PATH).from(BOOK).where(BOOK.ID.eq(bookId)).fetchAsync()

private suspend fun imageByBookId(bookId: Long): Pair<Binary, ByteArray> {
    val path = bookPath(bookId).await().single().value1()
    val fb = FictionBook(File(path))
    val id = fb.description.titleInfo.coverPage.first().value.replace("#", "")
    val binary = fb.binaries[id]!!
    val data = Base64().decode(binary.binary)
    return Pair(binary, data)
}

fun imageTypes(pathsByIds: List<Pair<Long, String>>): Map<Long, String?> {
    return pathsByIds
        .associate { (id, path) ->
            val fb = FictionBook(File(path))
            val type = fb.description.titleInfo.coverPage.firstOrNull()?.value?.replace("#", "")?.let {
                fb.binaries[it]?.contentType
            }
            id to type
        }

}

private fun authorName(authorId: Long): String {
    val fullname = concat(
        coalesce(AUTHOR.LAST_NAME, ""),
        if_(
            AUTHOR.FIRST_NAME.isNotNull,
            if_(AUTHOR.LAST_NAME.isNotNull, concat(", ", AUTHOR.FIRST_NAME), AUTHOR.FIRST_NAME),
            ""
        ),
        if_(
            AUTHOR.MIDDLE_NAME.isNotNull,
            if_(
                AUTHOR.FIRST_NAME.isNotNull.or(AUTHOR.LAST_NAME.isNotNull),
                concat(" ", AUTHOR.MIDDLE_NAME),
                AUTHOR.MIDDLE_NAME
            ),
            ""
        ),
        if_(
            AUTHOR.NICKNAME.isNotNull,
            if_(
                AUTHOR.FIRST_NAME.isNull.and(AUTHOR.LAST_NAME.isNotNull).and(AUTHOR.MIDDLE_NAME.isNotNull),
                AUTHOR.NICKNAME,
                concat(`val`(" ("), AUTHOR.NICKNAME, `val`(")"))
            ),
            ""
        ),
    )
    return create
        .select(fullname)
        .from(AUTHOR)
        .where(AUTHOR.ID.eq(authorId))
        .fetchSingle { it[fullname] }
}

@Suppress("unused")
fun IAuthor.buildName() = buildString {
    if (lastName != null) append(lastName)
    if (firstName != null) {
        if (lastName != null) append(", ").append(firstName)
        else append(firstName)
    }
    if (middleName != null) {
        if (firstName != null || lastName != null) append(" ").append(middleName)
        else append(middleName)
    }
    if (nickname != null) {
        if (firstName == null && lastName == null && middleName == null) append(nickname)
        else append(" ($nickname)")
    }
}

@Suppress("unused")
fun formatDate(x: LocalDateTime): String = dtf.format(x.z)

@Suppress("unused")
fun formatDate(x: ZonedDateTime): String = dtf.format(x)

@Suppress("unused")
fun formatDate(x: TemporalAccessor): String = dtf.format(x)

fun bookDescriptionsShorter(pathsByIds: List<Pair<Long, IBook>>): Map<Long, String?> {
    return pathsByIds
        .associate { (id, book) ->

            val file = File(book.path)
            val size = file.length().humanReadable()
            val seq = book.sequence
            val seqNo = book.sequenceNumber
            val descr = FictionBook(file).description.titleInfo.annotation?.text ?: ""
            val text = buildString {
                append("Size: $size.\n ")
                seq?.let { append("Series: $it") }
                seqNo?.let { append("#${it.toString().padStart(3, '0')}") }
                seq?.let { append(".\n ") }
                append(descr)
            }
            id to text
        }
}

fun bookDescriptionsLonger(pathsByIds: List<Pair<Long, IBook>>): Map<Long, String?> {
    return pathsByIds
        .associate { (id, book) ->
            val file = File(book.path)
            val size = file.length().humanReadable()
            val seq = book.sequence
            val seqNo = book.sequenceNumber
            val elements = FictionBook(file).description.titleInfo.annotation?.elements
            val descr = elements?.let { Element.getText(elements, "<br>") } ?: ""
            val text = buildString {
                append("<p><b>Size</b>: $size</p>")
                seq?.let { append("<p><b>Series</b>: $it") }
                seqNo?.let { append("#${it.toString().padStart(3, '0')}") }
                append("</p>")
                append(descr)
            }
            id to text
        }
}

private fun latestBooks() =
    getBookWithInfo()
        .orderBy(BOOK.ADDED.desc())
        .limit(20)
        .fetch()

fun Long.humanReadable(): String {
    val absB = if (this == Long.MIN_VALUE) Long.MAX_VALUE else abs(this)
    if (absB < 1024) {
        return "$this B"
    }
    var value = absB
    val ci = StringCharacterIterator("KMGTPE")
    var i = 40
    while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
        value = value shr 10
        ci.next()
        i -= 10
    }
    value *= sign.toLong()
    return String.format("%.1f %ciB", value / 1024.0, ci.current())
}

fun searchBookByText(term: String, page: Int, pageSize: Int = 50): Pair<List<BookWithInfo>, Boolean> {
    val ftsId = BOOKS_FTS.rowid().cast(Long::class.java).`as`("fts_id")
    val ids = create
        .select(ftsId)
        .from(BOOKS_FTS).where(BOOKS_FTS.match(value(term)))
        .orderBy(field("bm25(books_fts)"))
        .limit(pageSize + 1)
        .offset(page * pageSize)
    val infos = getBookWithInfo()
        .innerJoin(ids).on(ftsId.eq(BOOK.ID))
        .fetch { BookWithInfo(it) }
    val hasMore = infos.size > pageSize
    return infos.take(pageSize) to hasMore
}

fun Table<*>.match(text: Typed<String>): Condition {
    return condition("$name MATCH {0}", text)
}

@Suppress("unused")
class BookWithInfo(record: Record4<Long, List<BookPojo>, List<Author>, List<String>>) {
    val book: IBook = record.component2()[0]
    val authors: List<IAuthor> = record.component3()!!
    val genres: List<String> = record.component4().map { genreNames[it] ?: it }
    val id = record.get(BOOK.ID)!!
}