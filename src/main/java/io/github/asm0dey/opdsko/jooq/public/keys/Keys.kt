/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.`public`.keys


import org.jooq.ForeignKey
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal



// -------------------------------------------------------------------------
// UNIQUE and PRIMARY KEY definitions
// -------------------------------------------------------------------------

val AUTHOR_PKEY: UniqueKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.AuthorRecord> = Internal.createUniqueKey(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR, DSL.name("author_pkey"), arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.ID), true)
val BOOK_PKEY: UniqueKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord> = Internal.createUniqueKey(io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK, DSL.name("book_pkey"), arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK.ID), true)
val GENRE_NAME_KEY: UniqueKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.GenreRecord> = Internal.createUniqueKey(io.github.asm0dey.opdsko.jooq.`public`.tables.Genre.GENRE, DSL.name("genre_name_key"), arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Genre.GENRE.NAME), true)
val GENRE_PKEY: UniqueKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.GenreRecord> = Internal.createUniqueKey(io.github.asm0dey.opdsko.jooq.`public`.tables.Genre.GENRE, DSL.name("genre_pkey"), arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Genre.GENRE.ID), true)

// -------------------------------------------------------------------------
// FOREIGN KEY definitions
// -------------------------------------------------------------------------

val BOOK_AUTHOR__BOOK_AUTHOR_AUTHOR_ID_FKEY: ForeignKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord, io.github.asm0dey.opdsko.jooq.`public`.tables.records.AuthorRecord> = Internal.createForeignKey(io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR, DSL.name("book_author_author_id_fkey"), arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR.AUTHOR_ID), io.github.asm0dey.opdsko.jooq.`public`.keys.AUTHOR_PKEY, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.ID), true)
val BOOK_AUTHOR__BOOK_AUTHOR_BOOK_ID_FKEY: ForeignKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord> = Internal.createForeignKey(io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR, DSL.name("book_author_book_id_fkey"), arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR.BOOK_ID), io.github.asm0dey.opdsko.jooq.`public`.keys.BOOK_PKEY, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK.ID), true)
val BOOK_GENRE__BOOK_GENRE_BOOK_ID_FKEY: ForeignKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookGenreRecord, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord> = Internal.createForeignKey(io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE, DSL.name("book_genre_book_id_fkey"), arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE.BOOK_ID), io.github.asm0dey.opdsko.jooq.`public`.keys.BOOK_PKEY, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK.ID), true)
val BOOK_GENRE__BOOK_GENRE_GENRE_ID_FKEY: ForeignKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookGenreRecord, io.github.asm0dey.opdsko.jooq.`public`.tables.records.GenreRecord> = Internal.createForeignKey(io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE, DSL.name("book_genre_genre_id_fkey"), arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE.GENRE_ID), io.github.asm0dey.opdsko.jooq.`public`.keys.GENRE_PKEY, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Genre.GENRE.ID), true)
