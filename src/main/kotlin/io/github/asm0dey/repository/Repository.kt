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

import io.github.asm0dey.opdsko.jooq.Tables
import io.github.asm0dey.opdsko.jooq.Tables.BOOK
import io.github.asm0dey.opdsko.jooq.Tables.BOOK_AUTHOR
import io.github.asm0dey.opdsko.jooq.tables.pojos.Author
import io.github.asm0dey.opdsko.jooq.tables.pojos.Book
import io.github.asm0dey.service.BookWithInfo
import org.jooq.*
import org.jooq.impl.DSL
import java.time.LocalDateTime

class Repository(val create: DSLContext) {
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
        DSL.selectDistinct(Tables.AUTHOR.asterisk())
            .from(Tables.AUTHOR)
            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.AUTHOR_ID.eq(Tables.AUTHOR.ID))
            .where(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
    ).`as`("authors").convertFrom { it.into(Author::class.java) }

    private val bookGenres = DSL.multiset(
        DSL.selectDistinct(Tables.GENRE.NAME)
            .from(Tables.GENRE)
            .innerJoin(Tables.BOOK_GENRE).on(Tables.BOOK_GENRE.GENRE_ID.eq(Tables.GENRE.ID))
            .where(Tables.BOOK_GENRE.BOOK_ID.eq(BOOK.ID))
    ).`as`("genres").convertFrom { it.toList() }

    private fun Result<Record1<String>>.toList(): List<String> =
        collect(Records.intoList())

    private val bookById = run {
        val bookAlias = io.github.asm0dey.opdsko.jooq.tables.Book("b")
        DSL.multiset(DSL.selectFrom(bookAlias).where(bookAlias.ID.eq(BOOK.ID)))
            .`as`("book")
            .convertFrom { it.into(Book::class.java) }

    }

    fun getBookWithInfo(): SelectJoinStep<Record4<Long, List<Book>, List<Author>, List<String>>> = create
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

    fun bookPath(bookId: Long) =
        create.select(BOOK.PATH).from(BOOK).where(BOOK.ID.eq(bookId)).fetchSingle { it[BOOK.PATH] }


    fun authorName(authorId: Long): String {
        val fullname = DSL.concat(
            DSL.coalesce(Tables.AUTHOR.LAST_NAME, ""),
            DSL.if_(
                Tables.AUTHOR.FIRST_NAME.isNotNull,
                DSL.if_(
                    Tables.AUTHOR.LAST_NAME.isNotNull,
                    DSL.concat(", ", Tables.AUTHOR.FIRST_NAME),
                    Tables.AUTHOR.FIRST_NAME
                ),
                ""
            ),
            DSL.if_(
                Tables.AUTHOR.MIDDLE_NAME.isNotNull,
                DSL.if_(
                    Tables.AUTHOR.FIRST_NAME.isNotNull.or(Tables.AUTHOR.LAST_NAME.isNotNull),
                    DSL.concat(" ", Tables.AUTHOR.MIDDLE_NAME),
                    Tables.AUTHOR.MIDDLE_NAME
                ),
                ""
            ),
            DSL.if_(
                Tables.AUTHOR.NICKNAME.isNotNull,
                DSL.if_(
                    Tables.AUTHOR.FIRST_NAME.isNull.and(Tables.AUTHOR.LAST_NAME.isNotNull)
                        .and(Tables.AUTHOR.MIDDLE_NAME.isNotNull),
                    Tables.AUTHOR.NICKNAME,
                    DSL.concat(DSL.`val`(" ("), Tables.AUTHOR.NICKNAME, DSL.`val`(")"))
                ),
                ""
            ),
        )
        return create
            .select(fullname)
            .from(Tables.AUTHOR)
            .where(Tables.AUTHOR.ID.eq(authorId))
            .fetchSingle { it[fullname] }
    }

    fun searchBookByText(term: String, page: Int, pageSize: Int): Pair<List<BookWithInfo>, Boolean> {
        val ftsId = Tables.BOOKS_FTS.rowid().cast(Long::class.java).`as`("fts_id")
        val ids = create
            .select(ftsId)
            .from(Tables.BOOKS_FTS).where(Tables.BOOKS_FTS.match(DSL.value(term)))
            .orderBy(DSL.field("bm25(books_fts)"))
            .limit(pageSize + 1)
            .offset(page * pageSize)
        val infos = getBookWithInfo()
            .innerJoin(ids).on(ftsId.eq(BOOK.ID))
            .fetch { BookWithInfo(it) }
        val hasMore = infos.size > pageSize
        return infos.take(pageSize) to hasMore
    }

    private fun Table<*>.match(text: Typed<String>): Condition {
        return DSL.condition("$name MATCH {0}", text)
    }

    fun authorNameStarts(prefix: String, trim: Boolean): List<Pair<String, Long>> {
        val primaryNamesAreNulls = Tables.AUTHOR.LAST_NAME.isNull
            .and(Tables.AUTHOR.MIDDLE_NAME.isNull)
            .and(Tables.AUTHOR.FIRST_NAME.isNull)
        val fullName = DSL.trim(
            DSL.concat(
                DSL.if_(Tables.AUTHOR.LAST_NAME.isNotNull, Tables.AUTHOR.LAST_NAME.concat(" "), ""),
                DSL.if_(Tables.AUTHOR.FIRST_NAME.isNotNull, Tables.AUTHOR.FIRST_NAME.concat(" "), ""),
                DSL.if_(Tables.AUTHOR.MIDDLE_NAME.isNotNull, Tables.AUTHOR.MIDDLE_NAME.concat(" "), ""),
                DSL.if_(
                    Tables.AUTHOR.NICKNAME.isNull, "", DSL.concat(
                        DSL.if_(primaryNamesAreNulls, "", "("),
                        Tables.AUTHOR.NICKNAME,
                        DSL.if_(primaryNamesAreNulls, "", ")"),
                    )
                )
            )
        )

        val toSelect = (
                if (trim) DSL.substring(fullName, 1, prefix.length + 1)
                else fullName).`as`("term")
        val second = (if (trim) DSL.count(Tables.AUTHOR.ID).cast(Long::class.java) else Tables.AUTHOR.ID).`as`("number")
        return create.selectDistinct(
            toSelect, second
        )
            .from(Tables.AUTHOR)
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

    fun bookInfo(bookId: Long): Record4<Long, List<Book>, List<Author>, List<String>> {
        return getBookWithInfo()
            .where(BOOK.ID.eq(bookId))
            .fetchSingle()

    }

    fun allBooksByAuthor(authorId: Long): List<BookWithInfo> {
        return getBookWithInfo()
            .innerJoin(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
            .orderBy(BOOK.NAME)
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
}

