/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.daos;


import io.github.asm0dey.opdsko.jooq.tables.Book;
import io.github.asm0dey.opdsko.jooq.tables.records.BookRecord;

import java.time.LocalDateTime;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record2;
import org.jooq.impl.DAOImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BookDao extends DAOImpl<BookRecord, io.github.asm0dey.opdsko.jooq.tables.pojos.Book, Record2<Long, String>> {

    /**
     * Create a new BookDao without any configuration
     */
    public BookDao() {
        super(Book.BOOK, io.github.asm0dey.opdsko.jooq.tables.pojos.Book.class);
    }

    /**
     * Create a new BookDao with an attached configuration
     */
    public BookDao(Configuration configuration) {
        super(Book.BOOK, io.github.asm0dey.opdsko.jooq.tables.pojos.Book.class, configuration);
    }

    @Override
    public Record2<Long, String> getId(io.github.asm0dey.opdsko.jooq.tables.pojos.Book object) {
        return compositeKeyRecord(object.getId(), object.getPath());
    }

    /**
     * Fetch records that have <code>id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchRangeOfId(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(Book.BOOK.ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchById(Long... values) {
        return fetch(Book.BOOK.ID, values);
    }

    /**
     * Fetch records that have <code>path BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchRangeOfPath(String lowerInclusive, String upperInclusive) {
        return fetchRange(Book.BOOK.PATH, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>path IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchByPath(String... values) {
        return fetch(Book.BOOK.PATH, values);
    }

    /**
     * Fetch records that have <code>name BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchRangeOfName(String lowerInclusive, String upperInclusive) {
        return fetchRange(Book.BOOK.NAME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>name IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchByName(String... values) {
        return fetch(Book.BOOK.NAME, values);
    }

    /**
     * Fetch records that have <code>date BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchRangeOfDate(String lowerInclusive, String upperInclusive) {
        return fetchRange(Book.BOOK.DATE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>date IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchByDate(String... values) {
        return fetch(Book.BOOK.DATE, values);
    }

    /**
     * Fetch records that have <code>added BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchRangeOfAdded(LocalDateTime lowerInclusive, LocalDateTime upperInclusive) {
        return fetchRange(Book.BOOK.ADDED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>added IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchByAdded(LocalDateTime... values) {
        return fetch(Book.BOOK.ADDED, values);
    }

    /**
     * Fetch records that have <code>sequence BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchRangeOfSequence(String lowerInclusive, String upperInclusive) {
        return fetchRange(Book.BOOK.SEQUENCE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>sequence IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchBySequence(String... values) {
        return fetch(Book.BOOK.SEQUENCE, values);
    }

    /**
     * Fetch records that have <code>sequence_number BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchRangeOfSequenceNumber(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Book.BOOK.SEQUENCE_NUMBER, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>sequence_number IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchBySequenceNumber(Integer... values) {
        return fetch(Book.BOOK.SEQUENCE_NUMBER, values);
    }

    /**
     * Fetch records that have <code>lang BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchRangeOfLang(String lowerInclusive, String upperInclusive) {
        return fetchRange(Book.BOOK.LANG, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>lang IN (values)</code>
     */
    public List<io.github.asm0dey.opdsko.jooq.tables.pojos.Book> fetchByLang(String... values) {
        return fetch(Book.BOOK.LANG, values);
    }
}
