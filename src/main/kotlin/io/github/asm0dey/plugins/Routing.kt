package io.github.asm0dey.plugins

import com.kursx.parser.fb2.Binary
import com.kursx.parser.fb2.FictionBook
import io.github.asm0dey.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import opdsko.db.Author
import opdsko.db.Book
import org.apache.commons.codec.binary.Base64
import java.io.File
import java.net.URLEncoder
import java.nio.charset.Charset
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import opdsko.db.Author as SAuthor

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/opds") {
            call.respond(rootFeed(call.request.path()))
        }

        get("/opds/new") {
            call.respond(newFeed(call.request.path()))
        }
        get("/opds/image/{id}") {
            val (binary, data) = imageByBookId(call.parameters["id"]!!.toLong())
            call.respondBytes(data, ContentType.parse(binary.contentType))
        }
        get("/opds/book/download/{id}") {
            val bookId = call.parameters["id"]!!.toLong()
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "$bookId.fb2")
                    .toString()
            )
            call.respondBytes(File(bookPath(bookId)).readBytes(), ContentType.parse("application/fb2")) {
            }
        }
        get("/opds/author/browse/{id}") {
            val authorId = call.parameters["id"]!!.toLong()
            val path = call.request.path()
            val authorName = authorName(authorId)
            call.respond(authorRootFeed(path, authorId, authorName))
        }
        get("/opds/author/browse/{id}/all") {
            val authorId = call.parameters["id"]!!.toLong()
            val path = call.request.path()
            call.respond(allAuthorBooks(authorId, path))
        }
        get("/opds/author/browse/{id}/series") {
            val authorId = call.parameters["id"]!!.toLong()
            val path = call.request.path()
            call.respond(allSeries(authorId, path))
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}

fun allSeries(authorId: Long, path: String): NavFeed {
    val seriesNames = db.authorQueries.seriesNames(authorId).executeAsList()
    return NavFeed(
        path = path,
        lastUpdate = db.authorQueries.allSeriesLastUpdate(authorId).executeAsOne().toDate(),
        entries = seriesNames.map {
            val encodedSeriesName = URLEncoder.encode(it, Charset.defaultCharset())
            NavEntry(
                name = it,
                link = NavLink(
                    "subsection",
                    "/opds/author/browse/$authorId/series/$encodedSeriesName",
                    ACQUISITION_TYPE
                ),
                id = "author:$authorId:series:$it",
            )
        },
        title = "All series by ${authorName(authorId)}",
        id="author:$authorId:series",
        feedType = NAVIGATION_TYPE
    )
}

fun allAuthorBooks(authorId: Long, path: String): NavFeed {
    val books = db.bookQueries.allByAuthorId(authorId).executeAsList()
    val authorName = authorName(authorId)
    return bookFeed(books.associateBy { it.id }, path, "All books by $authorName", "author:$authorId:all")
}

private fun authorRootFeed(path: String, authorId: Long, authorName: String): NavFeed {
    val hasSeries = db.authorQueries.hasSeries(authorId).executeAsOneOrNull() != null
    return NavFeed(
        path = path,
        lastUpdate = latestAuthorUpdate(authorId),
        entries = listOfNotNull(
            if (hasSeries) NavEntry(
                "By series",
                NavLink("subsection", "/opds/author/browse/$authorId/series", NAVIGATION_TYPE),
                "author:$authorId:series",
                "All books by $authorName by series"
            ) else null,
            if (hasSeries) NavEntry(
                "Out of series",
                NavLink("subsection", "/opds/author/browse/$authorId/out", NAVIGATION_TYPE),
                "author:$authorId:out",
                "All books by $authorName"
            ) else null,
            NavEntry(
                "All books",
                NavLink("subsection", "/opds/author/browse/$authorId/all", NAVIGATION_TYPE),
                "author:$authorId:all",
                "All books by $authorName"
            ),
        ),
        title = authorName,
        id = "author:$authorId",
        feedType = NAVIGATION_TYPE
    )
}

private fun latestAuthorUpdate(authorId: Long) =
    db.authorQueries.latestUpdate(authorId).executeAsOneOrNull()?.toDate() ?: Date()

private fun authorName(authorId: Long) =
    db.authorQueries.byId(authorId).executeAsOne().buildName()

private fun bookPath(bookId: Long): String {
    return db.bookQueries.getPathById(bookId).executeAsOne()
}

private fun imageByBookId(bookId: Long): Pair<Binary, ByteArray> {
    val path = db.bookQueries.getPathById(bookId).executeAsOne()
    val fb = FictionBook(File(path))
    val id = fb.description.titleInfo.coverPage.first().value.replace("#", "")
    val binary = fb.binaries[id]!!
    val data = Base64().decode(binary.binary)
    return Pair(binary, data)
}

private fun rootFeed(path: String) =
    NavFeed(
        path,
        Timestamp.valueOf(lastestUpdate()),
        listOf(
            NavEntry(
                "New books",
                NavLink(
                    "http://opds-spec.org/sort/new",
                    "/opds/new",
                    ACQUISITION_TYPE
                ),
                "new",
                "Recent publications from this catalog"
            )
        ),
        "Asm0dey's books",
        "root",
        NAVIGATION_TYPE
    )

private fun lastestUpdate() = db.bookQueries.lastUpdate().executeAsOne()

private fun newFeed(path: String): NavFeed {
    return bookFeed(latestBooks(), path, "Latest books", "latest")
}

private fun bookFeed(
    books: Map<Long, Book>,
    path: String,
    title: String,
    feedId: String,
): NavFeed {
    val genres = bookGenres(books.keys)
    val authors = bookAuthors(books.keys)
    val descriptions = bookDescriptions(books.keys)
    val imageTypes = imageTypes(books.keys)

    return NavFeed(
        path = path,
        lastUpdate = Timestamp.valueOf(books.values.maxOf { it.added }),
        entries = books.keys.map {
            toBookEntry(
                bookId = it,
                book = books[it]!!,
                authors = authors[it]!!,
                genres = genres[it],
                genre = imageTypes[it],
                description = descriptions[it]
            )
        },
        title = title,
        id = feedId,
        feedType = ACQUISITION_TYPE
    )
}

fun imageTypes(keys: Collection<Long>): Map<Long, String?> {
    return db.bookQueries.pathsByIds(keys).executeAsList()
        .associate {
            val fb = FictionBook(File(it.path))
            val type = fb.description.titleInfo.coverPage.firstOrNull()?.value?.replace("#", "")?.let {
                fb.binaries[it]?.contentType
            }
            it.id to type
        }

}

private fun toBookEntry(
    bookId: Long,
    book: Book,
    authors: List<Author>,
    genres: List<String>?,
    genre: String?,
    description: String?,
): BookEntry {
    return BookEntry(
        title = book.name,
        id = "book:$bookId",
        updated = book.added.toDate(),
        author = toAuthorEntries(authors),
        genres = genres,
        imageLink = genre?.let { NavLink("http://opds-spec.org/image", "/opds/image/$bookId", it) },
        downloadLink = NavLink(
            "http://opds-spec.org/acquisition", "/opds/book/download/$bookId", "application/fb2"
        ),
        description = description,
        lang = book.lang,
        issued = book.date
    )
}

private fun toAuthorEntries(authors: List<Author>) =
    authors
        .map { EntryAuthor(it.buildName(), "/opds/author/browse/${it.id}") }

private fun SAuthor.buildName() = buildString {
    if (last_name != null) append(last_name)
    if (first_name != null) {
        if (last_name != null) append(", ").append(first_name)
        else append(first_name)
    }
    if (middle_name != null) {
        if (first_name != null || last_name != null) append(" ").append(middle_name)
        else append(middle_name)
    }
    if (nickname != null) {
        if (first_name != null || last_name != null || middle_name != null) append(
            " ($nickname)"
        )
        else append(nickname)
    }
}

fun bookDescriptions(keys: Collection<Long>): Map<Long, String?> {
    return db.bookQueries.pathsByIds(keys) { id, path ->
        id to FictionBook(File(path)).description.titleInfo.annotation?.text
    }.executeAsList().toMap()
}

private fun bookAuthors(books: Collection<Long>) =
    db.book_authorQueries.booksAuthors(books) { book_id, id, fb2id, first_name, middle_name, last_name, nickname, added ->
        book_id to SAuthor(id, fb2id, first_name, middle_name, last_name, nickname, added)
    }
        .executeAsList()
        .groupBy { it.first }
        .mapValues { it.value.map { it.second } }

private fun bookGenres(books: Collection<Long>) = db.book_genreQueries.booksGenres(books)
    .executeAsList()
    .map { it.copy(name = genres[it.name] ?: it.name) }
    .groupBy { it.book_id }
    .mapValues { it.value.map { it.name } }

private fun latestBooks() = db.bookQueries.lastN(20).executeAsList().associateBy { it.id }

fun LocalDateTime.toDate() = Timestamp.valueOf(this)
