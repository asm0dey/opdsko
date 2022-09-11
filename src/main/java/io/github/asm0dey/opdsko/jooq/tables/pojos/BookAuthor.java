/*
 * opdsko
 * Copyright (C) 2022  asm0dey
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
