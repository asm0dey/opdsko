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
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.references.SEARCH
import io.github.asm0dey.opdsko.jooq.public.tables.Author.Companion.AUTHOR
import io.github.asm0dey.opdsko.jooq.public.tables.Book.Companion.BOOK
import io.github.asm0dey.opdsko.jooq.public.tables.BookAuthor.Companion.BOOK_AUTHOR
import io.github.asm0dey.opdsko.jooq.public.tables.Genre.Companion.GENRE
import io.github.asm0dey.opdsko.jooq.public.tables.records.AuthorRecord
import io.github.asm0dey.opdsko.jooq.public.tables.records.BookRecord
import io.github.asm0dey.opdsko.jooq.public.tables.records.GenreRecord
import io.github.asm0dey.service.BookWithInfo
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Record3
import org.jooq.impl.DSL.*
import org.jooq.kotlin.get
import org.jooq.kotlin.mapping
import org.jooq.kotlin.or
import java.time.OffsetDateTime

class Repository(val create: DSLContext) {
    private val genres by lazy { genreNames() }
    fun seriesByAuthorId(authorId: Long): Map<Pair<Int, String>, Pair<OffsetDateTime, Int>> {
        val latestBookInSeq = max(BOOK.ADDED)
        val booksInSeries = count(BOOK.ID)
        return create.select(BOOK.SEQUENCE, latestBookInSeq, booksInSeries, min(BOOK.SEQID))
            .from(BOOK)
            .innerJoin(BOOK.bookAuthor())
            .where(BOOK.bookAuthor().AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNotNull)
            .groupBy(BOOK.SEQUENCE)
            .orderBy(BOOK.SEQUENCE, latestBookInSeq.desc())
            .fetch {
                (it.value4()!! to it[BOOK.SEQUENCE]!!) to (it[latestBookInSeq]!! to it[booksInSeries])
            }
            .toMap()
    }

    private val bookAuthors = multiset(
        selectDistinct(AUTHOR)
            .from(AUTHOR)
            .innerJoin(AUTHOR.bookAuthor)
            .where(AUTHOR.bookAuthor().BOOK_ID.eq(BOOK.ID))
    ).`as`("authors").mapping { it }

    private val bookGenres = multiset(
        select(GENRE)
            .from(GENRE)
            .innerJoin(GENRE.bookGenre)
            .where(GENRE.bookGenre.BOOK_ID.eq(BOOK.ID))
    ).`as`("genres").mapping { it }

    private fun getBookWithInfo() =
        create
            .select(
//                BOOK.ID,
                BOOK,
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


    fun authorName(authorId: Long): String {
        return create
            .select(AUTHOR.FULL_NAME)
            .from(AUTHOR)
            .where(AUTHOR.ID.eq(authorId))
            .fetchSingle { it[AUTHOR.FULL_NAME] }
    }

    fun searchBookByText(term: String, page: Int, pageSize: Int): Triple<List<BookWithInfo>, Boolean, Int> {
        val ids = create.select(
            SEARCH.ID,
//            bookById
        )
            .from(
                search(
                    term,
                    page * pageSize,
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
            count(SEARCH.ID),
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
            .innerJoin(ids).on(BOOK.ID.eq(ids[SEARCH.ID]))
            .fetch { BookWithInfo(it) }
        val hasMore = infos.size > pageSize
        return Triple(infos.take(pageSize), hasMore, total)
    }

    fun authorNameStarts(prefix: String, trim: Boolean): List<Pair<String, Long>> {
        val outPrefix = substring(AUTHOR.FULL_NAME, 1, prefix.length + 1)
        val toSelect = (if (trim) outPrefix else AUTHOR.FULL_NAME)
        val second = (if (trim) count(AUTHOR.ID).cast(Long::class.java) else min(AUTHOR.ID)).`as`("number")
        return create
            .selectDistinct(toSelect.`as`("res"), second)
            .from(AUTHOR)
            .where(toSelect.isNotNull, toSelect.ne(""), toSelect.startsWith(prefix))
            .groupBy(toSelect.`as`("res"))
            .orderBy(toSelect.`as`("res"))
            .fetch { it.value1() to it[second] }
    }

    fun seriesNameStarts(prefix: String, trim: Boolean): List<SequenceShortInfo> {
        val starts = name("starts").fields("prefix").`as`(
            if (trim) {
                val substring = substring(BOOK.SEQUENCE, 1, prefix.length + 1)
                selectDistinct(substring).from(BOOK).where(length(substring).gt(0))
            } else selectDistinct(BOOK.SEQUENCE).from(BOOK)
        )
        val foundPrefix = starts.field("prefix", String::class.java)
        val final = countDistinct(BOOK.SEQUENCE).eq(1).and(length(min(BOOK.SEQUENCE)).eq(prefix.length))
        return create
            .with(starts)
            .select(
                foundPrefix,
                if_(final.or(value(prefix.length == 5)), countDistinct(BOOK.ID), countDistinct(BOOK.SEQUENCE)),
                if (trim) final
                else value(true),
                if_(
                    value(prefix.length == 5).or(final),
                    min(BOOK.SEQID),
                    `val`(null, Int::class.java)
                ),
            )
            .from(BOOK)
            .innerJoin(starts).on(
                if (prefix.length == 5) BOOK.SEQUENCE.eq(foundPrefix) else BOOK.SEQUENCE.startsWith(foundPrefix)
            )
            .where(BOOK.SEQUENCE.startsWith(prefix), length(foundPrefix).gt(0))
            .groupBy(foundPrefix)
            .orderBy(foundPrefix)
            .fetch { SequenceShortInfo(it.value1(), it.value2(), it.value3(), it.value4()) }
    }

    data class SequenceShortInfo(val seqName: String, val bookCount: Int, val fullName: Boolean, val seqId: Int?)

    fun latestBooks(page: Int = 0) =
        getBookWithInfo()
            .orderBy(BOOK.ADDED.desc())
            .limit(21)
            .offset(page * 21)
            .fetch()

    fun bookInfo(bookId: Long): Record3<BookRecord, List<AuthorRecord>, List<GenreRecord>> {
        return getBookWithInfo()
            .where(BOOK.ID.eq(bookId))
            .fetchSingle()

    }

    fun allBooksByAuthor(authorId: Long, page: Int, pageSize: Int): Pair<Int, MutableList<BookWithInfo>> {
        val total = create.select(countDistinct(BOOK.ID))
            .from(BOOK)
            .innerJoin(BOOK.bookAuthor)
            .where(BOOK.bookAuthor.AUTHOR_ID.eq(authorId))
            .fetchSingle().value1()
        return total to getBookWithInfo()
            .innerJoin(BOOK.bookAuthor)
            .where(BOOK.bookAuthor.AUTHOR_ID.eq(authorId))
            .orderBy(BOOK.NAME)
            .limit(pageSize)
            .offset(pageSize * page)
            .fetch { BookWithInfo(it) }
    }

    fun booksWithoutSeriesByAuthorId(authorId: Long): List<BookWithInfo> {
        return getBookWithInfo()
            .innerJoin(BOOK.bookAuthor)
            .where(BOOK.bookAuthor.AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNull)
            .orderBy(BOOK.NAME)
            .fetch { BookWithInfo(it) }
    }

    fun booksBySeriesAndAuthor(
        seriesId: Long,
        authorId: Long,
    ): List<BookWithInfo> = getBookWithInfo()
        .innerJoin(BOOK.bookAuthor)
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
            .innerJoin(GENRE.bookGenre)
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
        return create
            .selectDistinct(AUTHOR.ID, AUTHOR.FULL_NAME)
            .from(AUTHOR)
            .innerJoin(AUTHOR.bookAuthor)
            .innerJoin(AUTHOR.bookAuthor.book)
            .innerJoin(AUTHOR.bookAuthor.book.bookGenre)
            .where(AUTHOR.bookAuthor.book.bookGenre.GENRE_ID.eq(genreId))
            .orderBy(AUTHOR.FULL_NAME)
            .fetch { Pair(it[AUTHOR.ID]!!, it[AUTHOR.FULL_NAME]!!) }
            .toList()
    }

    private fun genreName(it: Record): String =
        genres[it[GENRE.NAME]] ?: it[GENRE.NAME]!!

    fun booksByGenreAndAuthor(genreId: Long, authorId: Long): Pair<String, List<BookWithInfo>> {
        val authorName = authorName(authorId)
        return authorName to create
            .selectDistinct(
                BOOK.NAME,
                BOOK,
                bookAuthors,
                bookGenres,
            )
            .from(BOOK)
            .innerJoin(BOOK.bookGenre)
            .innerJoin(BOOK.bookAuthor)
            .where(BOOK.bookGenre.GENRE_ID.eq(genreId), BOOK.bookAuthor.AUTHOR_ID.eq(authorId))
            .orderBy(BOOK.NAME)
            .fetch { BookWithInfo(it.into(it.field2(), it.field3(), it.field4())) }
            .toList()

    }

    fun booksInGenre(genreId: Long, page: Int, pageSize: Int): Pair<Int, List<BookWithInfo>> {
        val total = create
            .select(countDistinct(GENRE.bookGenre.BOOK_ID))
            .from(GENRE)
            .innerJoin(GENRE.bookGenre)
            .where(GENRE.ID.eq(genreId))
            .fetchSingle().value1()

        return total to create
            .selectDistinct(
                BOOK.NAME,
                BOOK,
                bookAuthors,
                bookGenres,
            )
            .from(BOOK)
            .innerJoin(BOOK.bookGenre)
            .where(BOOK.bookGenre.GENRE_ID.eq(genreId))
            .orderBy(BOOK.NAME)
            .limit(pageSize)
            .offset(page * pageSize)
            .fetch { BookWithInfo(it.into(it.field2(), it.field3(), it.field4())) }
            .toList()
    }
}

