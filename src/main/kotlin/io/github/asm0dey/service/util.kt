package io.github.asm0dey.service

import com.kursx.parser.fb2.Element
import com.kursx.parser.fb2.FictionBook
import io.github.asm0dey.genreNames
import io.github.asm0dey.opdsko.jooq.Tables
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IAuthor
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IBook
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author
import io.github.asm0dey.opdsko.jooq.tables.pojos.Book
import io.github.asm0dey.plugins.dtf
import org.jooq.Record4
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.StringCharacterIterator
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAccessor
import kotlin.math.abs
import kotlin.math.sign

val LocalDateTime.z: ZonedDateTime
    get() = ZonedDateTime.of(this, ZoneId.systemDefault())
val String.decoded: String get() = URLDecoder.decode(this, StandardCharsets.UTF_8)

@JvmInline
@Suppress("unused")
value class BookWithInfo(val record: Record4<Long, List<Book>, List<Author>, List<String>>) {
    val book: IBook
        get() = record.component2()[0]
    val authors: List<IAuthor>
        get() = record.component3()!!
    val genres: List<String>
        get() = record.component4().map { genreNames[it] ?: it }
    val id
        get() = record.get(Tables.BOOK.ID)!!
}

@Suppress("unused")
fun formatDate(x: LocalDateTime): String = dtf.format(x.z)

@Suppress("unused")
fun formatDate(x: ZonedDateTime): String = dtf.format(x)

@Suppress("unused")
fun formatDate(x: TemporalAccessor): String = dtf.format(x)

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

