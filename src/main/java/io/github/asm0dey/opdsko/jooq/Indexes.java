/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq;


import io.github.asm0dey.opdsko.jooq.tables.Author;
import io.github.asm0dey.opdsko.jooq.tables.Book;
import io.github.asm0dey.opdsko.jooq.tables.BookAuthor;
import io.github.asm0dey.opdsko.jooq.tables.BookGenre;
import io.github.asm0dey.opdsko.jooq.tables.Genre;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables in the default schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index AUTHOR_ADDED = Internal.createIndex(DSL.name("author_added"), Author.AUTHOR, new OrderField[] { Author.AUTHOR.ADDED }, false);
    public static final Index AUTHOR_NAMES = Internal.createIndex(DSL.name("author_names"), Author.AUTHOR, new OrderField[] { Author.AUTHOR.MIDDLE_NAME, Author.AUTHOR.LAST_NAME, Author.AUTHOR.FIRST_NAME, Author.AUTHOR.NICKNAME }, false);
    public static final Index BOOK_ADDED = Internal.createIndex(DSL.name("book_added"), Book.BOOK, new OrderField[] { Book.BOOK.ADDED }, false);
    public static final Index BOOK_AUTHOR_AUTHOR_ID = Internal.createIndex(DSL.name("book_author_author_id"), BookAuthor.BOOK_AUTHOR, new OrderField[] { BookAuthor.BOOK_AUTHOR.AUTHOR_ID }, false);
    public static final Index BOOK_AUTHOR_BOOK_ID = Internal.createIndex(DSL.name("book_author_book_id"), BookAuthor.BOOK_AUTHOR, new OrderField[] { BookAuthor.BOOK_AUTHOR.BOOK_ID }, false);
    public static final Index BOOK_GENRE_BOOK_ID = Internal.createIndex(DSL.name("book_genre_book_id"), BookGenre.BOOK_GENRE, new OrderField[] { BookGenre.BOOK_GENRE.BOOK_ID }, false);
    public static final Index BOOK_GENRE_GENRE_ID = Internal.createIndex(DSL.name("book_genre_genre_id"), BookGenre.BOOK_GENRE, new OrderField[] { BookGenre.BOOK_GENRE.GENRE_ID }, false);
    public static final Index BOOK_SEQ = Internal.createIndex(DSL.name("book_seq"), Book.BOOK, new OrderField[] { Book.BOOK.SEQUENCE }, false);
    public static final Index GENRE_NAME = Internal.createIndex(DSL.name("genre_name"), Genre.GENRE, new OrderField[] { Genre.GENRE.NAME }, true);
}
