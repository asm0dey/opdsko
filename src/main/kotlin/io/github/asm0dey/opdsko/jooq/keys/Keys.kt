/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.keys


import io.github.asm0dey.opdsko.jooq.tables.Author
import io.github.asm0dey.opdsko.jooq.tables.Book
import io.github.asm0dey.opdsko.jooq.tables.BookAuthor
import io.github.asm0dey.opdsko.jooq.tables.BookGenre
import io.github.asm0dey.opdsko.jooq.tables.Genre
import io.github.asm0dey.opdsko.jooq.tables.records.AuthorRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookAuthorRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookGenreRecord
import io.github.asm0dey.opdsko.jooq.tables.records.BookRecord
import io.github.asm0dey.opdsko.jooq.tables.records.GenreRecord

import org.jooq.ForeignKey
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal



// -------------------------------------------------------------------------
// UNIQUE and PRIMARY KEY definitions
// -------------------------------------------------------------------------

val AUTHOR__PK_AUTHOR: UniqueKey<AuthorRecord> = Internal.createUniqueKey(Author.AUTHOR, DSL.name("pk_author"), arrayOf(Author.AUTHOR.ID), true)
val BOOK__PK_BOOK: UniqueKey<BookRecord> = Internal.createUniqueKey(Book.BOOK, DSL.name("pk_book"), arrayOf(Book.BOOK.ID), true)
val BOOK_AUTHOR__PK_BOOK_AUTHOR: UniqueKey<BookAuthorRecord> = Internal.createUniqueKey(BookAuthor.BOOK_AUTHOR, DSL.name("pk_book_author"), arrayOf(BookAuthor.BOOK_AUTHOR.BOOK_ID, BookAuthor.BOOK_AUTHOR.AUTHOR_ID), true)
val BOOK_GENRE__PK_BOOK_GENRE: UniqueKey<BookGenreRecord> = Internal.createUniqueKey(BookGenre.BOOK_GENRE, DSL.name("pk_book_genre"), arrayOf(BookGenre.BOOK_GENRE.BOOK_ID, BookGenre.BOOK_GENRE.GENRE_ID), true)
val GENRE__PK_GENRE: UniqueKey<GenreRecord> = Internal.createUniqueKey(Genre.GENRE, DSL.name("pk_genre"), arrayOf(Genre.GENRE.ID), true)
val GENRE__UK_GENRE_1_134034859: UniqueKey<GenreRecord> = Internal.createUniqueKey(Genre.GENRE, DSL.name("uk_genre_1_134034859"), arrayOf(Genre.GENRE.NAME), true)

// -------------------------------------------------------------------------
// FOREIGN KEY definitions
// -------------------------------------------------------------------------

val BOOK_AUTHOR__FK_BOOK_AUTHOR_PK_AUTHOR: ForeignKey<BookAuthorRecord, AuthorRecord> = Internal.createForeignKey(BookAuthor.BOOK_AUTHOR, DSL.name("fk_book_author_pk_author"), arrayOf(BookAuthor.BOOK_AUTHOR.AUTHOR_ID), io.github.asm0dey.opdsko.jooq.keys.AUTHOR__PK_AUTHOR, arrayOf(Author.AUTHOR.ID), true)
val BOOK_AUTHOR__FK_BOOK_AUTHOR_PK_BOOK: ForeignKey<BookAuthorRecord, BookRecord> = Internal.createForeignKey(BookAuthor.BOOK_AUTHOR, DSL.name("fk_book_author_pk_book"), arrayOf(BookAuthor.BOOK_AUTHOR.BOOK_ID), io.github.asm0dey.opdsko.jooq.keys.BOOK__PK_BOOK, arrayOf(Book.BOOK.ID), true)
val BOOK_GENRE__FK_BOOK_GENRE_PK_BOOK: ForeignKey<BookGenreRecord, BookRecord> = Internal.createForeignKey(BookGenre.BOOK_GENRE, DSL.name("fk_book_genre_pk_book"), arrayOf(BookGenre.BOOK_GENRE.BOOK_ID), io.github.asm0dey.opdsko.jooq.keys.BOOK__PK_BOOK, arrayOf(Book.BOOK.ID), true)
val BOOK_GENRE__FK_BOOK_GENRE_PK_GENRE: ForeignKey<BookGenreRecord, GenreRecord> = Internal.createForeignKey(BookGenre.BOOK_GENRE, DSL.name("fk_book_genre_pk_genre"), arrayOf(BookGenre.BOOK_GENRE.GENRE_ID), io.github.asm0dey.opdsko.jooq.keys.GENRE__PK_GENRE, arrayOf(Genre.GENRE.ID), true)
