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
package io.github.asm0dey.service

import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import com.github.pgreze.process.unwrap
import com.kursx.parser.fb2.Binary
import com.kursx.parser.fb2.FictionBook
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IBook
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author
import io.github.asm0dey.opdsko.jooq.tables.pojos.Book
import io.github.asm0dey.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import net.lingala.zip4j.ZipFile
import org.apache.commons.codec.binary.Base64
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.EntryUnit
import org.ehcache.config.units.MemoryUnit
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration
import org.ehcache.spi.serialization.Serializer
import org.jooq.Record2
import org.jooq.Record4
import org.jooq.Result
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

class InfoService(private val repo: Repository) {
    private val cacheDir = if (!Path.of("cache").exists()) Files.createDirectory(Path.of("cache")) else Path.of("cache")
    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerPersistenceConfiguration(cacheDir.toFile()))
        .withCache(
            "articlesCache",
            CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                    String::class.javaObjectType,
                    FictionBook::class.java,
                    ResourcePoolsBuilder
                        .newResourcePoolsBuilder()
                        .heap(1000, EntryUnit.ENTRIES)
                        .offheap(50, MemoryUnit.MB)
                        .disk(4, MemoryUnit.GB, true)
                )
                .withValueSerializer(FBSerializer)
        )
        .withCache(
            "sizeCache",
            CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                    String::class.javaObjectType,
                    String::class.java,
                    ResourcePoolsBuilder
                        .newResourcePoolsBuilder()
                        .heap(1000, EntryUnit.ENTRIES)
                        .offheap(10, MemoryUnit.MB)
                        .disk(1, MemoryUnit.GB, true)
                )
        )
        .build(true)

    private val bookCache =
        cacheManager.getCache("articlesCache", String::class.javaObjectType, FictionBook::class.java)
    private val sizeCache =
        cacheManager.getCache("sizeCache", String::class.javaObjectType, String::class.java)


    fun searchBookByText(searchTerm: String, page: Int, pageSize: Int = 50): Triple<List<BookWithInfo>, Boolean, Int> {
        return repo.searchBookByText(searchTerm, page, pageSize)
    }

    fun shortDescriptions(bookWithInfos: List<BookWithInfo>) =
        bookWithInfos.map { it.id to it.book }
            .associate { (id, book) ->
                val size = book.size
                val seq = book.sequence
                val seqNo = book.sequenceNumber

                val fb = obtainBook(book.zipFile, book.path)


                val descr = fb.description?.titleInfo?.annotation?.text ?: ""
                val text = buildString {
                    append("Size: $size.\n ")
                    seq?.let { append("Series: $it") }
                    seqNo?.let { append("#${it.toString().padStart(3, '0')}") }
                    seq?.let { append(".\n ") }
                    append(descr)
                }
                id to text
            }

    private val IBook.size: String
        get() {
            val bookPath = if (zipFile == null) path else "$zipFile#$path"
            return sizeCache[bookPath] ?: (doGetBookSize(this)).also { sizeCache.put(bookPath, it) }
        }

    private fun doGetBookSize(book: IBook) =
        if (book.zipFile == null) File(book.path).length().humanReadable() else {
            ZipFile(book.zipFile).getFileHeader(book.path).uncompressedSize.humanReadable()
        }

    private fun obtainBook(zipFile: String?, path: String): FictionBook {
        val bookPath = if (zipFile == null) path else "$zipFile#$path"
        return bookCache[bookPath] ?: readFb(zipFile, path).also { bookCache.put(bookPath, it) }
    }

    private fun readFb(zipFile: String?, path: String) =
        if (zipFile == null) FictionBook(File(path)) else {
            val zip = ZipFile(zipFile)
            val header = zip.getFileHeader(path)
            FictionBook(zip, header)
        }

    fun imageTypes(bookWithInfos: List<BookWithInfo>) = bookWithInfos
        .map { Triple(it.id, it.book.path, it.book.zipFile) }
        .associate { (id, path, zipFile) ->
            val fb = obtainBook(zipFile, path)
            val type = fb.description?.titleInfo?.coverPage?.firstOrNull()?.value?.replace("#", "")?.let {
                fb.binaries[it]?.contentType
            }
            id to type
        }

    fun latestBooks(): Result<Record4<Long, MutableList<Book>, MutableList<Author>, List<Record2<String, Long>>>> =
        repo.latestBooks()

    fun imageByBookId(bookId: Long): Pair<Binary, ByteArray> {
        val (path, archive) = repo.bookPath(bookId)
        val fb = obtainBook(archive, path)
        val id = fb.description?.titleInfo?.coverPage?.first()?.value?.replace("#", "")
        val binary = fb.binaries[id]!!
        val data = Base64().decode(binary.binary)
        return Pair(binary, data)
    }

    fun bookInfo(bookId: Long) = BookWithInfo(repo.bookInfo(bookId))

    @OptIn(ExperimentalPathApi::class)
    suspend fun toEpub(bookId: Long): ByteArray {
        val (path, archive) = repo.bookPath(bookId)
        val tmp = withContext(Dispatchers.IO) {
            Files.createTempDirectory("fb2c")
        }
        val source = if (archive == null) path
        else {
            val zip = ZipFile(archive)
            val header = zip.getFileHeader(path)
            val tmpFile = withContext(Dispatchers.IO) {
                File.createTempFile("conv", ".fb2")
            }
            tmpFile.delete()
            zip.extractFile(header, tmpFile.parent, tmpFile.name)
            tmpFile.absolutePath
        }
        val executable = "./fb2c".takeIf { File(it).exists() } ?: "fb2c.exe"
        process(
            executable,
            "-c",
            "fb2c.conf.toml",
            "convert",
            "--to",
            "epub",
            source,
            tmp.absolutePathString(),
            stdout = Redirect.SILENT,
            stderr = Redirect.SILENT,
        ).unwrap()
        try {
            return tmp.toFile().listFiles()!!.first().readBytes()
        } finally {
            tmp.deleteRecursively()
            File(source).delete()
        }
    }

    suspend fun zippedBook(bookId: Long): ByteArray {
        val (path, archive) = repo.bookPath(bookId)
        val bytes = if (archive == null) File(path).readBytes() else {
            val zip = ZipFile(archive)
            val header = zip.getFileHeader(path)
            zip.getInputStream(header).use { it.readAllBytes() }
        }
        return packedBytes(bytes, "$bookId.fb2")
    }

    private suspend fun packedBytes(bytes: ByteArray, name: String = UUID.randomUUID().toString()): ByteArray {
        return ByteArrayOutputStream().use { baos ->
            ZipOutputStream(baos).use {
                it.apply {
                    putNextEntry(ZipEntry(name))
                    withContext(Dispatchers.IO) {
                        write(bytes)
                    }
                    closeEntry()
                }
            }
            baos.toByteArray()
        }

    }

    fun authorNameStarts(prefix: String, trim: Boolean = true) = repo.authorNameStarts(prefix, trim)
    fun authorName(authorId: Long) = repo.authorName(authorId)
    fun seriesNumberByAuthor(authorId: Long) = repo.seriesNumberByAuthor(authorId)
    fun latestAuthorUpdate(authorId: Long) = repo.latestAuthorUpdate(authorId).z
    fun allBooksByAuthor(authorId: Long) = repo.allBooksByAuthor(authorId)
    fun seriesByAuthorId(authorId: Long) = repo.seriesByAuthorId(authorId)
    fun booksBySeriesAndAuthor(seriesName: String, authorId: Long) = repo.booksBySeriesAndAuthor(seriesName, authorId)
    fun booksWithoutSeriesByAuthorId(authorId: Long) = repo.booksWithoutSeriesByAuthorId(authorId)
    fun booksBySeriesName(name: String) = repo.booksBySeriesName(name)
    fun seriesNameStarts(prefix: String, trim: Boolean = true) = repo.seriesNameStarts(prefix, trim)
    fun genres(): List<Triple<Long, String, Int>> {
        return repo.genres()
    }

    fun genreName(genreId: Long): String? {
        return repo.genreName(genreId)
    }

    fun genreAuthors(genreId: Long): List<Pair<Long, String>> {
        return repo.genreAuthors(genreId)
    }

    fun booksByGenreAndAuthor(genreId: Long, authorId: Long): Pair<String, List<BookWithInfo>> {
        return repo.booksByGenreAndAuthor(genreId, authorId)
    }

    fun booksInGenre(genreId: Long, page: Int, pageSize: Int = 50): Pair<Int, List<BookWithInfo>> {
        return repo.booksInGenre(genreId, page, pageSize)
    }
}

object FBSerializer : Serializer<FictionBook> {

    @OptIn(ExperimentalSerializationApi::class)
    override fun equals(fb: FictionBook, binary: ByteBuffer): Boolean =
        Cbor.decodeFromByteArray<FictionBook>(binary.array()) == fb

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(fb: FictionBook): ByteBuffer = ByteBuffer.wrap(Cbor.encodeToByteArray(fb))

    @OptIn(ExperimentalSerializationApi::class)
    override fun read(binary: ByteBuffer): FictionBook = Cbor.decodeFromByteArray(binary.array())

}


