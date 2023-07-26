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
import kotlinx.html.SELECT
import org.jooq.*
import org.jooq.impl.DSL
import java.time.LocalDateTime

class Repository(val create: DSLContext) {
    private val genres by lazy { genreNames() }
    fun seriesByAuthorId(authorId: Long): Map<String, Pair<LocalDateTime, Int>> {
        val latestBookInSeq = DSL.max(BOOK.ADDED)
        val booksInSeries = DSL.count(BOOK.ID)
        return create.select(BOOK.SEQUENCE, latestBookInSeq, booksInSeries)
            .from(BOOK)
            .innerJoin(BOOK_AUTHOR).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNotNull)
            .groupBy(BOOK.SEQUENCE)
            .orderBy(BOOK.SEQUENCE, latestBookInSeq.desc())
            .fetch {
                it[BOOK.SEQUENCE] to (it[latestBookInSeq] to it[booksInSeries])
            }
            .toMap()
    }

    private val bookAuthors = DSL.multiset(
        DSL.selectDistinct(AUTHOR.asterisk())
            .from(AUTHOR)
            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.AUTHOR_ID.eq(AUTHOR.ID))
            .where(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
    ).`as`("authors").convertFrom { it.into(Author::class.java) }

    private val bookGenres = DSL.multiset(
        DSL.selectDistinct(GENRE.NAME, GENRE.ID)
            .from(GENRE)
            .innerJoin(BOOK_GENRE).on(BOOK_GENRE.GENRE_ID.eq(GENRE.ID))
            .where(BOOK_GENRE.BOOK_ID.eq(BOOK.ID))
    ).`as`("genres").convertFrom { it.toList() }

    private fun Result<Record1<String>>.toList(): List<String> =
        collect(Records.intoList())

    private val bookById = run {
        val bookAlias = io.github.asm0dey.opdsko.jooq.tables.Book("b")
        DSL.multiset(DSL.selectFrom(bookAlias).where(bookAlias.ID.eq(BOOK.ID)))
            .`as`("book")
            .convertFrom { it.into(Book::class.java) }

    }

    fun getBookWithInfo(): SelectJoinStep<Record4<Long, MutableList<Book>, MutableList<Author>, List<Record2<String, Long>>>> =
        create
            .selectDistinct(
                BOOK.ID,
                bookById,
                bookAuthors,
                bookGenres,
            )
            .from(BOOK)

    fun seriesNumberByAuthor(authorId: Long): Pair<Int, Int> {
        val notnull = DSL.count(BOOK.ID).filterWhere(BOOK.SEQUENCE.isNotNull).`as`("x")
        val isnull = DSL.count(BOOK.ID).filterWhere(BOOK.SEQUENCE.isNull).`as`("y")
        return create
            .select(
                notnull,
                isnull,
            )
            .from(BOOK_AUTHOR)
            .innerJoin(BOOK).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
            .limit(1)
            .fetchSingle { it[notnull] to it[isnull] }
    }

    fun latestAuthorUpdate(authorId: Long): LocalDateTime {
        return create
            .select(BOOK.ADDED)
            .from(BOOK)
            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
            .orderBy(BOOK.ADDED.desc())
            .limit(1)
            .fetchSingle { it[BOOK.ADDED] }
    }

    fun bookPath(bookId: Long): Pair<String, String?> =
        create.select(BOOK.PATH, BOOK.ZIP_FILE).from(BOOK).where(BOOK.ID.eq(bookId))
            .fetchSingle { it[BOOK.PATH] to it[BOOK.ZIP_FILE] }

    val fullname = DSL.concat(
        DSL.coalesce(AUTHOR.LAST_NAME, ""),
        DSL.if_(
            AUTHOR.FIRST_NAME.isNotNull,
            DSL.if_(
                AUTHOR.LAST_NAME.isNotNull,
                DSL.concat(", ", AUTHOR.FIRST_NAME),
                AUTHOR.FIRST_NAME
            ),
            ""
        ),
        DSL.if_(
            AUTHOR.MIDDLE_NAME.isNotNull,
            DSL.if_(
                AUTHOR.FIRST_NAME.isNotNull.or(AUTHOR.LAST_NAME.isNotNull),
                DSL.concat(" ", AUTHOR.MIDDLE_NAME),
                AUTHOR.MIDDLE_NAME
            ),
            ""
        ),
        DSL.if_(
            AUTHOR.NICKNAME.isNotNull,
            DSL.if_(
                AUTHOR.FIRST_NAME.isNull.and(AUTHOR.LAST_NAME.isNotNull)
                    .and(AUTHOR.MIDDLE_NAME.isNotNull),
                AUTHOR.NICKNAME,
                DSL.concat(DSL.`val`(" ("), AUTHOR.NICKNAME, DSL.`val`(")"))
            ),
            ""
        ),
    )

    fun authorName(authorId: Long): String {
        return create
            .select(fullname)
            .from(AUTHOR)
            .where(AUTHOR.ID.eq(authorId))
            .fetchSingle { it[fullname] }
    }

    fun searchBookByText(term: String, page: Int, pageSize: Int): Triple<List<BookWithInfo>, Boolean, Int> {
        val ftsId = BOOKS_FTS.rowid().cast(Long::class.java).`as`("fts_id")
        val total = create
            .select(DSL.count(BOOKS_FTS.rowid()))
            .from(BOOKS_FTS)
            .where(BOOKS_FTS.match(DSL.value(term)))
            .fetchSingle().value1()
        val ids = create
            .select(ftsId)
            .from(BOOKS_FTS).where(BOOKS_FTS.match(DSL.value(term)))
            .orderBy(DSL.field("bm25(books_fts)"))
            .limit(pageSize + 1)
            .offset(page * pageSize)
        val infos = getBookWithInfo()
            .innerJoin(ids).on(ftsId.eq(BOOK.ID))
            .fetch { BookWithInfo(it) }
        val hasMore = infos.size > pageSize
        return Triple(infos.take(pageSize), hasMore, total)
    }

    private fun Table<*>.match(text: Typed<String>): Condition {
        return DSL.condition("$name MATCH {0}", text)
    }


    fun authorNameStarts(prefix: String, trim: Boolean): List<Pair<String, Long>> {
        val primaryNamesAreNulls = AUTHOR.LAST_NAME.isNull
            .and(AUTHOR.MIDDLE_NAME.isNull)
            .and(AUTHOR.FIRST_NAME.isNull)

        val fullName = DSL.trim(
            DSL.concat(
                DSL.if_(AUTHOR.LAST_NAME.isNotNull, AUTHOR.LAST_NAME.concat(" "), ""),
                DSL.if_(AUTHOR.FIRST_NAME.isNotNull, AUTHOR.FIRST_NAME.concat(" "), ""),
                DSL.if_(AUTHOR.MIDDLE_NAME.isNotNull, AUTHOR.MIDDLE_NAME.concat(" "), ""),
                DSL.if_(
                    AUTHOR.NICKNAME.isNull, "", DSL.concat(
                        DSL.if_(primaryNamesAreNulls, "", "("),
                        AUTHOR.NICKNAME,
                        DSL.if_(primaryNamesAreNulls, "", ")"),
                    )
                )
            )
        )

        val toSelect = (
                if (trim) DSL.substring(fullName, 1, prefix.length + 1)
                else fullName).`as`("term")
        val second = (if (trim) DSL.count(AUTHOR.ID).cast(Long::class.java) else AUTHOR.ID).`as`("number")
        return create.selectDistinct(
            toSelect, second
        )
            .from(AUTHOR)
            .where(toSelect.isNotNull, toSelect.ne(""), toSelect.startsWith(prefix))
            .groupBy(toSelect)
            .orderBy(toSelect)
            .fetch { it[toSelect] to it[second] }
    }

    fun seriesNameStarts(prefix: String, trim: Boolean): List<Triple<String, Int, Boolean>> {
        val fst = (
                if (trim) DSL.substring(BOOK.SEQUENCE, 1, prefix.length + 1)
                else BOOK.SEQUENCE
                )
            .`as`("term")
        val snd = DSL.count(BOOK.ID).`as`("cnt")
        val third = if (trim) DSL.field(DSL.length(BOOK.SEQUENCE).eq(prefix.length)) else DSL.value(true)
        return create
            .selectDistinct(fst, snd, third)
            .from(BOOK)
            .where(fst.isNotNull, DSL.trim(fst).ne(""), fst.startsWith(prefix))
            .groupBy(fst)
            .orderBy(fst)
            .fetch { Triple(it[fst], it[snd], it[third]) }
    }

    fun latestBooks() =
        getBookWithInfo()
            .orderBy(BOOK.ADDED.desc())
            .limit(20)
            .fetch()

    fun bookInfo(bookId: Long): Record4<Long, MutableList<Book>, MutableList<Author>, List<Record2<String, Long>>> {
        return getBookWithInfo()
            .where(BOOK.ID.eq(bookId))
            .fetchSingle()

    }

    fun allBooksByAuthor(authorId: Long, page: Int, pageSize: Int): Pair<Int, MutableList<BookWithInfo>> {
        val total = create.select(DSL.countDistinct(BOOK.ID))
            .from(BOOK)
            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
            .fetchSingle().value1()
        return total to getBookWithInfo()
            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
            .orderBy(BOOK.NAME)
            .limit(pageSize)
            .offset(pageSize * page)
            .fetch { BookWithInfo(it) }
    }

    fun booksWithoutSeriesByAuthorId(authorId: Long): List<BookWithInfo> {
        return getBookWithInfo()
            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId), BOOK.SEQUENCE.isNull)
            .orderBy(BOOK.NAME)
            .fetch { BookWithInfo(it) }
    }

    fun booksBySeriesAndAuthor(
        seriesName: String,
        authorId: Long,
    ): List<BookWithInfo> = getBookWithInfo()
        .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
        .where(BOOK.SEQUENCE.eq(seriesName), BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
        .orderBy(BOOK.SEQUENCE_NUMBER.asc().nullsLast(), BOOK.NAME)
        .fetch { BookWithInfo(it) }

    fun booksBySeriesName(name: String) = getBookWithInfo()
        .where(BOOK.SEQUENCE.eq(name))
        .fetch { BookWithInfo(it) }
        .asSequence()

    fun genres(): List<Triple<Long, String, Int>> {
        val bookCount = DSL.count(BOOK.ID)
        val genreName = DSL.min(GENRE.NAME)
        val map = create
            .select(GENRE.ID, genreName, bookCount)
            .from(GENRE)
            .leftJoin(BOOK_GENRE).on(GENRE.ID.eq(BOOK_GENRE.GENRE_ID))
            .innerJoin(BOOK).on(BOOK_GENRE.BOOK_ID.eq(BOOK.ID))
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
        return create
            .selectDistinct(AUTHOR.ID, fullname)
            .from(GENRE)
            .innerJoin(BOOK_GENRE).on(BOOK_GENRE.GENRE_ID.eq(GENRE.ID))
            .innerJoin(BOOK).on(BOOK_GENRE.BOOK_ID.eq(BOOK.ID))
            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
            .innerJoin(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
            .where(GENRE.ID.eq(genreId))
            .orderBy(fullname)
            .fetch { Pair(it[AUTHOR.ID], it[fullname]) }
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
            )
            .from(GENRE)
            .innerJoin(BOOK_GENRE).on(BOOK_GENRE.GENRE_ID.eq(GENRE.ID))
            .innerJoin(BOOK).on(BOOK_GENRE.BOOK_ID.eq(BOOK.ID))
            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
            .innerJoin(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
            .where(GENRE.ID.eq(genreId), AUTHOR.ID.eq(authorId))
            .orderBy(BOOK.NAME)
            .fetch(::BookWithInfo)
            .toList()

    }

    fun booksInGenre(genreId: Long, page: Int, pageSize: Int): Pair<Int, List<BookWithInfo>> {
        val total = create
            .select(DSL.countDistinct(BOOK.ID))
            .from(GENRE)
            .innerJoin(BOOK_GENRE).on(BOOK_GENRE.GENRE_ID.eq(GENRE.ID))
            .innerJoin(BOOK).on(BOOK_GENRE.BOOK_ID.eq(BOOK.ID))
            .where(GENRE.ID.eq(genreId))
            .fetchSingle().value1()

        return total to create
            .selectDistinct(
                BOOK.ID,
                bookById,
                bookAuthors,
                bookGenres,
            )
            .from(GENRE)
            .innerJoin(BOOK_GENRE).on(BOOK_GENRE.GENRE_ID.eq(GENRE.ID))
            .innerJoin(BOOK).on(BOOK_GENRE.BOOK_ID.eq(BOOK.ID))
            .where(GENRE.ID.eq(genreId))
            .orderBy(BOOK.NAME)
            .limit(pageSize)
            .offset(page * pageSize)
            .fetch(::BookWithInfo)
            .toList()
    }
}

