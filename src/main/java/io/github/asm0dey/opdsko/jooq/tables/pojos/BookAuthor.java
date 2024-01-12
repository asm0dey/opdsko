/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.pojos;


import io.github.asm0dey.opdsko.jooq.tables.interfaces.IBookAuthor;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BookAuthor other = (BookAuthor) obj;
        if (this.bookId == null) {
            if (other.bookId != null)
                return false;
        }
        else if (!this.bookId.equals(other.bookId))
            return false;
        if (this.authorId == null) {
            if (other.authorId != null)
                return false;
        }
        else if (!this.authorId.equals(other.authorId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.bookId == null) ? 0 : this.bookId.hashCode());
        result = prime * result + ((this.authorId == null) ? 0 : this.authorId.hashCode());
        return result;
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
