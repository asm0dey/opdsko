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
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function10;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row10;
import org.jooq.Schema;
import org.jooq.SelectField;
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
    public final TableField<BookRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.identity(true), this, "");

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
    public final TableField<BookRecord, LocalDateTime> ADDED = createField(DSL.name("added"), SQLDataType.LOCALDATETIME(0).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP"), SQLDataType.LOCALDATETIME)), this, "");

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

    /**
     * The column <code>book.zip_file</code>.
     */
    public final TableField<BookRecord, String> ZIP_FILE = createField(DSL.name("zip_file"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>book.seqid</code>.
     */
    public final TableField<BookRecord, Integer> SEQID = createField(DSL.name("seqid"), SQLDataType.INTEGER, this, "");

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
        return Arrays.asList(Indexes.BOOK_ADDED, Indexes.BOOK_SEQ);
    }

    @Override
    public Identity<BookRecord, Long> getIdentity() {
        return (Identity<BookRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<BookRecord> getPrimaryKey() {
        return Keys.BOOK__PK_BOOK;
    }

    @Override
    public Book as(String alias) {
        return new Book(DSL.name(alias), this);
    }

    @Override
    public Book as(Name alias) {
        return new Book(alias, this);
    }

    @Override
    public Book as(Table<?> alias) {
        return new Book(alias.getQualifiedName(), this);
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

    /**
     * Rename this table
     */
    @Override
    public Book rename(Table<?> name) {
        return new Book(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row10 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row10<Long, String, String, String, LocalDateTime, String, Integer, String, String, Integer> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function10<? super Long, ? super String, ? super String, ? super String, ? super LocalDateTime, ? super String, ? super Integer, ? super String, ? super String, ? super Integer, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function10<? super Long, ? super String, ? super String, ? super String, ? super LocalDateTime, ? super String, ? super Integer, ? super String, ? super String, ? super Integer, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
