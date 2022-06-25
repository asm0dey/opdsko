/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables;


import io.github.asm0dey.opdsko.jooq.DefaultSchema;
import io.github.asm0dey.opdsko.jooq.tables.records.BooksFtsRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BooksFts extends TableImpl<BooksFtsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>books_fts</code>
     */
    public static final BooksFts BOOKS_FTS = new BooksFts();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BooksFtsRecord> getRecordType() {
        return BooksFtsRecord.class;
    }

    /**
     * The column <code>books_fts.name</code>.
     */
    public final TableField<BooksFtsRecord, String> NAME = createField(DSL.name("name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>books_fts.sequence</code>.
     */
    public final TableField<BooksFtsRecord, String> SEQUENCE = createField(DSL.name("sequence"), SQLDataType.CLOB, this, "");

    private BooksFts(Name alias, Table<BooksFtsRecord> aliased) {
        this(alias, aliased, null);
    }

    private BooksFts(Name alias, Table<BooksFtsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>books_fts</code> table reference
     */
    public BooksFts(String alias) {
        this(DSL.name(alias), BOOKS_FTS);
    }

    /**
     * Create an aliased <code>books_fts</code> table reference
     */
    public BooksFts(Name alias) {
        this(alias, BOOKS_FTS);
    }

    /**
     * Create a <code>books_fts</code> table reference
     */
    public BooksFts() {
        this(DSL.name("books_fts"), null);
    }

    public <O extends Record> BooksFts(Table<O> child, ForeignKey<O, BooksFtsRecord> key) {
        super(child, key, BOOKS_FTS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public BooksFts as(String alias) {
        return new BooksFts(DSL.name(alias), this);
    }

    @Override
    public BooksFts as(Name alias) {
        return new BooksFts(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public BooksFts rename(String name) {
        return new BooksFts(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BooksFts rename(Name name) {
        return new BooksFts(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}