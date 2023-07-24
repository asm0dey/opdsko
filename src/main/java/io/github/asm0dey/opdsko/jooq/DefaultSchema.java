/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq;


import io.github.asm0dey.opdsko.jooq.tables.Author;
import io.github.asm0dey.opdsko.jooq.tables.AuthorsFts;
import io.github.asm0dey.opdsko.jooq.tables.Book;
import io.github.asm0dey.opdsko.jooq.tables.BookAuthor;
import io.github.asm0dey.opdsko.jooq.tables.BookGenre;
import io.github.asm0dey.opdsko.jooq.tables.BooksFts;
import io.github.asm0dey.opdsko.jooq.tables.Genre;

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DefaultSchema extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>DEFAULT_SCHEMA</code>
     */
    public static final DefaultSchema DEFAULT_SCHEMA = new DefaultSchema();

    /**
     * The table <code>author</code>.
     */
    public final Author AUTHOR = Author.AUTHOR;

    /**
     * The table <code>authors_fts</code>.
     */
    public final AuthorsFts AUTHORS_FTS = AuthorsFts.AUTHORS_FTS;

    /**
     * The table <code>book</code>.
     */
    public final Book BOOK = Book.BOOK;

    /**
     * The table <code>book_author</code>.
     */
    public final BookAuthor BOOK_AUTHOR = BookAuthor.BOOK_AUTHOR;

    /**
     * The table <code>book_genre</code>.
     */
    public final BookGenre BOOK_GENRE = BookGenre.BOOK_GENRE;

    /**
     * The table <code>books_fts</code>.
     */
    public final BooksFts BOOKS_FTS = BooksFts.BOOKS_FTS;

    /**
     * The table <code>genre</code>.
     */
    public final Genre GENRE = Genre.GENRE;

    /**
     * No further instances allowed
     */
    private DefaultSchema() {
        super("", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            Author.AUTHOR,
            AuthorsFts.AUTHORS_FTS,
            Book.BOOK,
            BookAuthor.BOOK_AUTHOR,
            BookGenre.BOOK_GENRE,
            BooksFts.BOOKS_FTS,
            Genre.GENRE
        );
    }
}
