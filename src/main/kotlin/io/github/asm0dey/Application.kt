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
package io.github.asm0dey

import com.kursx.parser.fb2.FictionBook
import io.github.asm0dey.model.Entry
import io.github.asm0dey.opdsko.jooq.Tables.*
import io.github.asm0dey.opdsko.jooq.tables.daos.BookAuthorDao
import io.github.asm0dey.opdsko.jooq.tables.daos.BookGenreDao
import io.github.asm0dey.opdsko.jooq.tables.daos.GenreDao
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author
import io.github.asm0dey.opdsko.jooq.tables.pojos.Book
import io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor
import io.github.asm0dey.opdsko.jooq.tables.pojos.BookGenre
import io.github.asm0dey.opdsko.jooq.tables.records.AuthorRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookAuthorRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookGenreRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookRecord
import io.github.asm0dey.plugins.OPDSKO_JDBC
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.impl.DSL.using
import org.tinylog.Logger
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.attribute.PosixFilePermission.*
import java.time.LocalDateTime
import java.util.*
import java.util.zip.ZipFile
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.StartElement
import kotlin.collections.set
import kotlin.io.path.Path
import kotlin.io.path.setPosixFilePermissions

const val FB2C_VERSION = "v1.69.0"

val genreNames = genreNames()
var epubConverterAccessible = true
fun downloadFile(url: URL, outputFileName: String) {
    url.openStream().use {
        Channels.newChannel(it).use { rbc ->
            FileOutputStream(outputFileName).use { fos ->
                fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
            }
        }
    }
}

val os by lazy {
    val osName = System.getProperty("os.name").lowercase()
    when {
        osName.contains("win") -> "win"
        osName.contains("linux") -> "linux"
        osName.contains("mac") -> "darwin"
        else -> {
            epubConverterAccessible = false
            null
        }
    }
}

fun main(args: Array<String>) {
    Flyway.configure().dataSource(OPDSKO_JDBC, null, null).load().migrate()
    val osArch = System.getProperty("os.arch").lowercase()
    val arch = if (os != null) {
        when {
            osArch.contains("64") -> when (os) {
                "linux" -> "_amd64"
                "win" -> "64"
                "darwin" -> "amd64"
                else -> error("Unsupported platform")
            }

            osArch.contains("86") -> when (os) {
                "linux" -> "_i386"
                "win" -> "32"
                else -> error("Unsupported platform")
            }

            else -> {
                epubConverterAccessible = false
                null
            }
        }
    } else null
    if (arch != null) {
        val targetFile = "fb2c_$FB2C_VERSION.zip"
        if (!File(targetFile).exists()) {
            println(
                "It seems that there is no archive of fb2c next to the executable,\n" +
                        "downloading it…"
            )
            downloadFile(
                URL("https://github.com/rupor-github/fb2converter/releases/download/$FB2C_VERSION/fb2c_$os$arch.zip"),
                targetFile
            )
            println(
                """Downloaded db2c archive.
                |Unpacking…
            """.trimMargin()
            )
        }
        ZipFile(targetFile).use {
            val myEntry = it.entries().asIterator().asSequence().first { it.name.startsWith("fb2c") }
            it.getInputStream(myEntry).use { inp ->
                FileOutputStream(myEntry.name).use { out ->
                    inp.buffered().copyTo(out.buffered())
                }
                posixSetAccessible(myEntry.name)
            }
        }
        if (!File("fb2c.conf.toml").exists())
            Application::class.java.classLoader.getResourceAsStream("fb2c.conf.toml").buffered().use { inp ->
                FileOutputStream("fb2c.conf.toml").buffered().use { out ->
                    inp.copyTo(out)
                }
                posixSetAccessible("fb2c.conf.toml")

            }
        println("Unpacked. Resuming application launch.")


    }
    EngineMain.main(args)
}

private fun posixSetAccessible(fileName: String) = try {
    Path(fileName).setPosixFilePermissions(setOf(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE))
} catch (_: Exception) {
}

@Suppress("UnusedReceiverParameter")
fun Application.main() {
    initDb()
}

private fun initDb() {
}

private fun genreNames(): HashMap<String, String> {
    fun StartElement.getAttributeValue(attribute: String) = getAttributeByName(QName.valueOf(attribute)).value

    Entry::class.java.classLoader.getResourceAsStream("genres.xml")?.buffered().use {
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

fun scan(libraryRoot: String, create: DSLContext) {
    File(libraryRoot).walkTopDown().filter { it.name.endsWith(".fb2", true) }.forEach { file ->
        val bookPath = file.absoluteFile.canonicalPath
        Logger.info { "Processing file $bookPath" }
        if (file.length() < 20) return@forEach
        val fb = try {
            FictionBook(file)
        } catch (e: Exception) {
            Logger.error("Unable to parse fb2 in file $bookPath", e)
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
            if (existingBookId != null) {
                Logger.debug { "Already added" }
                return@transaction
            }
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
                .returning()
                .fetchSingle()
                .id
            val authorIds = fb.description.titleInfo.authors.map {
                transaction.select(AUTHOR.ID)
                    .from(AUTHOR)
                    .where(
                        it.middleName.normalizeName()?.let { AUTHOR.MIDDLE_NAME.eq(it) } ?: AUTHOR.MIDDLE_NAME.isNull,
                        it.lastName.normalizeName()?.let { AUTHOR.LAST_NAME.eq(it) } ?: AUTHOR.LAST_NAME.isNull,
                        it.firstName.normalizeName()?.let { AUTHOR.FIRST_NAME.eq(it) } ?: AUTHOR.FIRST_NAME.isNull,
                        it.nickname.normalizeName()?.let { AUTHOR.NICKNAME.eq(it) } ?: AUTHOR.NICKNAME.isNull,
                    )
                    .fetchOne { it[AUTHOR.ID] }
                    ?: transaction.insertInto(AUTHOR)
                        .set(
                            AuthorRecord(
                                Author(
                                    null,
                                    it.id,
                                    it.firstName.normalizeName(),
                                    it.middleName.normalizeName(),
                                    it.lastName.normalizeName(),
                                    it.nickname.normalizeName(),
                                    LocalDateTime.now()
                                )
                            )
                        )
                        .returning(AUTHOR.ID)
                        .fetchSingle()
                        .id
            }.toSet()
            val genreIds = fb.description.titleInfo.genres.map {
                GenreDao(txConfig).fetchByName(it).firstOrNull()?.id
                    ?: transaction
                        .insertInto(GENRE, GENRE.NAME)
                        .values(it)
                        .returning()
                        .fetchSingle()
                        .id
            }.toSet()
            BookAuthorDao(txConfig).insert(authorIds.map { BookAuthor(bookId, it) })
            BookGenreDao(txConfig).insert(genreIds.map { BookGenre(bookId, it) })
            transaction.batchInsert(
                *authorIds.map { BookAuthorRecord(bookId, it) }.toTypedArray(),
                *genreIds.map { BookGenreRecord(bookId, it) }.toTypedArray(),
            )

        }
    }
}

fun String?.normalizeName() = if (isNullOrBlank()) null else trim().split(" ").joinToString(" ") {
    it.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
