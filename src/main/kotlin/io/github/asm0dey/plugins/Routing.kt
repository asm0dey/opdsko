package io.github.asm0dey.plugins

import com.kursx.parser.fb2.Binary
import com.kursx.parser.fb2.FictionBook
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.asm0dey.*
import io.github.asm0dey.opdsko.jooq.Tables.*
import io.github.asm0dey.opdsko.jooq.tables.Book
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IAuthor
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author
import io.github.asm0dey.opdsko.jooq.tables.records.BookRecord
import io.ktor.http.ContentDisposition.Companion.Attachment
import io.ktor.http.ContentDisposition.Parameters.FileName
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders.ContentDisposition
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.future.await
import org.apache.commons.codec.binary.Base64
import org.jooq.*
import org.jooq.conf.Settings
import org.jooq.impl.DSL.*
import org.jooq.impl.DefaultExecuteListener
import org.jooq.impl.DefaultExecuteListenerProvider
import org.tinylog.kotlin.Logger
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.StringCharacterIterator
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.CompletionStage
import kotlin.math.abs
import kotlin.math.sign
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author as AuthorPojo
import io.github.asm0dey.opdsko.jooq.tables.pojos.Book as BookPojo


fun Application.routes() {
    routing {
        route("/") {
            get {
                call.respondText("Hello World!")
            }
            route("/opds") {
                get {
                    call.respond(rootFeed(call.request.path()))
                }
                get("/search") {
                    val parameters = call.request.queryParameters
                    val author = parameters["author"]
                    val title = parameters["title"]
                    val searchTerm = parameters["q"]
                }
                get("/new") {
                    call.respond(newFeed(call.request.path()))
                }
                get("/image/{id}") {
                    val (binary, data) = imageByBookId(call.parameters["id"]!!.toLong())
                    call.respondBytes(data, ContentType.parse(binary.contentType))
                }
                route("/book/download/{id}") {
                    get {
                        val bookId = call.parameters["id"]!!.toLong()
                        call.response.header(
                            ContentDisposition,
                            Attachment.withParameter(FileName, "$bookId.fb2").toString()
                        )
                        call.respondBytes(
                            File(bookPath(bookId).await().single().value1()).readBytes(),
                            ContentType.parse("application/fb2")
                        )
                    }
                }
                route("/author") {
                    get("/c/{name?}") {
                        val path = call.request.path()
                        val nameStart = URLDecoder.decode(call.parameters["name"] ?: "", Charset.defaultCharset())
                        val trim = nameStart.length < 5
                        val items = authorNameStarts(nameStart, trim).await().map { it.component1() to it.component2() }
                        call.respond(authorCatalogue(nameStart, path, items, trim))
                    }
                    route("/browse") {
                        get("/{id}") {
                            val authorId = call.parameters["id"]!!.toLong()
                            val path = call.request.path()
                            val authorName = authorName(authorId)
                            call.respond(authorRootFeed(path, authorId, authorName))
                        }
                        get("/{id}/all") {
                            val authorId = call.parameters["id"]!!.toLong()
                            val path = call.request.path()
                            call.respond(allAuthorBooks(authorId, path))
                        }
                        get("/{id}/series") {
                            val authorId = call.parameters["id"]!!.toLong()
                            val path = call.request.path()
                            call.respond(allSeries(authorId, path))
                        }
                        get("/{id}/series/{name}") {
                            val authorId = call.parameters["id"]!!.toLong()
                            val seriesName = call.parameters["name"]!!
                            val path = call.request.path()
                            call.respond(series(authorId, seriesName, path))
                        }
                        get("/{id}/out") {
                            val authorId = call.parameters["id"]!!.toLong()
                            val path = call.request.path()
                            call.respond(booksWithoutSeries(authorId, path))
                        }
                    }
                }
            }
        }

        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}

private suspend fun booksWithoutSeries(authorId: Long, path: String): NavFeed {
    val books = getBookWithInfo()
        .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
        .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNull)
        .orderBy(BOOK.NAME)
        .fetchAsync()
    val authorName = authorName(authorId).await().single().component1()
    return bookFeed(books, path, "All books by $authorName without series", "author:$authorId:out")
}

private fun authorCatalogue(
    nameStart: String,
    path: String,
    items: List<Pair<String, Long>>,
    trim: Boolean,
) = NavFeed(
    id = "authors:start_with:$nameStart",
    title = if (nameStart.isBlank()) "First character of all authors" else "Authors starting with $nameStart",
    updated = dtf.format(now().z),
    author = feedAuthor,
    links = listOf(
        selfLink(path, NAVIGATION_TYPE),
        startLink(),
    ),
    entries = items.map { item ->
        NavFeed.NavEntry(
            name = item.first,
            link = NavFeed.NavLink(
                type = NAVIGATION_TYPE,
                rel = "subsection",
                href = if (trim) "/opds/author/c/${URLEncoder.encode(item.first, Charset.defaultCharset())}"
                else "/opds/author/browse/${item.second}"
            ),
            id = "authors:start_with:${item.first}",
            description = if (trim) "${item.second} items" else item.first,
            updated = dtf.format(now().z)
        )
    }
)

private val LocalDateTime.z: ZonedDateTime
    get() = ZonedDateTime.of(this, ZoneId.systemDefault())

suspend fun series(authorId: Long, seriesName: String, path: String): NavFeed {
    val result = getBookWithInfo()
        .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
        .where(BOOK.SEQUENCE.eq(seriesName))
        .orderBy(BOOK.SEQUENCE_NUMBER.asc().nullsLast(), BOOK.NAME)
        .fetchAsync()
    return bookFeed(result, path, seriesName, "author:$authorId:series:$seriesName")
}

private fun authorNameStarts(prefix: String, trim: Boolean = true): CompletionStage<Result<Record2<String, Long>>> {
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
        .fetchAsync()
}

const val OPDSKO_JDBC = "jdbc:sqlite:opds.db"

var ds = run {
    val config = HikariConfig()
    config.poolName = "opdsko pool"
    config.driverClassName = "org.sqlite.JDBC"
    config.jdbcUrl = OPDSKO_JDBC
    config.connectionTestQuery = "SELECT 1"
    config.maxLifetime = 60000 // 60 Sec
    config.idleTimeout = 45000 // 45 Sec
    config.maximumPoolSize = 3 // 50 Connections (including idle connections)
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
                Logger.tag("JOOQ").info(create.renderInlined(ctx.query()));
            }
        }
    }))
}

suspend fun allSeries(authorId: Long, path: String): NavFeed {
    val namesWithDates = create.select(BOOK.SEQUENCE, BOOK.ADDED)
        .from(BOOK)
        .innerJoin(BOOK_AUTHOR).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
        .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNotNull)
        .orderBy(BOOK.ADDED.desc())
        .fetchGroups({ it[BOOK.SEQUENCE] }, { it[BOOK.ADDED] })
        .mapValues { it.value.max() }
    return NavFeed(
        "author:$authorId:series",
        "All series by ${authorName(authorId).await().single().component1()}",
        dtf.format(namesWithDates.values.max().z),
        feedAuthor,
        listOf(
            startLink(),
            selfLink(path, NAVIGATION_TYPE)
        ),
        namesWithDates.keys.map {
            val encodedSeriesName = it
            NavFeed.NavEntry(
                name = it,
                link = NavFeed.NavLink(
                    rel = "subsection",
                    href = "/opds/author/browse/$authorId/series/$encodedSeriesName",
                    type = ACQUISITION_TYPE
                ),
                id = "author:$authorId:series:$it",
                description = null,
                updated = dtf.format(namesWithDates[it]!!.z)
            )
        }
    )
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

private fun getBookWithInfo() = create
    .selectDistinct(
        BOOK.ID,
        bookById,
        bookAuthors,
        bookGenres,
    )
    .from(BOOK)

suspend fun allAuthorBooks(authorId: Long, path: String): NavFeed {
    val books = getBookWithInfo()
        .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
        .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
        .fetchAsync()
    val authorName = authorName(authorId).await().single().component1()
    return bookFeed(books, path, "All books by $authorName", "author:$authorId:all")
}


private suspend fun authorRootFeed(
    path: String,
    authorId: Long,
    authorNameProvider: CompletionStage<Result<Record1<String>>>,
): NavFeed {
    val (inseries, out) = hasSeries(authorId).await().single()
    val authorName = authorNameProvider.await().single().value1()
    return NavFeed(
        id = "author:$authorId",
        title = authorName,
        updated = dtf.format((latestAuthorUpdate(authorId).await().firstOrNull()?.value1() ?: now()).z),
        author = feedAuthor,
        links = listOf(
            startLink(),
            selfLink(path, NAVIGATION_TYPE)
        ),
        entries = listOfNotNull(
            if (inseries > 0) {
                NavFeed.NavEntry(
                    name = "By series",
                    link = NavFeed.NavLink(
                        rel = "subsection",
                        href = "/opds/author/browse/$authorId/series",
                        type = NAVIGATION_TYPE
                    ),
                    id = "author:$authorId:series",
                    description = "All books by $authorName by series",
                    updated = dtf.format(now().z),
                )
            } else null,
            if (out > 0 && inseries > 0) {
                NavFeed.NavEntry(
                    name = "Out of series",
                    link = NavFeed.NavLink(
                        rel = "subsection",
                        href = "/opds/author/browse/$authorId/out",
                        type = NAVIGATION_TYPE
                    ),
                    id = "author:$authorId:out",
                    description = "All books by $authorName",
                    updated = dtf.format(now().z),
                )
            } else null,
            NavFeed.NavEntry(
                name = "All books",
                link = NavFeed.NavLink(
                    rel = "subsection",
                    href = "/opds/author/browse/$authorId/all",
                    type = NAVIGATION_TYPE
                ),
                id = "author:$authorId:all",
                description = "All books by $authorName",
                updated = dtf.format(now().z),
            ),
        )
    )
}

private fun hasSeries(authorId: Long): CompletionStage<Result<Record2<Int, Int>>> {
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
        .fetchAsync()
}

private fun latestAuthorUpdate(authorId: Long): CompletionStage<Result<Record1<LocalDateTime>>> {
    return create
        .select(BOOK.ADDED)
        .from(BOOK)
        .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
        .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
        .orderBy(BOOK.ADDED.desc())
        .limit(1)
        .fetchAsync()
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

private fun rootFeed(path: String): NavFeed {
    return NavFeed(
        id = "root",
        title = "Asm0dey's books",
        updated = dtf.format(latestUpdate()!!.z),
        author = feedAuthor,
        links = listOf(
            startLink(),
            selfLink(path, NAVIGATION_TYPE)
        ),
        entries = listOf(
            NavFeed.NavEntry(
                name = "New books",
                link = NavFeed.NavLink(
                    rel = "http://opds-spec.org/sort/new",
                    href = "/opds/new",
                    type = ACQUISITION_TYPE
                ),
                id = "new",
                description = "Recent publications from this catalog",
                updated = dtf.format(now().z)
            ),
            NavFeed.NavEntry(
                name = "Books by authors",
                link = NavFeed.NavLink(
                    rel = "category",
                    href = "/opds/author/c/",
                    type = NAVIGATION_TYPE
                ),
                id = "authors",
                description = "Authors categorized",
                updated = dtf.format(now().z)
            ),
        )
    )
}

private fun latestUpdate() = create
    .select(BOOK.ADDED)
    .from(BOOK)
    .orderBy(BOOK.ADDED.desc())
    .limit(1)
    .fetchOne(BOOK.ADDED)

private suspend fun newFeed(path: String): NavFeed {
    return bookFeed(latestBooks(), path, "Latest books", "latest")
}

typealias BookWithPathAuthorsAndGenres = CompletionStage<Result<Record4<Long, List<BookPojo>, List<Author>, List<String>>>>

private suspend fun bookFeed(
    bookRecord: BookWithPathAuthorsAndGenres,
    path: String,
    title: String,
    feedId: String,
): NavFeed {
    val books = bookRecord
        .await()
        .map { record ->
            object {
                val id = record[BOOK.ID]
                val book = record[bookById].single()
                val authors = record[bookAuthors]
                val genres = record[bookGenres]?.map { genreNames[it] ?: it } ?: emptyList()
            }
        }
    val descriptions = bookDescriptions(books.map { it.id to it.book.path })
    val imageTypes = imageTypes(books.map { it.id to it.book.path })
    return NavFeed(
        id = feedId,
        title = title,
        updated = dtf.format(books.maxOf { it.book.added }.z),
        author = feedAuthor,
        links = listOf(
            startLink(),
            selfLink(path, ACQUISITION_TYPE)
        ),
        entries = books.map { bookRaw ->
            val book = bookRaw.book
            NavFeed.BookEntry(
                title = book.name,
                id = "book:${book.id}",
                author = bookRaw.authors.toAuthorEntries(),
                published = dtf.format(book.added.z),
                lang = book.lang,
                date = book.date,
                summary = descriptions[bookRaw.id]?.let { it.substring(0 until kotlin.math.min(150, it.length)) + "…" },
                genres = bookRaw.genres.map(NavFeed.BookEntry::XCategory),
                links = listOfNotNull(
                    imageTypes[bookRaw.id]?.let {
                        imageLink("/opds/image/${bookRaw.id}", it)
                    },
                    downloadLink("/opds/book/download/${bookRaw.id}", "application/fb2")
                ),
                content = buildString {
                    book.sequence?.let { series ->
                        val num = book.sequenceNumber?.toString()?.padStart(3, '0') ?: ""
                        append("Series: $series. $num\n")
                    }
                    append("Size: ${File(book.path).length().humanReadable()}\n")
                    descriptions[bookRaw.id]?.let { append("$it\n\n\n") }
                }.split("\n")
            )
        }
    )
}

private val feedAuthor = listOf(NavFeed.XAuthor("Pasha Finkelshteyn", "https://github.com/asm0dey/opdsKo"))

private fun downloadLink(downloadLink: String, fb2Type: String) = NavFeed.NavLink(
    rel = "http://opds-spec.org/acquisition",
    href = downloadLink,
    type = fb2Type
)

private fun imageLink(href: String, type: String) = NavFeed.NavLink(
    rel = "http://opds-spec.org/image",
    href = href,
    type = type
)

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

private fun List<Author>.toAuthorEntries() = map { NavFeed.XAuthor(it.buildName(), "/opds/author/browse/${it.id}") }

private fun authorName(authorId: Long) = create
    .select(
        concat(
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
    )
    .from(AUTHOR)
    .where(AUTHOR.ID.eq(authorId))
    .fetchAsync()

private fun IAuthor.buildName() = buildString {
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

fun bookDescriptions(pathsByIds: List<Pair<Long, String>>): Map<Long, String?> {
    return pathsByIds
        .associate { (id, path) ->
            id to FictionBook(File(path)).description.titleInfo.annotation?.text
        }
}

private suspend fun latestBooks() =
    getBookWithInfo()
        .orderBy(BOOK.ADDED.desc())
        .limit(20)
        .fetchAsync()

fun Long.humanReadable(): String {
    val absB = if (this == Long.MIN_VALUE) Long.MAX_VALUE else abs(this)
    if (absB < 1024) {
        return "${this} B"
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
