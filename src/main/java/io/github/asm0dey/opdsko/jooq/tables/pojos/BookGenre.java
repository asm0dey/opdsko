/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.pojos;


import io.github.asm0dey.opdsko.jooq.tables.interfaces.IBookGenre;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BookGenre implements IBookGenre {

    private static final long serialVersionUID = 1L;

    private final Long bookId;
    private final Long genreId;

    public BookGenre(IBookGenre value) {
        this.bookId = value.getBookId();
        this.genreId = value.getGenreId();
    }

    public BookGenre(
        Long bookId,
        Long genreId
    ) {
        this.bookId = bookId;
        this.genreId = genreId;
    }

    /**
     * Getter for <code>book_genre.book_id</code>.
     */
    @Override
    public Long getBookId() {
        return this.bookId;
    }

    /**
     * Getter for <code>book_genre.genre_id</code>.
     */
    @Override
    public Long getGenreId() {
        return this.genreId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BookGenre (");

        sb.append(bookId);
        sb.append(", ").append(genreId);

        sb.append(")");
        return sb.toString();
    }
}