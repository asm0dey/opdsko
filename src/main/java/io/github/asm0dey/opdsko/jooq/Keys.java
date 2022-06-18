/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq;


import io.github.asm0dey.opdsko.jooq.tables.Author;
import io.github.asm0dey.opdsko.jooq.tables.Book;
import io.github.asm0dey.opdsko.jooq.tables.BookAuthor;
import io.github.asm0dey.opdsko.jooq.tables.BookGenre;
import io.github.asm0dey.opdsko.jooq.tables.Genre;
import io.github.asm0dey.opdsko.jooq.tables.records.AuthorRecord;
import io.github.asm0dey.opdsko.jooq.tables.records.BookAuthorRecord;
import io.github.asm0dey.opdsko.jooq.tables.records.BookGenreRecord;
import io.github.asm0dey.opdsko.jooq.tables.records.BookRecord;
import io.github.asm0dey.opdsko.jooq.tables.records.GenreRecord;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in the
 * default schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<AuthorRecord> AUTHOR__ = Internal.createUniqueKey(Author.AUTHOR, DSL.name(""), new TableField[] { Author.AUTHOR.ID }, true);
    public static final UniqueKey<BookRecord> BOOK__ = Internal.createUniqueKey(Book.BOOK, DSL.name(""), new TableField[] { Book.BOOK.ID, Book.BOOK.PATH }, true);
    public static final UniqueKey<BookAuthorRecord> BOOK_AUTHOR__PK_BOOK_AUTHOR = Internal.createUniqueKey(BookAuthor.BOOK_AUTHOR, DSL.name("pk_book_author"), new TableField[] { BookAuthor.BOOK_AUTHOR.BOOK_ID, BookAuthor.BOOK_AUTHOR.AUTHOR_ID }, true);
    public static final UniqueKey<BookGenreRecord> BOOK_GENRE__PK_BOOK_GENRE = Internal.createUniqueKey(BookGenre.BOOK_GENRE, DSL.name("pk_book_genre"), new TableField[] { BookGenre.BOOK_GENRE.BOOK_ID, BookGenre.BOOK_GENRE.GENRE_ID }, true);
    public static final UniqueKey<GenreRecord> GENRE__ = Internal.createUniqueKey(Genre.GENRE, DSL.name(""), new TableField[] { Genre.GENRE.ID, Genre.GENRE.NAME }, true);
}
