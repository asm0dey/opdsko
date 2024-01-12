/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.daos;


import io.github.asm0dey.opdsko.jooq.tables.BookAuthor;
import io.github.asm0dey.opdsko.jooq.tables.records.BookAuthorRecord;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record2;
import org.jooq.impl.DAOImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class BookAuthorDao extends DAOImpl<BookAuthorRecord, io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor, Record2<Long, Long>> {

    /**
     * Create a new BookAuthorDao without any configuration
     */
    public BookAuthorDao() {
        super(BookAuthor.BOOK_AUTHOR, io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor.class);
    }

    /**
     * Create a new BookAuthorDao with an attached configuration
     */
    public BookAuthorDao(Configuration configuration) {
        super(BookAuthor.BOOK_AUTHOR, io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor.class, configuration);
    }

    @Override
    public Record2<Long, Long> getId(io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor object) {
        return compositeKeyRecord(object.getBookId(), object.getAuthorId());
    }

    /**
     * Fetch records that have <code>book_id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor> fetchRangeOfBookId(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(BookAuthor.BOOK_AUTHOR.BOOK_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>book_id IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor> fetchByBookId(Long... values) {
        return fetch(BookAuthor.BOOK_AUTHOR.BOOK_ID, values);
    }

    /**
     * Fetch records that have <code>author_id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor> fetchRangeOfAuthorId(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(BookAuthor.BOOK_AUTHOR.AUTHOR_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>author_id IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.BookAuthor> fetchByAuthorId(Long... values) {
        return fetch(BookAuthor.BOOK_AUTHOR.AUTHOR_ID, values);
    }
}
