package io.github.asm0dey.service

import com.kursx.parser.fb2.Binary
import com.kursx.parser.fb2.FictionBook
import io.github.asm0dey.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class InfoService(private val repo: Repository) {
    fun searchBookByText(searchTerm: String, page: Int, pageSize: Int = 50): Pair<List<BookWithInfo>, Boolean> {
        return repo.searchBookByText(searchTerm, page, pageSize)
    }

    fun shortDescriptions(bookWithInfos: List<BookWithInfo>) =
        bookWithInfos.map { it.id to it.book }
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

    fun imageTypes(bookWithInfos: List<BookWithInfo>) = bookWithInfos
        .map { it.id to it.book.path }
        .associate { (id, path) ->
            val fb = FictionBook(File(path))
            val type = fb.description.titleInfo.coverPage.firstOrNull()?.value?.replace("#", "")?.let {
                fb.binaries[it]?.contentType
            }
            id to type
        }

    fun latestBooks() = repo.latestBooks()

    fun imageByBookId(bookId: Long): Pair<Binary, ByteArray> {
        val path = repo.bookPath(bookId)
        val fb = FictionBook(File(path))
        val id = fb.description.titleInfo.coverPage.first().value.replace("#", "")
        val binary = fb.binaries[id]!!
        val data = Base64().decode(binary.binary)
        return Pair(binary, data)
    }

    fun bookInfo(bookId: Long) = BookWithInfo(repo.bookInfo(bookId))

    suspend fun zippedBook(bookId: Long): ByteArray {
        return ByteArrayOutputStream().use { baos ->
            ZipOutputStream(baos).use {
                it.apply {
                    val path = repo.bookPath(bookId)
                    putNextEntry(ZipEntry("$bookId.fb2"))
                    withContext(Dispatchers.IO) {
                        write(File(path).readBytes())
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

