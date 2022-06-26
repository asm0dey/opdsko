@file:Suppress("HttpUrlsUsage")

package io.github.asm0dey.plugins

import com.kursx.parser.fb2.Binary
import com.kursx.parser.fb2.Element
import com.kursx.parser.fb2.FictionBook
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.asm0dey.genreNames
import io.github.asm0dey.model.Entry
import io.github.asm0dey.opdsko.jooq.Tables.*
import io.github.asm0dey.opdsko.jooq.tables.Book
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IAuthor
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IBook
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author
import io.ktor.http.ContentDisposition.Companion.Attachment
import io.ktor.http.ContentDisposition.Parameters.FileName
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders.ContentDisposition
import io.ktor.server.application.*
import io.ktor.server.jte.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
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
import kotlin.math.sign
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author as AuthorPojo
import io.github.asm0dey.opdsko.jooq.tables.pojos.Book as BookPojo

val dtf: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
const val NAVIGATION_TYPE = "application/atom+xml;profile=opds-catalog;kind=navigation"
const val ACQUISITION_TYPE = "application/atom+xml;profile=opds-catalog;kind=acquisition"

fun Application.routes() {
    routing {
        route("/") {
            get {
                call.respondText("Hello World!")
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
                            JteContent(
                                "books.kte",
                                books(
                                    books,
                                    call.request.uri,
                                    "Search results for \"$searchTerm\"",
                                    "search:$searchTerm",
                                    ZonedDateTime.now(),
                                    paginationLinks(hasMore, searchTerm, page)
                                ),
                                contentType = ContentType.parse(ACQUISITION_TYPE)
                            )
                        )
                    }
                }
                get("/new") {
                    val books = latestBooks().map { BookWithInfo(it) }
                    call.respond(
                        JteContent(
                            "books.kte",
                            params = books(books, call.request.path(), "Latest books", "new"),
                            contentType = ContentType.parse(ACQUISITION_TYPE)
                        )
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
                        call.respond(JteContent(
                            template = "navigation.kte",
                            params = mapOf(
                                "feedId" to "authors:start_with:$nameStart",
                                "feedTitle" to if (nameStart.isBlank()) "First character of all authors" else "Authors starting with $nameStart",
                                "feedUpdated" to ZonedDateTime.now(),
                                "path" to path,
                                "entries" to items.map { (name, countOrId) ->
                                    Entry(
                                        title = name,
                                        links = listOf(
                                            Entry.Link(
                                                type = NAVIGATION_TYPE,
                                                rel = "subsection",
                                                href = if (trim) "/opds/author/c/${URLEncoder.encode(name, UTF_8)}"
                                                else "/opds/author/browse/${countOrId}",
                                                count = if (trim) countOrId else null,
                                            )
                                        ),
                                        id = "authors:start_with:$nameStart",
                                        summary = name,
                                        updated = ZonedDateTime.now()
                                    )
                                }
                            ),
                            contentType = ContentType.parse(NAVIGATION_TYPE)
                        ))
                    }
                    route("/browse") {
                        get("/{id}") {
                            val authorId = call.parameters["id"]!!.toLong()
                            val path = call.request.path()
                            val authorName = authorName(authorId)
                            val (inseries, out) = hasSeries(authorId)
                            val latestAuthorUpdate = latestAuthorUpdate(authorId).z
                            call.respond(
                                JteContent(
                                    template = "navigation.kte",
                                    params = mapOf(
                                        "feedId" to "author:$authorId",
                                        "feedTitle" to authorName,
                                        "feedUpdated" to latestAuthorUpdate,
                                        "path" to path,
                                        "entries" to listOfNotNull(
                                            if (inseries > 0) Entry(
                                                title = "By series",
                                                links = listOf(
                                                    Entry.Link(
                                                        type = NAVIGATION_TYPE,
                                                        rel = "subsection",
                                                        href = "/opds/author/browse/$authorId/series",
                                                    )
                                                ),
                                                id = "author:$authorId:series",
                                                summary = "All books by $authorName by series",
                                                updated = ZonedDateTime.now(),
                                            ) else null,
                                            if (out > 0 && inseries > 0) Entry(
                                                title = "Out of series",
                                                links = listOf(
                                                    Entry.Link(
                                                        type = NAVIGATION_TYPE,
                                                        rel = "subsection",
                                                        href = "/opds/author/browse/$authorId/out",
                                                    )
                                                ),
                                                id = "author:$authorId:out",
                                                summary = "All books by $authorName",
                                                updated = ZonedDateTime.now(),
                                            ) else null,
                                            Entry(
                                                title = "All books",
                                                links = listOf(
                                                    Entry.Link(
                                                        type = NAVIGATION_TYPE,
                                                        rel = "subsection",
                                                        href = "/opds/author/browse/$authorId/all",
                                                    )
                                                ),
                                                id = "author:$authorId:all",
                                                summary = "All books by $authorName",
                                                updated = latestAuthorUpdate,
                                            ),
                                        )
                                    ),
                                    contentType = ContentType.parse(NAVIGATION_TYPE)
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
                                JteContent(
                                    "books.kte", books(books, path, "All books by $authorName", "author:$authorId:all"),
                                    contentType = ContentType.parse(ACQUISITION_TYPE)
                                )
                            )
                        }
                        get("/{id}/series") {
                            val authorId = call.parameters["id"]!!.toLong()
                            val path = call.request.path()
                            val namesWithDates = seriesByAuthorId(authorId)
                            val authorName = authorName(authorId)
                            call.respond(JteContent(
                                template = "navigation.kte",
                                params = mapOf(
                                    "feedId" to "author:$authorId:series",
                                    "feedTitle" to "Series of $authorName",
                                    "feedUpdated" to namesWithDates.values.maxOf { it.first }.z,
                                    "path" to path,
                                    "entries" to namesWithDates.map { (name, dateAndCount) ->
                                        Entry(
                                            name,
                                            listOf(
                                                Entry.Link(
                                                    rel = "subsection",
                                                    href = "/opds/author/browse/$authorId/series/${
                                                        URLEncoder.encode(
                                                            name,
                                                            UTF_8
                                                        )
                                                    }",
                                                    type = ACQUISITION_TYPE,
                                                    count = dateAndCount.second.toLong()
                                                )
                                            ),
                                            "author:$authorId:series:$name",
                                            name,
                                            dateAndCount.first.z
                                        )
                                    }
                                ),
                                contentType = ContentType.parse(NAVIGATION_TYPE)
                            ))
                        }
                        get("/{id}/series/{name}") {
                            val authorId = call.parameters["id"]!!.toLong()
                            val seriesName = URLDecoder.decode(call.parameters["name"]!!, UTF_8)
                            val result = booksBySeriesAndAuthor(seriesName, authorId)
                            val path = call.request.path()
                            call.respond(
                                JteContent(
                                    template = "books.kte",
                                    params = books(result, path, seriesName, "author:$authorId:series:$seriesName"),
                                    contentType = ContentType.parse(ACQUISITION_TYPE)
                                )
                            )
                        }
                        get("/{id}/out") {
                            val authorId = call.parameters["id"]!!.toLong()
                            val path = call.request.path()
                            val books = booksWithoutSeriesByAuthorId(authorId)
                            val authorName = authorName(authorId)
                            call.respond(
                                JteContent(
                                    template = "books.kte",
                                    params = books(
                                        books,
                                        path,
                                        "Books without series by $authorName",
                                        "author:$authorId:series:out"
                                    ),
                                    contentType = ContentType.parse(ACQUISITION_TYPE)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun paginationLinks(
    hasMore: Boolean,
    searchTerm: String?,
    page: Int,
) = listOfNotNull(
    if (hasMore) {
        Entry.Link(
            rel = "next",
            "/opds/search/${URLEncoder.encode(searchTerm, UTF_8)}?page=${page + 1}",
            type = ACQUISITION_TYPE
        )
    } else null,
    if (page > 0) {
        Entry.Link(
            rel = "previous",
            "/opds/search/${URLEncoder.encode(searchTerm, UTF_8)}?page=${page - 1}",
            type = ACQUISITION_TYPE
        )
    } else null,
    if (page > 0) {
        Entry.Link(
            rel = "first",
            type = ACQUISITION_TYPE,
            href = "/opds/search/${URLEncoder.encode(searchTerm, UTF_8)}"
        )
    } else null,
)

private fun books(
    books: List<BookWithInfo>,
    path: String,
    title: String,
    id: String,
    updated: ZonedDateTime = books.maxOf { it.book.added }.z,
    additionalLinks: List<Entry.Link> = listOf(),
) = mapOf(
    "books" to books,
    "bookDescriptions" to bookDescriptionsShorter(books.map { it.id to it.book }),
    "imageTypes" to imageTypes(books.map { it.id to it.book.path }),
    "path" to path,
    "title" to title,
    "feedId" to id,
    "feedUpdated" to updated,
    "additionalLinks" to additionalLinks
)

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

private fun authorNameStarts(prefix: String, trim: Boolean = true): MutableList<Pair<String, Long>> {
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

@Suppress("SpellCheckingInspection")
const val OPDSKO_JDBC = "jdbc:sqlite:opds.db"

var ds = run {
    val config = HikariConfig()
    config.poolName = "opdsko pool"
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
    val ids = create
        .select(BOOKS_FTS.rowid().cast(Long::class.java))
        .from(BOOKS_FTS).where(BOOKS_FTS.match(value(term)))
        .orderBy(field("bm25(books_fts)"))
        .limit(pageSize + 1)
        .offset(page * pageSize)
    val infos = getBookWithInfo()
        .where(BOOK.ID.`in`(ids))
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