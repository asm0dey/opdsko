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
package io.github.asm0dey.repository

import io.github.asm0dey.genreNames
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.routines.references.search
import io.github.asm0dey.opdsko.jooq.public.tables.Author
import io.github.asm0dey.opdsko.jooq.public.tables.Author.Companion.AUTHOR
import io.github.asm0dey.opdsko.jooq.public.tables.Book
import io.github.asm0dey.opdsko.jooq.public.tables.Book.Companion.BOOK
import io.github.asm0dey.opdsko.jooq.public.tables.BookAuthor.Companion.BOOK_AUTHOR
import io.github.asm0dey.opdsko.jooq.public.tables.Genre.Companion.GENRE
import io.github.asm0dey.opdsko.jooq.public.tables.interfaces.IAuthor
import io.github.asm0dey.opdsko.jooq.public.tables.pojos.Genre
import io.github.asm0dey.opdsko.jooq.public.tables.records.AuthorRecord
import io.github.asm0dey.opdsko.jooq.public.tables.records.BookRecord
import io.github.asm0dey.opdsko.jooq.public.tables.records.GenreRecord
import io.github.asm0dey.service.BookWithInfo
import org.jooq.*
import org.jooq.impl.DSL.*
import org.jooq.kotlin.get
import org.jooq.kotlin.mapping
import java.time.OffsetDateTime
import kotlin.sequences.Sequence

class Repository(val create: DSLContext) {
    private val genres by lazy { genreNames() }
    fun seriesByAuthorId(authorId: Long): Map<Pair<Int, String>, Pair<OffsetDateTime, Int>> {
        val latestBookInSeq = max(BOOK.ADDED)
        val booksInSeries = count(BOOK.ID)
        return create.select(BOOK.SEQUENCE, latestBookInSeq, booksInSeries, BOOK.SEQID)
            .from(BOOK)
            .innerJoin(BOOK.bookAuthor())
            .where(BOOK.bookAuthor().AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNotNull)
            .groupBy(BOOK.SEQUENCE)
            .orderBy(BOOK.SEQUENCE, latestBookInSeq.desc())
            .fetch {
                (it[BOOK.SEQID]!! to it[BOOK.SEQUENCE]!!) to (it[latestBookInSeq]!! to it[booksInSeries])
            }
            .toMap()
    }

    private val bookAuthors = multiset(
        selectDistinct(AUTHOR)
            .from(AUTHOR)
            .innerJoin(AUTHOR.bookAuthor())
            .where(AUTHOR.bookAuthor().BOOK_ID.eq(BOOK.ID))
    ).`as`("authors").mapping { it }

    private val bookGenres = multiset(
        selectFrom(GENRE)
            .where(GENRE.bookGenre.BOOK_ID.eq(BOOK.ID))
    ).`as`("genres").convertFrom { it.map { it } }

    private fun Result<Record1<String>>.toList(): List<String> =
        collect(Records.intoList())

    private val bookById = run {
        val bookAlias = Book("b")
        selectFrom(bookAlias).where(bookAlias.ID.eq(BOOK.ID)).asField<BookRecord>()
    }

    fun getBookWithInfo() =
        create
            .selectDistinct(
                bookById,
                bookAuthors,
                bookGenres,
            )
            .from(BOOK)

    fun seriesNumberByAuthor(authorId: Long): Pair<Int, Int> {
        val notnull = count(BOOK.ID).filterWhere(BOOK.SEQUENCE.isNotNull).`as`("x")
        val isnull = count(BOOK.ID).filterWhere(BOOK.SEQUENCE.isNull).`as`("y")
        return create
            .select(
                notnull,
                isnull,
            )
            .from(BOOK_AUTHOR)
            .innerJoin(BOOK.bookAuthor())
            .where(BOOK.bookAuthor().AUTHOR_ID.eq(authorId))
            .limit(1)
            .fetchSingle { it[notnull] to it[isnull] }
    }

    fun latestAuthorUpdate(authorId: Long): OffsetDateTime {
        return create
            .select(BOOK.ADDED)
            .from(BOOK)
            .innerJoin(BOOK.bookAuthor())
            .where(BOOK.bookAuthor().AUTHOR_ID.eq(authorId))
            .orderBy(BOOK.ADDED.desc())
            .limit(1)
            .fetchSingle { it[BOOK.ADDED] }
    }

    fun bookPath(bookId: Long): Pair<String, String?> =
        create.select(BOOK.PATH, BOOK.ZIP_FILE).from(BOOK).where(BOOK.ID.eq(bookId))
            .fetchSingle { it[BOOK.PATH]!! to it[BOOK.ZIP_FILE] }

    private val fullName = concat(
        coalesce(AUTHOR.LAST_NAME, ""),
        if_(
            AUTHOR.FIRST_NAME.isNotNull,
            if_(
                AUTHOR.LAST_NAME.isNotNull,
                concat(", ", AUTHOR.FIRST_NAME),
                AUTHOR.FIRST_NAME
            ),
            ""
        ),
        if_(
            AUTHOR.MIDDLE_NAME.isNotNull,
            if_(
                AUTHOR.FIRST_NAME.isNotNull.or(AUTHOR.LAST_NAME.isNotNull),
                concat(" ", AUTHOR.MIDDLE_NAME),
                AUTHOR.MIDDLE_NAME
            ),
            ""
        ),
        if_(
            AUTHOR.NICKNAME.isNotNull,
            if_(
                AUTHOR.FIRST_NAME.isNull.and(AUTHOR.LAST_NAME.isNotNull)
                    .and(AUTHOR.MIDDLE_NAME.isNotNull),
                AUTHOR.NICKNAME,
                concat(`val`(" ("), AUTHOR.NICKNAME, `val`(")"))
            ),
            ""
        ),
    )

    fun authorName(authorId: Long): String {
        return create
            .select(fullName)
            .from(AUTHOR)
            .where(AUTHOR.ID.eq(authorId))
            .fetchSingle { it[fullName] }
    }

    fun searchBookByText(term: String, page: Int, pageSize: Int): Triple<List<BookWithInfo>, Boolean, Int> {
        val ids = create.select(
            BOOK.ID,
//            bookById
        )
            .from(
                search(
                    term,
                    page + pageSize,
                    pageSize + 1,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        val total = create.select(
            count(BOOK.ID),
//            bookById
        )
            .from(
                search(
                    term,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
            .fetchSingle { it.value1() }

        val bookWithInfo = getBookWithInfo()
        val infos = bookWithInfo
            .innerJoin(ids).on(bookWithInfo[BOOK.ID]!!.eq(BOOK.ID))
            .fetch { BookWithInfo(it) }
        val hasMore = infos.size > pageSize
        return Triple(infos.take(pageSize), hasMore, total)
    }

    private fun Table<*>.match(text: Typed<String>): Condition {
        return condition("$name MATCH {0}", text)
    }


    fun authorNameStarts(prefix: String, trim: Boolean): List<Pair<String, Long>> {
        val primaryNamesAreNulls = AUTHOR.LAST_NAME.isNull
            .and(AUTHOR.MIDDLE_NAME.isNull)
            .and(AUTHOR.FIRST_NAME.isNull)

        val fullName = trim(
            concat(
                if_(AUTHOR.LAST_NAME.isNotNull, AUTHOR.LAST_NAME.concat(" "), ""),
                if_(AUTHOR.FIRST_NAME.isNotNull, AUTHOR.FIRST_NAME.concat(" "), ""),
                if_(AUTHOR.MIDDLE_NAME.isNotNull, AUTHOR.MIDDLE_NAME.concat(" "), ""),
                if_(
                    AUTHOR.NICKNAME.isNull, "", concat(
                        if_(primaryNamesAreNulls, "", "("),
                        AUTHOR.NICKNAME,
                        if_(primaryNamesAreNulls, "", ")"),
                    )
                )
            )
        )

        val toSelect = (
                if (trim) substring(fullName, 1, prefix.length + 1)
                else fullName).`as`("term")
        val second = (if (trim) count(AUTHOR.ID).cast(Long::class.java) else AUTHOR.ID).`as`("number")
        return create
            .selectDistinct(toSelect, second)
            .from(AUTHOR)
            .where(toSelect.isNotNull, toSelect.ne(""), toSelect.startsWith(prefix))
            .groupBy(toSelect)
            .orderBy(toSelect)
            .fetch { it[toSelect] to it[second] }
    }

    fun seriesNameStarts(prefix: String, trim: Boolean): List<SequenceShortInfo> {
        val fst = (
                if (trim) substring(BOOK.SEQUENCE, 1, prefix.length + 1)
                else BOOK.SEQUENCE
                )
        val snd = count(BOOK.ID)
        val third = if (trim) field(length(BOOK.SEQUENCE).eq(prefix.length)) else value(true)
        val fourth = if_(
            length(BOOK.SEQUENCE).eq(prefix.length).or(value(prefix.length == 5)),
            BOOK.SEQID,
            castNull(Int::class.javaObjectType)
        )
        return create
            .selectDistinct(fst, snd, third, fourth)
            .from(BOOK)
            .where(fst.isNotNull, trim(fst).ne(""), fst.startsWith(prefix))
            .groupBy(fst)
            .orderBy(fst)
            .fetch { SequenceShortInfo(it[fst], it[snd], it[third], it[fourth]) }
    }

    data class SequenceShortInfo(val seqName: String, val bookCount: Int, val fullName: Boolean, val seqId: Int?)

    fun latestBooks(page: Int = 0) =
        getBookWithInfo()
            .orderBy(BOOK.ADDED.desc())
            .limit(21)
            .offset(page * 21)
            .fetch()

    fun bookInfo(bookId: Long): Record3<BookRecord, List<AuthorRecord>, MutableList<GenreRecord>> {
        return getBookWithInfo()
            .where(BOOK.ID.eq(bookId))
            .fetchSingle()

    }

    fun allBooksByAuthor(authorId: Long, page: Int, pageSize: Int): Pair<Int, MutableList<BookWithInfo>> {
        val total = create.select(countDistinct(BOOK.ID))
            .from(BOOK)
            .where(BOOK.bookAuthor.AUTHOR_ID.eq(authorId))
            .fetchSingle().value1()
        return total to getBookWithInfo()
            .where(BOOK.bookAuthor.AUTHOR_ID.eq(authorId))
            .orderBy(BOOK.NAME)
            .limit(pageSize)
            .offset(pageSize * page)
            .fetch { BookWithInfo(it) }
    }

    fun booksWithoutSeriesByAuthorId(authorId: Long): List<BookWithInfo> {
        return getBookWithInfo()
            .where(BOOK.bookAuthor.AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNull)
            .orderBy(BOOK.NAME)
            .fetch { BookWithInfo(it) }
    }

    fun booksBySeriesAndAuthor(
        seriesId: Long,
        authorId: Long,
    ): List<BookWithInfo> = getBookWithInfo()
        .where(BOOK.SEQID.eq(seriesId.toInt()), BOOK.bookAuthor.AUTHOR_ID.eq(authorId))
        .orderBy(BOOK.SEQUENCE_NUMBER.asc().nullsLast(), BOOK.NAME)
        .fetch { BookWithInfo(it) }

    fun booksBySeriesName(seqId: Int): Sequence<BookWithInfo> = getBookWithInfo()
        .where(BOOK.SEQID.eq(seqId))
        .fetch { BookWithInfo(it) }
        .asSequence()

    fun genres(): List<Triple<Long, String, Int>> {
        val bookCount = count(GENRE.bookGenre.BOOK_ID)
        val genreName = min(GENRE.NAME)
        val map = create
            .select(GENRE.ID, genreName, bookCount)
            .from(GENRE)
            .groupBy(GENRE.ID)
            .fetch { Triple(it[GENRE.ID]!!, it[genreName]!!, it[bookCount]) }
            .groupBy { genres.containsKey(it.second) }
        val def = map[true]?.map { (a, b, c) -> Triple(a, genres[b]!!, c) }?.sortedBy { it.second } ?: listOf()
        val undef = map[false]?.sortedBy { it.second } ?: listOf()
        return def + undef
    }

    fun genreName(genreId: Long): String? {
        return create
            .select(GENRE.NAME)
            .from(GENRE)
            .where(GENRE.ID.isNotDistinctFrom(genreId))
            .fetchOne { genreName(it) }
    }

    fun genreAuthors(genreId: Long): List<Pair<Long, String>> {
        val book = AUTHOR.bookAuthor.book
        val genre = book.bookGenre.genre
        return create
            .selectDistinct(AUTHOR.ID, fullName)
            .from(AUTHOR)
            .innerJoin(book)
            .innerJoin(genre)
            .where(genre.ID.eq(genreId))
            .orderBy(fullName)
            .fetch { Pair(it[AUTHOR.ID]!!, it[fullName]) }
            .toList()
    }

    private fun genreName(it: Record): String =
        genres[it[GENRE.NAME]] ?: it[GENRE.NAME]!!

    fun booksByGenreAndAuthor(genreId: Long, authorId: Long): Pair<String, List<BookWithInfo>> {
        val authorName = authorName(authorId)
        return authorName to create
            .selectDistinct(
                bookById,
                bookAuthors,
                bookGenres,
            )
            .from(BOOK)
            .where(BOOK.bookGenre.GENRE_ID.eq(genreId), BOOK.bookAuthor.AUTHOR_ID.eq(authorId))
            .orderBy(BOOK.NAME)
            .fetch(::BookWithInfo)
            .toList()

    }

    fun booksInGenre(genreId: Long, page: Int, pageSize: Int): Pair<Int, List<BookWithInfo>> {
        val total = create
            .select(countDistinct(GENRE.bookGenre.BOOK_ID))
            .from(GENRE)
            .where(GENRE.ID.eq(genreId))
            .fetchSingle().value1()

        return total to create
            .selectDistinct(
                bookById,
                bookAuthors,
                bookGenres,
            )
            .from(BOOK)
            .where(BOOK.bookGenre.GENRE_ID.eq(genreId))
            .orderBy(BOOK.NAME)
            .limit(pageSize)
            .offset(page * pageSize)
            .fetch(::BookWithInfo)
            .toList()
    }
}

