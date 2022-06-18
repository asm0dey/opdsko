package io.github.asm0dey

import com.kursx.parser.fb2.FictionBook
import io.github.asm0dey.opdsko.jooq.Tables.*
import io.github.asm0dey.opdsko.jooq.tables.daos.BookAuthorDao
import io.github.asm0dey.opdsko.jooq.tables.daos.BookGenreDao
import io.github.asm0dey.opdsko.jooq.tables.daos.GenreDao
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author
import io.github.asm0dey.opdsko.jooq.tables.pojos.Book
import io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor
import io.github.asm0dey.opdsko.jooq.tables.pojos.BookGenre
import io.github.asm0dey.opdsko.jooq.tables.records.AuthorRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookRecord
import io.github.asm0dey.plugins.OPDSKO_JDBC
import io.github.asm0dey.plugins.create
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.flywaydb.core.Flyway
import org.jooq.impl.DSL.*
import org.tinylog.kotlin.Logger
import java.io.File
import java.time.LocalDateTime
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.StartElement
import kotlin.concurrent.thread

val genreNames = genreNames()

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.main() {
    val dir = environment.config.propertyOrNull("ktor.indexer.path")?.getString()
    initDb()
    if (dir != null) {
        thread(start = true, isDaemon = true, name = "Scanner", priority = 1) {
            scan(dir)
        }
    }
}

private fun initDb() {
    Flyway.configure().dataSource(OPDSKO_JDBC, null, null).load().migrate()
}

private fun genreNames(): HashMap<String, String> {
    fun StartElement.getAttributeValue(attribute: String) = getAttributeByName(QName.valueOf(attribute)).value

    NavFeed.BookEntry::class.java.classLoader.getResourceAsStream("genres.xml")?.buffered().use {
        val reader = XMLInputFactory.newDefaultFactory().createXMLEventReader(it)
        var currentGenre = ""
        val data = hashMapOf<String, String>()
        while (reader.hasNext()) {
            val nextEvent = reader.nextEvent()
            when {
                nextEvent.isStartElement -> {
                    val startElement = nextEvent.asStartElement()
                    when (startElement.name.localPart) {
                        "genre" -> currentGenre = startElement.getAttributeValue("value")
                        "root-descr" ->
                            if (startElement.getAttributeValue("lang") == "en")
                                data[currentGenre] = startElement.getAttributeValue("genre-title")

                        "subgenre" -> currentGenre = startElement.getAttributeValue("value")
                        "genre-descr" ->
                            if (startElement.getAttributeValue("lang") == "en")
                                data[currentGenre] = startElement.getAttributeValue("title")

                        "genre-alt" ->
                            data[startElement.getAttributeValue("value")] = data[currentGenre]!!
                    }
                }

                nextEvent.isEndElement ->
                    if (nextEvent.asEndElement().name.localPart == "genre")
                        currentGenre = ""
            }
        }
        return data
    }
}

private fun scan(libraryRoot: String) {
    File(libraryRoot).walkTopDown().filter { it.name.endsWith(".fb2") }.forEach { file ->
        val bookPath = file.absoluteFile.canonicalPath
        Logger.info { "Processing file $bookPath" }
        if (file.length() < 20) return@forEach
        val fb = try {
            FictionBook(file)
        } catch (e: Exception) {
            return@forEach
        }
        create.transaction { txConfig ->
            if (fb.description.titleInfo.bookTitle == null) {
                Logger.error(IllegalStateException("No book title in $bookPath"))
                return@transaction
            }
            val transaction = using(txConfig)
            val existingBookId = transaction.select(BOOK.ID)
                .from(BOOK)
                .where(
                    BOOK.NAME.eq(fb.description.titleInfo.bookTitle),
                    BOOK.PATH.eq(bookPath)
                )
                .limit(1)
                .fetchOne { it[BOOK.ID] }
            if (existingBookId != null)
                return@transaction
            transaction.delete(BOOK).where(BOOK.PATH.eq(bookPath)).execute()
            val bookId = transaction
                .insertInto(BOOK)
                .set(
                    BookRecord(
                        Book(
                            null,
                            bookPath,
                            fb.description.titleInfo.bookTitle,
                            fb.description.titleInfo.date,
                            LocalDateTime.now(),
                            fb.description.titleInfo.sequence?.name,
                            fb.description.titleInfo.sequence?.number?.toIntOrNull(),
                            fb.description.titleInfo.lang
                        )
                    )
                )
                .returning(BOOK.ID)
                .fetchOne { it.id }!!
            val authorIds = fb.description.titleInfo.authors.map {
                transaction.select(AUTHOR.ID)
                    .from(AUTHOR)
                    .where(
                        if (it.middleName == null) AUTHOR.MIDDLE_NAME.isNull
                        else trim(lower(AUTHOR.MIDDLE_NAME)).eq(trim(lower(it.middleName!!))),
                        if (it.lastName == null) AUTHOR.LAST_NAME.isNull
                        else trim(lower(AUTHOR.LAST_NAME)).eq(trim(lower(it.lastName!!))),
                        if (it.firstName == null) AUTHOR.FIRST_NAME.isNull
                        else trim(lower(AUTHOR.FIRST_NAME)).eq(trim(lower(it.firstName!!))),
                        if (it.nickname == null) AUTHOR.NICKNAME.isNull
                        else trim(lower(AUTHOR.NICKNAME)).eq(trim(lower(it.nickname!!))),
                    )
                    .fetchOne { it[AUTHOR.ID] }
                    ?: transaction.insertInto(AUTHOR)
                        .set(
                            AuthorRecord(
                                Author(
                                    null,
                                    it.id,
                                    it.firstName?.trim(),
                                    it.middleName?.trim(),
                                    it.lastName?.trim(),
                                    it.nickname?.trim() ,
                                    LocalDateTime.now()
                                )
                            )
                        )
                        .returning(AUTHOR.ID)
                        .fetchOne { it.id }!!
            }.toSet()
            val genreIds = fb.description.titleInfo.genres.map {
                GenreDao(txConfig).fetchByName(it).firstOrNull()?.id
                    ?: transaction.insertInto(GENRE, GENRE.NAME).values(it).returning().fetchOne()!!.id
            }.toSet()
            BookAuthorDao(txConfig).insert(authorIds.map { BookAuthor(bookId, it) })
            BookGenreDao(txConfig).insert(genreIds.map { BookGenre(bookId, it) })
        }
    }
}

