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
import com.kursx.parser.fb2.Person
import io.github.asm0dey.inpx.InpxParser
import io.github.asm0dey.model.Entry
import io.github.asm0dey.opdsko.jooq.tables.daos.BookAuthorDao
import io.github.asm0dey.opdsko.jooq.tables.daos.BookGenreDao
import io.github.asm0dey.opdsko.jooq.tables.daos.GenreDao
import io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor
import io.github.asm0dey.opdsko.jooq.tables.pojos.BookGenre
import io.github.asm0dey.opdsko.jooq.tables.records.AuthorRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookAuthorRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookGenreRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookRecord
import io.github.asm0dey.opdsko.jooq.tables.references.AUTHOR
import io.github.asm0dey.opdsko.jooq.tables.references.BOOK
import io.github.asm0dey.opdsko.jooq.tables.references.GENRE
import io.ktor.server.application.*
import io.ktor.server.netty.*
import net.lingala.zip4j.ZipFile
import org.flywaydb.core.Flyway
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.tinylog.Logger
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.attribute.PosixFilePermission.*
import java.time.LocalDateTime
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.StartElement
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.collections.set
import kotlin.io.path.Path
import kotlin.io.path.setPosixFilePermissions

const val FB2C_VERSION = "v1.75.1"

val genreNames by lazy(PUBLICATION) {
    fun StartElement.getAttributeValue(attribute: String) =
        getAttributeByName(QName.valueOf(attribute)).value

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
                                data[currentGenre] =
                                    startElement.getAttributeValue("genre-title")

                        "subgenre" -> currentGenre = startElement.getAttributeValue("value")
                        "genre-descr" ->
                            if (startElement.getAttributeValue("lang") == "en")
                                data[currentGenre] = startElement.getAttributeValue("title")

                        "genre-alt" ->
                            data[startElement.getAttributeValue("value")] = data[currentGenre]!!
                    }
                }

                nextEvent.isEndElement ->
                    if (nextEvent.asEndElement().name.localPart == "genre") currentGenre = ""
            }
        }
        data
    }

}
var epubConverterAccessible = true

fun downloadFile(url: URL, outputFileName: String) {
    Channels.newChannel(url.openStream()).use { rbc ->
        FileOutputStream(outputFileName).use { fos ->
            fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
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
    downloadEpubConverter()
    EngineMain.main(args)
}

private fun downloadEpubConverter() {
    val osArch = System.getProperty("os.arch").lowercase()
    val arch =
        if (os != null) {
            when {
                osArch.contains("64") ->
                    when (os) {
                        "linux", "win", "darwin" -> "amd64"
                        else -> error("Unsupported platform")
                    }

                osArch.contains("86") ->
                    when (os) {
                        "linux", "win" -> "386"
                        else -> error("Unsupported platform")
                    }

                else -> {
                    epubConverterAccessible = false
                    null
                }
            }
        } else null
    if (arch != null) {
        val targetFile = "fb2c-$FB2C_VERSION.zip"
        if (!File(targetFile).exists()) {
            println(
                "It seems that there is no archive of fb2c next to the executable,\n" +
                        "downloading it…"
            )
            downloadFile(
                URI(
                    "https://github.com/rupor-github/fb2converter/releases/download/$FB2C_VERSION/fb2c-$os-$arch.zip"
                )
                    .toURL(),
                targetFile
            )
            println(
                """Downloaded fb2c archive.
                |Unpacking…
            """.trimMargin()
            )
        }
        ZipFile(targetFile).use {
            val myHeader = it.fileHeaders.first { it.fileName.startsWith("fb2c") }
            it.extractFile(
                myHeader,
                it.file.absoluteFile.parentFile.absolutePath,
                myHeader.fileName.substringAfter('/')
            )
            posixSetAccessible(myHeader.fileName)
        }
        if (!File("fb2c.conf.toml").exists())
            Application::class
                .java
                .classLoader
                .getResourceAsStream("fb2c.conf.toml")
                ?.buffered()
                ?.use { inp ->
                    FileOutputStream("fb2c.conf.toml").buffered().use { out ->
                        inp.copyTo(out)
                    }
                    posixSetAccessible("fb2c.conf.toml")
                }
        println("Unpacked. Resuming application launch.")
    }
}

private fun posixSetAccessible(fileName: String) =
    try {
        Path(fileName).setPosixFilePermissions(setOf(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE))
    } catch (_: Exception) {
    }

@Suppress("unused")
fun Application.main() {
    initDb()
    Flyway.configure()
        .mixed(true)
        .dataSource(
            environment.config.propertyOrNull("db.url")?.getString()
                ?: throw IllegalStateException("No db url defined"),
            environment.config.propertyOrNull("db.username")?.getString(),
            environment.config.propertyOrNull("db.password")?.getString()
        )
        .load()
        .migrate()
}

private fun initDb() {}

fun scan(libraryRoot: String, create: DSLContext, ext: String?, inpxMode: Boolean = false) {
    require(!inpxMode || (inpxMode && ext == null))
    if (inpxMode) {
        return handleInpx(libraryRoot, create)
    }
    if (ext == null || ext == "fb2")
        create.transaction { txCtx ->
            for (file in
            File(libraryRoot).walkTopDown().filter { it.name.endsWith(".fb2", true) }) {
                val bookPath = file.absoluteFile.canonicalPath
                Logger.info { "Processing file $bookPath" }
                if (file.length() < 20) continue
                val fb =
                    try {
                        FictionBook(file)
                    } catch (e: Exception) {
                        Logger.error("Unable to parse fb2 in file $bookPath", e)
                        continue
                    }
                txCtx.saveBook(
                    bookPath = bookPath,
                    title = fb.description?.titleInfo?.bookTitle,
                    date = fb.description?.titleInfo?.date,
                    seqName = fb.description?.titleInfo?.sequence?.name,
                    seqNo = fb.description?.titleInfo?.sequence?.number?.toIntOrNull(),
                    language = fb.description?.titleInfo?.lang,
                    authors = fb.description?.titleInfo?.authors,
                    genres = fb.description?.titleInfo?.genres
                )
            }
            updateSequenceIds(txCtx)
        }
    if (ext == null || ext == "zip")
        create.transaction { txCtx ->
            for (file in
            File(libraryRoot).walkTopDown().filter { it.name.endsWith(".zip", true) }) {
                val bookPath = file.absoluteFile.canonicalPath
                Logger.info { "Processing file $bookPath" }
                if (file.length() < 20) continue
                val zipFile = ZipFile(file)
                for (fileHeader in zipFile.fileHeaders) {
                    if (!fileHeader.fileName.endsWith(".fb2")) continue
                    val fb =
                        try {
                            Logger.info { "\tProcessing entry $fileHeader" }
                            FictionBook(zipFile, fileHeader)
                        } catch (e: Exception) {
                            Logger.error("Unable to parse fb2 in file $bookPath", e)
                            continue
                        }
                    txCtx.saveBook(
                        bookPath = fileHeader.fileName,
                        archive = zipFile.file.absoluteFile.canonicalPath,
                        title = fb.description?.titleInfo?.bookTitle,
                        date = fb.description?.titleInfo?.date,
                        seqName = fb.description?.titleInfo?.sequence?.name,
                        seqNo = fb.description?.titleInfo?.sequence?.number?.toIntOrNull(),
                        language = fb.description?.titleInfo?.lang,
                        authors = fb.description?.titleInfo?.authors,
                        genres = fb.description?.titleInfo?.genres
                    )
                }
            }
            updateSequenceIds(txCtx)
        }
}

private fun handleInpx(libraryRoot: String, create: DSLContext) {
    val inpx = File(libraryRoot).listFiles { it -> it.name.endsWith(".inpx") }?.first()
    require(inpx != null) { ".inpx should be located in the scan root" }
    val inpxData = InpxParser.scan(inpx.absolutePath)[true]!!
    val files =
        File(libraryRoot)
            .walkTopDown()
            .filter { it.name.endsWith(".zip") }
            .flatMap {
                val zip = ZipFile(it)
                zip.fileHeaders.map {
                    Triple(
                        zip.file.absolutePath,
                        it.fileName,
                        it.fileName
                            .substringAfterLast('/')
                            .substringBeforeLast(".fb2")
                            .toIntOrNull()
                    )
                }
            }
            .filterNot { it.third == null }
            .associateBy { it.third }
            .mapValues { (_, b) -> b.first to b.second }
    create.transaction { txConfig ->
        for ((key, value) in files) {
            Logger.info { "Processing book $key" }
            val fb = inpxData[key] ?: continue
            val authors =
                fb.authors.map {
                    Person().apply {
                        lastName = it.names.firstOrNull()
                        firstName = it.names.getOrNull(1)
                        middleName = it.names.getOrNull(2)
                    }
                }
            txConfig.saveBook(
                bookPath = value.second,
                archive = value.first,
                title = fb.title,
                date = fb.date,
                seqName = fb.bookSequence?.name,
                seqNo = fb.bookSequence?.no,
                language = fb.lang,
                authors = authors,
                genres = fb.genres
            )
        }
        try {
            updateSequenceIds(txConfig)
        } catch (e: Exception) {
            Logger.error(e) { "Error while updating sequence ids" }
        }
    }
    create.transaction { txConfig ->
        val deleted = InpxParser.scan(inpx.absolutePath)[false] ?: return@transaction
        deleted.keys.map { it.toLong() }.chunked(100).forEach {
            using(txConfig).deleteFrom(BOOK).where(BOOK.ID.`in`(it))
        }
    }
}

private fun updateSequenceIds(txConfig: Configuration) {
    val tr = using(txConfig)
    val names =
        selectDistinct(BOOK.SEQUENCE.`as`("seqname"))
            .from(BOOK)
            .where(BOOK.SEQUENCE.isNotNull)
            .orderBy(BOOK.SEQUENCE)
            .asTable("names")
    val numbers =
        select(rowNumber().over().`as`("seqrow"), names.field("seqname")!!.`as`("seqname"))
            .from(names)
            .asTable("numbers")
    tr.update(BOOK)
        .set(BOOK.SEQID, numbers.field("seqrow", Int::class.javaObjectType))
        .from(numbers)
        .where(BOOK.SEQUENCE.eq(numbers.field("seqname", String::class.java)))
        .execute()
}

private fun Configuration.saveBook(
    bookPath: String,
    archive: String? = null,
    title: String?,
    date: String?,
    seqName: String?,
    seqNo: Int?,
    language: String?,
    authors: List<Person>?,
    genres: List<String>?
) {
    if (title.isNullOrBlank()) {
        Logger.error(IllegalStateException("No book title in $bookPath"))
        return
    }

    val transaction = using(this)
    val existingBookId = transaction.findExistingBook(bookPath, title, archive)

    if (existingBookId != null) {
        Logger.debug { "Already added" }
        return
    }

    transaction.deleteExistingBooks(bookPath, archive)

    val bookId = transaction.insertBook(bookPath, title, date, seqName, seqNo, language, archive)
    val authorIds = transaction.processAuthors(authors)
    val genreIds = transaction.processGenres(genres)

    transaction.insertBookAssociations(bookId, authorIds, genreIds)
}

private fun DSLContext.findExistingBook(bookPath: String, title: String?, archive: String?): Long? {
    return select(BOOK.ID)
        .from(BOOK)
        .where(
            BOOK.NAME.eq(title),
            BOOK.PATH.eq(bookPath),
            BOOK.ZIP_FILE.isNotDistinctFrom(archive)
        )
        .limit(1)
        .fetchOne { it[BOOK.ID] }
}

private fun DSLContext.deleteExistingBooks(bookPath: String, archive: String?) {
    delete(BOOK)
        .where(BOOK.PATH.eq(bookPath), BOOK.ZIP_FILE.isNotDistinctFrom(archive))
        .execute()
}

private fun DSLContext.insertBook(
    bookPath: String,
    title: String,
    date: String?,
    seqName: String?,
    seqNo: Int?,
    language: String?,
    archive: String?
): Long {
    return insertInto(BOOK)
        .set(BookRecord(null, bookPath, title, date, LocalDateTime.now(), seqName, seqNo, language, archive, null))
        .returning()
        .fetchSingle()
        .id!!
}

private fun DSLContext.processAuthors(authors: List<Person>?): Set<Long> {
    return authors?.map {
        select(AUTHOR.ID)
            .from(AUTHOR)
            .where(
                AUTHOR.MIDDLE_NAME.isNotDistinctFrom(it.middleName.normalizeName()),
                AUTHOR.LAST_NAME.isNotDistinctFrom(it.lastName.normalizeName()),
                AUTHOR.FIRST_NAME.isNotDistinctFrom(it.firstName.normalizeName()),
                AUTHOR.NICKNAME.isNotDistinctFrom(it.nickname.normalizeName()),
            )
            .fetchOne { it[AUTHOR.ID] }
            ?: insertInto(AUTHOR)
                .set(
                    AuthorRecord(
                        null,
                        it.id,
                        it.firstName.normalizeName(),
                        it.middleName.normalizeName(),
                        it.lastName.normalizeName(),
                        it.nickname.normalizeName(),
                        LocalDateTime.now()
                    )
                )
                .returning(AUTHOR.ID)
                .fetchSingle()
                .id!!
    }?.toSet() ?: emptySet()
}

private fun DSLContext.processGenres(genres: List<String>?): Set<Long> {
    return genres?.map {
        GenreDao(configuration()).fetchByName(it).firstOrNull()?.id ?: insertInto(GENRE, GENRE.NAME)
            .values(it)
            .returning()
            .fetchSingle()
            .id!!
    }?.toSet() ?: setOf()
}

private fun DSLContext.insertBookAssociations(bookId: Long, authorIds: Set<Long>, genreIds: Set<Long>) {
//    BookAuthorDao(configuration()).insert(authorIds.map { BookAuthor(bookId, it) })
//    BookGenreDao(configuration()).insert(genreIds.map { BookGenre(bookId, it) })
    batchInsert(
        authorIds.map { BookAuthorRecord(bookId, it) } +
                genreIds.map { BookGenreRecord(bookId, it) }
    )
        .execute()
}

fun String?.normalizeName() =
    if (isNullOrBlank()) null
    else
        trim().split(" ").joinToString(" ") {
            it.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
