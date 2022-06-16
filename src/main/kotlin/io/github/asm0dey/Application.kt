package io.github.asm0dey

import com.kursx.parser.fb2.FictionBook
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.logs.LogSqliteDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.github.asm0dey.plugins.configureHTTP
import io.github.asm0dey.plugins.configureMonitoring
import io.github.asm0dey.plugins.configureRouting
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import opdsko.db.Book_author
import opdsko.db.Book_genre
import opdsko.db.OpdsDb
import org.tinylog.kotlin.Logger
import java.io.File
import java.time.LocalDateTime
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.StartElement
import kotlin.concurrent.thread

val genres = genreNames()
const val file = "opds.db"
const val CURRENT_SCHEMA_VER = 0

val driver = JdbcSqliteDriver("jdbc:sqlite:$file")
val db = OpdsDb(
    driver = if (System.getenv().containsKey("OPDSKO_LOGGING"))
        LogSqliteDriver(driver, Logger.tag("SQL")::debug) else driver,
    bookAdapter = opdsko.db.Book.Adapter(
        addedAdapter = LocalDateTimeStringColumnAdapter
    ),
    authorAdapter = opdsko.db.Author.Adapter(
        addedAdapter = LocalDateTimeStringColumnAdapter
    )
)

object LocalDateTimeStringColumnAdapter : ColumnAdapter<LocalDateTime, String> {
    override fun decode(databaseValue: String): LocalDateTime {
        return LocalDateTime.parse(databaseValue)
    }

    override fun encode(value: LocalDateTime): String {
        return value.toString()
    }
}

fun main(vararg args: String) {
    initDb()
    if (args.size == 1) {
        val file = args[0]
        thread(start = true, isDaemon = true, name = "Scanner") {
            scan(file)
        }
    }
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureHTTP()
        configureMonitoring()
    }.start(wait = true)
}

private fun initDb() {
    if (!File(file).exists()) OpdsDb.Schema.create(driver)
    val dbVer = db.versionQueries.currentVersion().executeAsOne()
    if (dbVer < CURRENT_SCHEMA_VER)
        OpdsDb.Schema.migrate(driver, dbVer.toInt(), CURRENT_SCHEMA_VER)
}

private fun genreNames(): HashMap<String, String> {
    fun StartElement.getAttributeValue(attribute: String) = getAttributeByName(QName.valueOf(attribute)).value

    BookEntry::class.java.classLoader.getResourceAsStream("genres.xml")?.buffered().use {
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

private fun scan(file: String) {
    val file = File(file)
    val walkBottomUp = file.walkBottomUp()
    walkBottomUp.filter { it.name.endsWith(".fb2") }.forEach { file ->
        if (file.length() < 20) return@forEach
        val fb = FictionBook(file)
        db.transaction {
            if (fb.description.titleInfo.bookTitle == null) {
                Logger.error(IllegalStateException("No book title in ${file.absolutePath}"))
            }
            if (db.bookQueries.findBookByPathAndName(fb.description.titleInfo.bookTitle, file.absolutePath)
                    .executeAsOneOrNull() != null
            )
                return@transaction
            db.bookQueries.deleteBookByPath(file.absolutePath)
            db.bookQueries.createBook(
                path = file.absolutePath,
                name = fb.description.titleInfo.bookTitle,
                date = fb.description.titleInfo.date,
                added = LocalDateTime.now(),
                sequence = fb.description.titleInfo.sequence?.name,
                sequence_number = fb.description.titleInfo.sequence?.number?.toLongOrNull(),
                lang = fb.description.titleInfo.lang
            )
            val bookId = db.bookQueries.idByPath(file.absolutePath).executeAsOne()
            val authorIds = fb.description.titleInfo.authors.map {
                db.authorQueries.findByNames(
                    lastName = it.lastName?.trim(),
                    firstName = it.firstName?.trim(),
                    middleName = it.middleName?.trim(),
                    nickname = it.nickname?.trim()
                ).executeAsOneOrNull()
                    ?: kotlin.run {
                        db.authorQueries.create(
                            fb2id = it.id,
                            last_name = it.lastName?.trim(),
                            first_name = it.firstName?.trim(),
                            middle_name = it.middleName?.trim(),
                            nickname = it.nickname?.trim(),
                            added = LocalDateTime.now()
                        )
                        db.authorQueries.findByNames(
                            lastName = it.lastName?.trim(),
                            firstName = it.firstName?.trim(),
                            middleName = it.middleName?.trim(),
                            nickname = it.nickname?.trim(),
                        ).executeAsOneOrNull() ?: error("Issues with author in $file")
                    }
            }.toSet()
            val genreIds = fb.description.titleInfo.genres.map {
                db.genreQueries.idByName(it).executeAsOneOrNull()
                    ?: kotlin.run {
                        db.genreQueries.create(it)
                        db.genreQueries.idByName(it).executeAsOne()
                    }
            }.toSet()
            for (authorId in authorIds) {
                db.book_authorQueries.create(Book_author(bookId, authorId))
            }
            for (genreId in genreIds) {
                db.book_genreQueries.create(Book_genre(bookId, genreId))
            }
        }
    }
}



