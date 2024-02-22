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

import com.kursx.parser.fb2.Element
import com.kursx.parser.fb2.FictionBook
import io.github.asm0dey.genreNames
import io.github.asm0dey.opdsko.jooq.public.tables.interfaces.IAuthor
import io.github.asm0dey.opdsko.jooq.public.tables.interfaces.IBook
import io.github.asm0dey.opdsko.jooq.public.tables.records.AuthorRecord
import io.github.asm0dey.opdsko.jooq.public.tables.records.BookRecord
import io.github.asm0dey.opdsko.jooq.public.tables.records.GenreRecord
import io.github.asm0dey.plugins.dtf
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.lingala.zip4j.ZipFile
import org.jooq.Record3
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
value class BookWithInfo(val record: Record3<BookRecord, List<AuthorRecord>, List<GenreRecord>>) {
    val book: IBook
        get() = record.component1()
    val authors: List<IAuthor>
        get() = record.component2()!!
    val genres: List<Pair<String, Long>>
        get() = record.component3().map {
            val id = it.id!!
            val name = genreNames[it.name] ?: it.name
            name to id
        }
    val id
        get() = book.id!!
    val sequence
        get() = book.sequence
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

fun bookDescriptionsLonger(
    pathsByIds: List<Pair<Long, IBook>>,
    sequenceLinkBase: String = "/api/series/item/",
    htmx: Boolean = true
): Map<Long, String?> {
    return pathsByIds
        .associate { (id, book) ->
            val file = File(book.path)
            val seq = book.sequence
            val seqNo = book.sequenceNumber
            val (size, fb) = if (book.zipFile == null) file.length().humanReadable() to FictionBook(file)
            else {
                val zip = ZipFile(book.zipFile)
                val header = zip.getFileHeader(book.path)
                header.uncompressedSize.humanReadable() to FictionBook(zip, header)
            }
            val elements = fb.description?.titleInfo?.annotation?.elements
            val descr = elements?.let { Element.getText(elements, "<br>") } ?: ""
            val text = createHTML(false).div {
                p {
                    b { +"Size" }
                    +": $size"
                }
                seq?.let {
                    p {
                        b { +"Series" }
                        +": "
                        a {
                            if (htmx) {
                                at["_"] = "on click take .is-active from #modal wait 200ms then remove #modal"
                                at["hx-trigger"] = "click"
                                at["hx-get"] = "$sequenceLinkBase${book.seqid}"
                                at["hx-target"] = "#layout"
                                at["hx-push-url"] = "true"
                            } else {
                                href = "$sequenceLinkBase${book.seqid}"
                            }
                            +seq
                        }
                        seqNo?.let { +" #${it.toString().padStart(3, '0')}" }
                    }
                }
                unsafe { +descr }
            }
            id to text
        }
}

val Tag.at
    get() = attributes

