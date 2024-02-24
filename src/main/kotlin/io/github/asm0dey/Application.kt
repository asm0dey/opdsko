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
import io.github.asm0dey.opdsko.jooq.public.keys.GENRE_NAME_KEY
import io.github.asm0dey.opdsko.jooq.public.tables.records.*
import io.github.asm0dey.opdsko.jooq.public.tables.references.AUTHOR
import io.github.asm0dey.opdsko.jooq.public.tables.references.BOOK
import io.github.asm0dey.opdsko.jooq.public.tables.references.GENRE
import io.ktor.server.application.*
import io.ktor.server.netty.*
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import net.lingala.zip4j.ZipFile
import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.tinylog.Logger
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.attribute.PosixFilePermission.*
import java.sql.DriverManager
import java.time.OffsetDateTime
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.StartElement
import kotlin.collections.set
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.setPosixFilePermissions


const val FB2C_VERSION = "v1.75.1"

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
    thread {
        val osArch = System.getProperty("os.arch").lowercase()
        val arch = if (os != null) {
            when {
                osArch.contains("64") -> when (os) {
                    "linux" -> "-amd64"
                    "win" -> "64"
                    "darwin" -> "amd64"
                    else -> error("Unsupported platform")
                }

                osArch.contains("86") -> when (os) {
                    "linux" -> "-i386"
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
                    URI("https://github.com/rupor-github/fb2converter/releases/download/$FB2C_VERSION/fb2c-$os$arch.zip").toURL(),
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
                Application::class.java.classLoader.getResourceAsStream("fb2c.conf.toml")?.buffered()?.use { inp ->
                    FileOutputStream("fb2c.conf.toml").buffered().use { out ->
                        inp.copyTo(out)
                    }
                    posixSetAccessible("fb2c.conf.toml")

                }
            println("Unpacked. Resuming application launch.")


        }
    }
    EngineMain.main(args)
}

private fun posixSetAccessible(fileName: String) = try {
    Path(fileName).setPosixFilePermissions(setOf(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE))
} catch (_: Exception) {
}

@Suppress("unused")
fun Application.main() {
    val connection = DriverManager.getConnection(
        environment.config.propertyOrNull("db.url")?.getString()
            ?: throw IllegalStateException("No db url defined"),
        environment.config.propertyOrNull("db.username")?.getString(),
        environment.config.propertyOrNull("db.password")?.getString(),
    )
    connection.use {
        val database: liquibase.database.Database =
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                JdbcConnection(
                    connection
                )
            )
        val liquibase = Liquibase("db/changelog-main.xml", ClassLoaderResourceAccessor(), database)
        liquibase.update()
    }

}

private fun initDb() {
}

fun genreNames(): Map<String, String> {
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

fun scan(libraryRoot: String, create: DSLContext, ext: String?, inpxMode: Boolean = false) {
    require(!inpxMode || (inpxMode && ext == null))
    if (inpxMode) {
        val inpx = File(libraryRoot).listFiles { it -> it.name.endsWith(".inpx") }?.first()
        require(inpx != null) { ".inpx should be located in the scan root" }
        val inpxData = InpxParser.scan(inpx.absolutePath)
        val files = File(libraryRoot)
            .walkTopDown()
            .filter { it.name.endsWith(".zip") }
            .flatMap {
                val zip = ZipFile(it)
                zip.fileHeaders.map {
                    Triple(
                        zip.file.absolutePath,
                        it.fileName,
                        it.fileName.substringAfterLast('/').substringBeforeLast(".fb2").toIntOrNull()
                    )
                }
            }
            .filterNot { it.third == null }
            .associateBy { it.third }
            .mapValues { (_, b) -> b.first to b.second }
            .toList()
            .chunked(1000)
        for (chunk in files) {
            create.transaction { txc ->
                for ((key, value) in chunk) {
                    Logger.info { "Processing book $key" }
                    val fb = inpxData[key] ?: continue
                    val authors = fb.authors.map {
                        Person().apply {
                            lastName = it.names.firstOrNull()
                            firstName = it.names.getOrNull(1)
                            middleName = it.names.getOrNull(2)
                        }
                    }
                    using(txc).saveBook(
                        bookPath = value.second,
                        archive = value.first,
                        title = fb.title,
                        date = fb.date,
                        seqName = fb.bookSequence?.name,
                        seqNo = fb.bookSequence?.no,
                        lang = fb.lang,
                        authors = authors,
                        genres = fb.genres
                    )
                }

            }
        }
        try {
            updateSequenceIds(create)
        } catch (e: Exception) {
            Logger.error(e) { "Error while updating sequence ids" }
        }

        return
    }
    if (ext == null || ext == "fb2")
        for (file in File(libraryRoot).walkTopDown().filter { it.name.endsWith(".fb2", true) }) {
            val bookPath = file.absoluteFile.canonicalPath
            Logger.info { "Processing file $bookPath" }
            if (file.length() < 20) continue
            val fb = try {
                FictionBook(file)
            } catch (e: Exception) {
                Logger.error("Unable to parse fb2 in file $bookPath", e)
                continue
            }
            create.saveBook(
                bookPath = bookPath,
                title = fb.description?.titleInfo?.bookTitle,
                date = fb.description?.titleInfo?.date,
                seqName = fb.description?.titleInfo?.sequence?.name,
                seqNo = fb.description?.titleInfo?.sequence?.number?.toIntOrNull(),
                lang = fb.description?.titleInfo?.lang,
                authors = fb.description?.titleInfo?.authors,
                genres = fb.description?.titleInfo?.genres
            )
        }
    updateSequenceIds(create)
    if (ext == null || ext == "zip")
        for (file in File(libraryRoot).walkTopDown().filter { it.name.endsWith(".zip", true) }) {
            val bookPath = file.absoluteFile.canonicalPath
            Logger.info { "Processing file $bookPath" }
            if (file.length() < 20) continue
            val zipFile = ZipFile(file)
            for (fileHeader in zipFile.fileHeaders) {
                if (!fileHeader.fileName.endsWith(".fb2")) continue
                val fb = try {
                    Logger.info { "\tProcessing entry $fileHeader" }
                    FictionBook(zipFile, fileHeader)
                } catch (e: Exception) {
                    Logger.error("Unable to parse fb2 in file $bookPath", e)
                    continue
                }
                create.saveBook(
                    bookPath = fileHeader.fileName,
                    archive = zipFile.file.absoluteFile.canonicalPath,
                    title = fb.description?.titleInfo?.bookTitle,
                    date = fb.description?.titleInfo?.date,
                    seqName = fb.description?.titleInfo?.sequence?.name,
                    seqNo = fb.description?.titleInfo?.sequence?.number?.toIntOrNull(),
                    lang = fb.description?.titleInfo?.lang,
                    authors = fb.description?.titleInfo?.authors,
                    genres = fb.description?.titleInfo?.genres
                )
            }
        }
    updateSequenceIds(create)

}

private fun updateSequenceIds(txConfig: DSLContext) {
    val names = selectDistinct(BOOK.SEQUENCE.`as`("seqname"))
        .from(BOOK)
        .where(BOOK.SEQUENCE.isNotNull)
        .orderBy(BOOK.SEQUENCE)
        .asTable("names")
    val numbers = select(rowNumber().over().`as`("seqrow"), names.field("seqname")!!.`as`("seqname"))
        .from(names)
        .asTable("numbers")
    txConfig.update(BOOK)
        .set(BOOK.SEQID, numbers.field("seqrow", Int::class.javaObjectType))
        .from(numbers)
        .where(BOOK.SEQUENCE.eq(numbers.field("seqname", String::class.java)))
        .execute()
}

private fun DSLContext.saveBook(
    bookPath: String,
    archive: String? = null,
    title: String?,
    date: String?,
    seqName: String?,
    seqNo: Int?,
    lang: String?,
    authors: List<Person>?,
    genres: List<String>?
) {
    if (title.isNullOrBlank()) {
        Logger.error(IllegalStateException("No book title in $bookPath"))
        return
    }
    val existingBookId = this.select(BOOK.ID)
        .from(BOOK)
        .where(
            BOOK.NAME.eq(title),
            BOOK.PATH.eq(bookPath),
            BOOK.ZIP_FILE.isNotDistinctFrom(archive)
        )
        .limit(1)
        .fetchOne { it[BOOK.ID] }
    if (existingBookId != null) {
        Logger.debug { "Already added" }
        return
    }

    val bookId = this
        .insertInto(BOOK)
        .set(
            BookRecord(
                null,
                bookPath,
                title,
                date,
                OffsetDateTime.now(),
                seqName,
                seqNo?.toLong(),
                lang,
                archive,
                null
            )
        )
        .onConflict(BOOK.PATH, BOOK.ZIP_FILE)
        .doUpdate()
        .set(
            mapOf(
                BOOK.NAME to excluded(BOOK.NAME),
                BOOK.SEQUENCE to excluded(BOOK.SEQUENCE),
                BOOK.DATE to excluded(BOOK.DATE),
                BOOK.ADDED to excluded(BOOK.ADDED),
                BOOK.SEQUENCE_NUMBER to excluded(BOOK.SEQUENCE_NUMBER),
                BOOK.LANG to excluded(BOOK.LANG)
            )
        )
        .returning()
        .fetchSingle()
        .id!!
    val authorIds = this.insertInto(AUTHOR)
        .set(
            authors?.toSet()?.map {
                AuthorRecord(
                    null,
                    it.id?.toIntOrNull(),
                    it.firstName.normalizeName(),
                    it.middleName.normalizeName(),
                    it.lastName.normalizeName(),
                    it.nickname.normalizeName(),
                    OffsetDateTime.now()
                )

            }
        )
        .onConflict(AUTHOR.MIDDLE_NAME, AUTHOR.LAST_NAME, AUTHOR.FIRST_NAME, AUTHOR.NICKNAME)
        .doUpdate()
        .set(AUTHOR.FB2ID, excluded(AUTHOR.FB2ID))
        .returning(AUTHOR.ID)
        .fetch { it[AUTHOR.ID]!! }
        .toSet()

    val genreIds = this
        .insertInto(GENRE)
        .set(genres?.distinct()?.map { GenreRecord(null, it) })
        .onConflictOnConstraint(GENRE_NAME_KEY)
        .doUpdate()
        .set(GENRE.NAME, excluded(GENRE.NAME))
        .returning()
        .fetch { it[GENRE.ID]!! }
        .toSet()
    this.batchInsert(
        authorIds.map { BookAuthorRecord(bookId, it) } +
                genreIds.map { BookGenreRecord(bookId, it) }
    )
        .execute()
}

fun String?.normalizeName() = if (isNullOrBlank()) null else trim().split(" ").joinToString(" ") {
    it.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
