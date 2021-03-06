/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables;


import io.github.asm0dey.opdsko.jooq.DefaultSchema;
import io.github.asm0dey.opdsko.jooq.Indexes;
import io.github.asm0dey.opdsko.jooq.Keys;
import io.github.asm0dey.opdsko.jooq.tables.records.BookAuthorRecord;

import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
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
public class BookAuthor extends TableImpl<BookAuthorRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>book_author</code>
     */
    public static final BookAuthor BOOK_AUTHOR = new BookAuthor();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BookAuthorRecord> getRecordType() {
        return BookAuthorRecord.class;
    }

    /**
     * The column <code>book_author.book_id</code>.
     */
    public final TableField<BookAuthorRecord, Long> BOOK_ID = createField(DSL.name("book_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>book_author.author_id</code>.
     */
    public final TableField<BookAuthorRecord, Long> AUTHOR_ID = createField(DSL.name("author_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private BookAuthor(Name alias, Table<BookAuthorRecord> aliased) {
        this(alias, aliased, null);
    }

    private BookAuthor(Name alias, Table<BookAuthorRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>book_author</code> table reference
     */
    public BookAuthor(String alias) {
        this(DSL.name(alias), BOOK_AUTHOR);
    }

    /**
     * Create an aliased <code>book_author</code> table reference
     */
    public BookAuthor(Name alias) {
        this(alias, BOOK_AUTHOR);
    }

    /**
     * Create a <code>book_author</code> table reference
     */
    public BookAuthor() {
        this(DSL.name("book_author"), null);
    }

    public <O extends Record> BookAuthor(Table<O> child, ForeignKey<O, BookAuthorRecord> key) {
        super(child, key, BOOK_AUTHOR);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.BOOK_AUTHOR_AUTHOR_ID, Indexes.BOOK_AUTHOR_BOOK_ID);
    }

    @Override
    public UniqueKey<BookAuthorRecord> getPrimaryKey() {
        return Keys.BOOK_AUTHOR__PK_BOOK_AUTHOR;
    }

    @Override
    public BookAuthor as(String alias) {
        return new BookAuthor(DSL.name(alias), this);
    }

    @Override
    public BookAuthor as(Name alias) {
        return new BookAuthor(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public BookAuthor rename(String name) {
        return new BookAuthor(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BookAuthor rename(Name name) {
        return new BookAuthor(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Long, Long> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
