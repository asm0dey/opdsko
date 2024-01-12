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
import io.github.asm0dey.opdsko.jooq.Tables.*
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author
import io.github.asm0dey.opdsko.jooq.tables.pojos.Book
import io.github.asm0dey.service.BookWithInfo
import org.jooq.*
import org.jooq.impl.DSL.*
import java.time.LocalDateTime
import kotlin.sequences.Sequence

class Repository(val create: DSLContext) {
    private val genres by lazy { genreNames() }
    fun seriesByAuthorId(authorId: Long): Map<Pair<Int, String>, Pair<LocalDateTime, Int>> {
        val latestBookInSeq = max(BOOK.ADDED)
        val booksInSeries = count(BOOK.ID)
        return create.select(BOOK.SEQUENCE, latestBookInSeq, booksInSeries, BOOK.SEQID)
            .from(BOOK)
            .innerJoin(BOOK.author())
            .where(BOOK.author().ID.eq(authorId), BOOK.SEQUENCE.isNotNull)
            .groupBy(BOOK.SEQUENCE)
            .orderBy(BOOK.SEQUENCE, latestBookInSeq.desc())
            .fetch {
                (it[BOOK.SEQID] to it[BOOK.SEQUENCE]) to (it[latestBookInSeq] to it[booksInSeries])
            }
            .toMap()
    }

    private val bookAuthors = multiset(
        selectDistinct(AUTHOR.asterisk())
            .from(AUTHOR)
            .innerJoin(AUTHOR.book())
            .where(AUTHOR.book().ID.eq(BOOK.ID))
    ).`as`("authors").convertFrom { it.into(Author::class.java) }

    private val bookGenres = multiset(
        selectDistinct(GENRE.NAME, GENRE.ID)
            .from(GENRE)
            .innerJoin(GENRE.book())
            .where(GENRE.book().ID.eq(BOOK.ID))
    ).`as`("genres").convertFrom { it.toList() }

    private fun Result<Record1<String>>.toList(): List<String> =
        collect(Records.intoList())

    private val bookById = run {
        val bookAlias = io.github.asm0dey.opdsko.jooq.tables.Book("b")
        multiset(selectFrom(bookAlias).where(bookAlias.ID.eq(BOOK.ID)))
            .`as`("book")
            .convertFrom { it.into(Book::class.java) }

    }

    fun getBookWithInfo(): SelectJoinStep<Record5<Long, MutableList<Book>, MutableList<Author>, List<Record2<String, Long>>, String>> =
        create
            .selectDistinct(
                BOOK.ID,
                bookById,
                bookAuthors,
                bookGenres,
                BOOK.SEQUENCE
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
            .innerJoin(BOOK.author())
            .where(BOOK.author().ID.eq(authorId))
            .limit(1)
            .fetchSingle { it[notnull] to it[isnull] }
    }

    fun latestAuthorUpdate(authorId: Long): LocalDateTime {
        return create
            .select(BOOK.ADDED)
            .from(BOOK)
            .innerJoin(BOOK.author())
            .where(BOOK.author().ID.eq(authorId))
            .orderBy(BOOK.ADDED.desc())
            .limit(1)
            .fetchSingle { it[BOOK.ADDED] }
    }

    fun bookPath(bookId: Long): Pair<String, String?> =
        create.select(BOOK.PATH, BOOK.ZIP_FILE).from(BOOK).where(BOOK.ID.eq(bookId))
            .fetchSingle { it[BOOK.PATH] to it[BOOK.ZIP_FILE] }

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
        val ftsId = BOOKS_FTS.rowid().cast(Long::class.java).`as`("fts_id")
        val total = create
            .select(count(BOOKS_FTS.rowid()))
            .from(BOOKS_FTS)
            .where(BOOKS_FTS.match(value(term)))
            .fetchSingle().value1()
        val ids = create
            .select(ftsId)
            .from(BOOKS_FTS).where(BOOKS_FTS.match(value(term)))
            .orderBy(field("bm25(books_fts)"))
            .limit(pageSize + 1)
            .offset(page * pageSize)
        val infos = getBookWithInfo()
            .innerJoin(ids).on(ftsId.eq(BOOK.ID))
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

    fun bookInfo(bookId: Long): Record5<Long, MutableList<Book>, MutableList<Author>, List<Record2<String, Long>>, String> {
        return getBookWithInfo()
            .where(BOOK.ID.eq(bookId))
            .fetchSingle()

    }

    fun allBooksByAuthor(authorId: Long, page: Int, pageSize: Int): Pair<Int, MutableList<BookWithInfo>> {
        val total = create.select(countDistinct(BOOK.ID))
            .from(BOOK)
            .innerJoin(BOOK.author())
            .where(BOOK.author().ID.eq(authorId))
            .fetchSingle().value1()
        return total to getBookWithInfo()
            .innerJoin(BOOK.author())
            .where(BOOK.author().ID.eq(authorId))
            .orderBy(BOOK.NAME)
            .limit(pageSize)
            .offset(pageSize * page)
            .fetch { BookWithInfo(it) }
    }

    fun booksWithoutSeriesByAuthorId(authorId: Long): List<BookWithInfo> {
        return getBookWithInfo()
            .innerJoin(BOOK.author())
            .where(BOOK.author().ID.eq(authorId), BOOK.SEQUENCE.isNull)
            .orderBy(BOOK.NAME)
            .fetch { BookWithInfo(it) }
    }

    fun booksBySeriesAndAuthor(
        seriesId: Long,
        authorId: Long,
    ): List<BookWithInfo> = getBookWithInfo()
        .innerJoin(BOOK.author())
        .where(BOOK.SEQID.eq(seriesId.toInt()), BOOK.author().ID.eq(authorId))
        .orderBy(BOOK.SEQUENCE_NUMBER.asc().nullsLast(), BOOK.NAME)
        .fetch { BookWithInfo(it) }

    fun booksBySeriesName(seqId: Int): Sequence<BookWithInfo> = getBookWithInfo()
        .where(BOOK.SEQID.eq(seqId))
        .fetch { BookWithInfo(it) }
        .asSequence()

    fun genres(): List<Triple<Long, String, Int>> {
        val bookCount = count(GENRE.book().ID)
        val genreName = min(GENRE.NAME)
        val map = create
            .select(GENRE.ID, genreName, bookCount)
            .from(GENRE)
            .leftJoin(GENRE.book())
            .groupBy(GENRE.ID)
            .fetch { Triple(it[GENRE.ID], it[genreName], it[bookCount]) }
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
        val book = AUTHOR.book()
        val genre = book.genre()
        return create
            .selectDistinct(AUTHOR.ID, fullName)
            .from(AUTHOR)
            .innerJoin(book)
            .innerJoin(genre)
            .where(genre.ID.eq(genreId))
            .orderBy(fullName)
            .fetch { Pair(it[AUTHOR.ID], it[fullName]) }
            .toList()
    }

    private fun genreName(it: Record): String =
        genres[it[GENRE.NAME]] ?: it[GENRE.NAME]

    fun booksByGenreAndAuthor(genreId: Long, authorId: Long): Pair<String, List<BookWithInfo>> {
        val authorName = authorName(authorId)
        return authorName to create
            .selectDistinct(
                BOOK.ID,
                bookById,
                bookAuthors,
                bookGenres,
                BOOK.SEQUENCE
            )
            .from(BOOK)
            .innerJoin(BOOK.genre())
            .innerJoin(BOOK.author())
            .where(BOOK.genre().ID.eq(genreId), BOOK.author().ID.eq(authorId))
            .orderBy(BOOK.NAME)
            .fetch(::BookWithInfo)
            .toList()

    }

    fun booksInGenre(genreId: Long, page: Int, pageSize: Int): Pair<Int, List<BookWithInfo>> {
        val total = create
            .select(countDistinct(BOOK.ID))
            .from(BOOK)
            .innerJoin(BOOK.genre())
            .where(BOOK.genre().ID.eq(genreId))
            .fetchSingle().value1()

        return total to create
            .selectDistinct(
                BOOK.ID,
                bookById,
                bookAuthors,
                bookGenres,
                BOOK.SEQUENCE
            )
            .from(BOOK)
            .innerJoin(BOOK.genre())
            .where(BOOK.genre().ID.eq(genreId))
            .orderBy(BOOK.NAME)
            .limit(pageSize)
            .offset(page * pageSize)
            .fetch(::BookWithInfo)
            .toList()
    }
}

