/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.indexes


import io.github.asm0dey.opdsko.jooq.tables.Author
import io.github.asm0dey.opdsko.jooq.tables.Book
import io.github.asm0dey.opdsko.jooq.tables.BookAuthor
import io.github.asm0dey.opdsko.jooq.tables.BookGenre
import io.github.asm0dey.opdsko.jooq.tables.Genre

import org.jooq.Index
import org.jooq.impl.DSL
import org.jooq.impl.Internal



// -------------------------------------------------------------------------
// INDEX definitions
// -------------------------------------------------------------------------

val AUTHOR_ADDED: Index = Internal.createIndex(DSL.name("author_added"), Author.AUTHOR, arrayOf(Author.AUTHOR.ADDED), false)
val AUTHOR_NAMES: Index = Internal.createIndex(DSL.name("author_names"), Author.AUTHOR, arrayOf(Author.AUTHOR.MIDDLE_NAME, Author.AUTHOR.LAST_NAME, Author.AUTHOR.FIRST_NAME, Author.AUTHOR.NICKNAME), false)
val BOOK_ADDED: Index = Internal.createIndex(DSL.name("book_added"), Book.BOOK, arrayOf(Book.BOOK.ADDED), false)
val BOOK_AUTHOR_AUTHOR_ID: Index = Internal.createIndex(DSL.name("book_author_author_id"), BookAuthor.BOOK_AUTHOR, arrayOf(BookAuthor.BOOK_AUTHOR.AUTHOR_ID), false)
val BOOK_AUTHOR_BOOK_ID: Index = Internal.createIndex(DSL.name("book_author_book_id"), BookAuthor.BOOK_AUTHOR, arrayOf(BookAuthor.BOOK_AUTHOR.BOOK_ID), false)
val BOOK_GENRE_BOOK_ID: Index = Internal.createIndex(DSL.name("book_genre_book_id"), BookGenre.BOOK_GENRE, arrayOf(BookGenre.BOOK_GENRE.BOOK_ID), false)
val BOOK_GENRE_GENRE_ID: Index = Internal.createIndex(DSL.name("book_genre_genre_id"), BookGenre.BOOK_GENRE, arrayOf(BookGenre.BOOK_GENRE.GENRE_ID), false)
val BOOK_SEQ: Index = Internal.createIndex(DSL.name("book_seq"), Book.BOOK, arrayOf(Book.BOOK.SEQUENCE), false)
val BOOK_SEQUENCE_INDEX: Index = Internal.createIndex(DSL.name("book_sequence_index"), Book.BOOK, arrayOf(Book.BOOK.SEQUENCE), false)
val GENRE_NAME: Index = Internal.createIndex(DSL.name("genre_name"), Genre.GENRE, arrayOf(Genre.GENRE.NAME), true)
