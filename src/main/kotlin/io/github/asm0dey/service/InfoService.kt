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
import io.github.asm0dey.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.*

class InfoService(private val repo: Repository) {
    fun searchBookByText(searchTerm: String, page: Int, pageSize: Int = 50): Pair<List<BookWithInfo>, Boolean> {
        return repo.searchBookByText(searchTerm, page, pageSize)
    }

    fun shortDescriptions(bookWithInfos: List<BookWithInfo>) =
        bookWithInfos.map { it.id to it.book }
            .associate { (id, book) ->
                val path = File(book.path)
                var size = path.length().humanReadable()
                val seq = book.sequence
                val seqNo = book.sequenceNumber
                val fb = if (book.zipFile == null) FictionBook(path) else {
                    val zip = ZipFile(book.zipFile)
                    val header = zip.getFileHeader(book.path)
                    size = header.uncompressedSize.humanReadable()
                    FictionBook(zip, header)

                }
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

    fun imageTypes(bookWithInfos: List<BookWithInfo>) = bookWithInfos
        .map { Triple(it.id, it.book.path, it.book.zipFile) }
        .associate { (id, path, zipFile) ->
            val fb = if (zipFile == null) FictionBook(File(path)) else {
                val zip = ZipFile(zipFile)
                FictionBook(zip, zip.getFileHeader(path))
            }
            val type = fb.description?.titleInfo?.coverPage?.firstOrNull()?.value?.replace("#", "")?.let {
                fb.binaries[it]?.contentType
            }
            id to type
        }

    fun latestBooks() = repo.latestBooks()

    fun imageByBookId(bookId: Long): Pair<Binary, ByteArray> {
        val (path, archive) = repo.bookPath(bookId)
        val fb = if (archive == null) FictionBook(File(path)) else {
            val zipFile = ZipFile(archive)
            FictionBook(zipFile, zipFile.getFileHeader(path))
        }
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
            val tmpFile = File.createTempFile("conv", ".fb2")
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
}

