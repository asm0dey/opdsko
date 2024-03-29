/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.`public`.indexes


import org.jooq.Index
import org.jooq.impl.DSL
import org.jooq.impl.Internal



// -------------------------------------------------------------------------
// INDEX definitions
// -------------------------------------------------------------------------

val AUTHOR_ADDED_IDX: Index = Internal.createIndex(DSL.name("author_added_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.ADDED), false)
val AUTHOR_NAME_IDX: Index = Internal.createIndex(DSL.name("author_name_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.MIDDLE_NAME, io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.LAST_NAME, io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.FIRST_NAME, io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.NICKNAME), true)
val AUTHOR_NAMES_IDX: Index = Internal.createIndex(DSL.name("author_names_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.MIDDLE_NAME, io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.LAST_NAME, io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.FIRST_NAME, io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.NICKNAME), false)
val BOOK_ADDED_IDX: Index = Internal.createIndex(DSL.name("book_added_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK.ADDED), false)
val BOOK_AUTHOR_AUTHOR_IDX: Index = Internal.createIndex(DSL.name("book_author_author_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR.AUTHOR_ID), false)
val BOOK_AUTHOR_BOOK_AUTHOR_IDX: Index = Internal.createIndex(DSL.name("book_author_book_author_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR.BOOK_ID, io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR.AUTHOR_ID), true)
val BOOK_AUTHOR_BOOK_IDX: Index = Internal.createIndex(DSL.name("book_author_book_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BOOK_AUTHOR.BOOK_ID), false)
val BOOK_GENRE_BOOK_BOOK_IDX: Index = Internal.createIndex(DSL.name("book_genre_book_book_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE.BOOK_ID), false)
val BOOK_GENRE_BOOK_GENRE_IDX: Index = Internal.createIndex(DSL.name("book_genre_book_genre_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE.BOOK_ID, io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE.GENRE_ID), true)
val BOOK_GENRE_GENRE_IDX: Index = Internal.createIndex(DSL.name("book_genre_genre_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BOOK_GENRE.GENRE_ID), false)
val BOOK_PATH_ARCHIVE: Index = Internal.createIndex(DSL.name("book_path_archive"), io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK.PATH, io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK.ZIP_FILE), true)
val BOOK_SEQUENCE_IDX: Index = Internal.createIndex(DSL.name("book_sequence_idx"), io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK.SEQUENCE), false)
val NEWONE: Index = Internal.createIndex(DSL.name("newone"), io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK, arrayOf(io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BOOK.SEQUENCE), false)
