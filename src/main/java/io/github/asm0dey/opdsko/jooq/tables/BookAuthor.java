/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables;


import io.github.asm0dey.opdsko.jooq.DefaultSchema;
import io.github.asm0dey.opdsko.jooq.Indexes;
import io.github.asm0dey.opdsko.jooq.Keys;
import io.github.asm0dey.opdsko.jooq.tables.Author.AuthorPath;
import io.github.asm0dey.opdsko.jooq.tables.Book.BookPath;
import io.github.asm0dey.opdsko.jooq.tables.records.BookAuthorRecord;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
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
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private BookAuthor(Name alias, Table<BookAuthorRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
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

    public <O extends Record> BookAuthor(Table<O> path, ForeignKey<O, BookAuthorRecord> childPath, InverseForeignKey<O, BookAuthorRecord> parentPath) {
        super(path, childPath, parentPath, BOOK_AUTHOR);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class BookAuthorPath extends BookAuthor implements Path<BookAuthorRecord> {
        public <O extends Record> BookAuthorPath(Table<O> path, ForeignKey<O, BookAuthorRecord> childPath, InverseForeignKey<O, BookAuthorRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private BookAuthorPath(Name alias, Table<BookAuthorRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public BookAuthorPath as(String alias) {
            return new BookAuthorPath(DSL.name(alias), this);
        }

        @Override
        public BookAuthorPath as(Name alias) {
            return new BookAuthorPath(alias, this);
        }

        @Override
        public BookAuthorPath as(Table<?> alias) {
            return new BookAuthorPath(alias.getQualifiedName(), this);
        }
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
    public List<ForeignKey<BookAuthorRecord, ?>> getReferences() {
        return Arrays.asList(Keys.BOOK_AUTHOR__FK_BOOK_AUTHOR_PK_BOOK, Keys.BOOK_AUTHOR__FK_BOOK_AUTHOR_PK_AUTHOR);
    }

    private transient BookPath _book;

    /**
     * Get the implicit join path to the <code>book</code> table.
     */
    public BookPath book() {
        if (_book == null)
            _book = new BookPath(this, Keys.BOOK_AUTHOR__FK_BOOK_AUTHOR_PK_BOOK, null);

        return _book;
    }

    private transient AuthorPath _author;

    /**
     * Get the implicit join path to the <code>author</code> table.
     */
    public AuthorPath author() {
        if (_author == null)
            _author = new AuthorPath(this, Keys.BOOK_AUTHOR__FK_BOOK_AUTHOR_PK_AUTHOR, null);

        return _author;
    }

    @Override
    public BookAuthor as(String alias) {
        return new BookAuthor(DSL.name(alias), this);
    }

    @Override
    public BookAuthor as(Name alias) {
        return new BookAuthor(alias, this);
    }

    @Override
    public BookAuthor as(Table<?> alias) {
        return new BookAuthor(alias.getQualifiedName(), this);
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

    /**
     * Rename this table
     */
    @Override
    public BookAuthor rename(Table<?> name) {
        return new BookAuthor(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BookAuthor where(Condition condition) {
        return new BookAuthor(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BookAuthor where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BookAuthor where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BookAuthor where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BookAuthor where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BookAuthor where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BookAuthor where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public BookAuthor where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BookAuthor whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public BookAuthor whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
