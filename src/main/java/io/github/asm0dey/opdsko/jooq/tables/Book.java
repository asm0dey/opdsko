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
package io.github.asm0dey.opdsko.jooq.tables;


import io.github.asm0dey.opdsko.jooq.DefaultSchema;
import io.github.asm0dey.opdsko.jooq.Indexes;
import io.github.asm0dey.opdsko.jooq.Keys;
import io.github.asm0dey.opdsko.jooq.tables.records.BookRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row8;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Book extends TableImpl<BookRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>book</code>
     */
    public static final Book BOOK = new Book();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BookRecord> getRecordType() {
        return BookRecord.class;
    }

    /**
     * The column <code>book.id</code>.
     */
    public final TableField<BookRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>book.path</code>.
     */
    public final TableField<BookRecord, String> PATH = createField(DSL.name("path"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>book.name</code>.
     */
    public final TableField<BookRecord, String> NAME = createField(DSL.name("name"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>book.date</code>.
     */
    public final TableField<BookRecord, String> DATE = createField(DSL.name("date"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>book.added</code>.
     */
    public final TableField<BookRecord, LocalDateTime> ADDED = createField(DSL.name("added"), SQLDataType.LOCALDATETIME(0).nullable(false).defaultValue(DSL.field("CURRENT_TIMESTAMP", SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>book.sequence</code>.
     */
    public final TableField<BookRecord, String> SEQUENCE = createField(DSL.name("sequence"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>book.sequence_number</code>.
     */
    public final TableField<BookRecord, Integer> SEQUENCE_NUMBER = createField(DSL.name("sequence_number"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>book.lang</code>.
     */
    public final TableField<BookRecord, String> LANG = createField(DSL.name("lang"), SQLDataType.CLOB, this, "");

    private Book(Name alias, Table<BookRecord> aliased) {
        this(alias, aliased, null);
    }

    private Book(Name alias, Table<BookRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>book</code> table reference
     */
    public Book(String alias) {
        this(DSL.name(alias), BOOK);
    }

    /**
     * Create an aliased <code>book</code> table reference
     */
    public Book(Name alias) {
        this(alias, BOOK);
    }

    /**
     * Create a <code>book</code> table reference
     */
    public Book() {
        this(DSL.name("book"), null);
    }

    public <O extends Record> Book(Table<O> child, ForeignKey<O, BookRecord> key) {
        super(child, key, BOOK);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.BOOK_ADDED, Indexes.BOOK_PATH, Indexes.BOOK_PATH_NAME, Indexes.BOOK_SEQ);
    }

    @Override
    public Identity<BookRecord, Long> getIdentity() {
        return (Identity<BookRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<BookRecord> getPrimaryKey() {
        return Keys.BOOK__;
    }

    @Override
    public Book as(String alias) {
        return new Book(DSL.name(alias), this);
    }

    @Override
    public Book as(Name alias) {
        return new Book(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Book rename(String name) {
        return new Book(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Book rename(Name name) {
        return new Book(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<Long, String, String, String, LocalDateTime, String, Integer, String> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
