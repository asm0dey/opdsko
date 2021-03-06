/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.pojos;


import io.github.asm0dey.opdsko.jooq.tables.interfaces.IBookAuthor;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BookAuthor implements IBookAuthor {

    private static final long serialVersionUID = 1L;

    private final Long bookId;
    private final Long authorId;

    public BookAuthor(IBookAuthor value) {
        this.bookId = value.getBookId();
        this.authorId = value.getAuthorId();
    }

    public BookAuthor(
        Long bookId,
        Long authorId
    ) {
        this.bookId = bookId;
        this.authorId = authorId;
    }

    /**
     * Getter for <code>book_author.book_id</code>.
     */
    @Override
    public Long getBookId() {
        return this.bookId;
    }

    /**
     * Getter for <code>book_author.author_id</code>.
     */
    @Override
    public Long getAuthorId() {
        return this.authorId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BookAuthor (");

        sb.append(bookId);
        sb.append(", ").append(authorId);

        sb.append(")");
        return sb.toString();
    }
}
