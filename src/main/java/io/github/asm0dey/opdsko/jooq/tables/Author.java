/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables;


import io.github.asm0dey.opdsko.jooq.DefaultSchema;
import io.github.asm0dey.opdsko.jooq.Indexes;
import io.github.asm0dey.opdsko.jooq.Keys;
import io.github.asm0dey.opdsko.jooq.tables.Book.BookPath;
import io.github.asm0dey.opdsko.jooq.tables.BookAuthor.BookAuthorPath;
import io.github.asm0dey.opdsko.jooq.tables.records.AuthorRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
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
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Author extends TableImpl<AuthorRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>author</code>
     */
    public static final Author AUTHOR = new Author();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AuthorRecord> getRecordType() {
        return AuthorRecord.class;
    }

    /**
     * The column <code>author.id</code>.
     */
    public final TableField<AuthorRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.identity(true), this, "");

    /**
     * The column <code>author.fb2id</code>.
     */
    public final TableField<AuthorRecord, String> FB2ID = createField(DSL.name("fb2id"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>author.first_name</code>.
     */
    public final TableField<AuthorRecord, String> FIRST_NAME = createField(DSL.name("first_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>author.middle_name</code>.
     */
    public final TableField<AuthorRecord, String> MIDDLE_NAME = createField(DSL.name("middle_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>author.last_name</code>.
     */
    public final TableField<AuthorRecord, String> LAST_NAME = createField(DSL.name("last_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>author.nickname</code>.
     */
    public final TableField<AuthorRecord, String> NICKNAME = createField(DSL.name("nickname"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>author.added</code>.
     */
    public final TableField<AuthorRecord, LocalDateTime> ADDED = createField(DSL.name("added"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "");

    private Author(Name alias, Table<AuthorRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Author(Name alias, Table<AuthorRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>author</code> table reference
     */
    public Author(String alias) {
        this(DSL.name(alias), AUTHOR);
    }

    /**
     * Create an aliased <code>author</code> table reference
     */
    public Author(Name alias) {
        this(alias, AUTHOR);
    }

    /**
     * Create a <code>author</code> table reference
     */
    public Author() {
        this(DSL.name("author"), null);
    }

    public <O extends Record> Author(Table<O> path, ForeignKey<O, AuthorRecord> childPath, InverseForeignKey<O, AuthorRecord> parentPath) {
        super(path, childPath, parentPath, AUTHOR);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class AuthorPath extends Author implements Path<AuthorRecord> {
        public <O extends Record> AuthorPath(Table<O> path, ForeignKey<O, AuthorRecord> childPath, InverseForeignKey<O, AuthorRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private AuthorPath(Name alias, Table<AuthorRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public AuthorPath as(String alias) {
            return new AuthorPath(DSL.name(alias), this);
        }

        @Override
        public AuthorPath as(Name alias) {
            return new AuthorPath(alias, this);
        }

        @Override
        public AuthorPath as(Table<?> alias) {
            return new AuthorPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.AUTHOR_ADDED, Indexes.AUTHOR_NAMES);
    }

    @Override
    public Identity<AuthorRecord, Long> getIdentity() {
        return (Identity<AuthorRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<AuthorRecord> getPrimaryKey() {
        return Keys.AUTHOR__PK_AUTHOR;
    }

    private transient BookAuthorPath _bookAuthor;

    /**
     * Get the implicit to-many join path to the <code>book_author</code> table
     */
    public BookAuthorPath bookAuthor() {
        if (_bookAuthor == null)
            _bookAuthor = new BookAuthorPath(this, null, Keys.BOOK_AUTHOR__FK_BOOK_AUTHOR_PK_AUTHOR.getInverseKey());

        return _bookAuthor;
    }

    /**
     * Get the implicit many-to-many join path to the <code>book</code> table
     */
    public BookPath book() {
        return bookAuthor().book();
    }

    @Override
    public Author as(String alias) {
        return new Author(DSL.name(alias), this);
    }

    @Override
    public Author as(Name alias) {
        return new Author(alias, this);
    }

    @Override
    public Author as(Table<?> alias) {
        return new Author(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Author rename(String name) {
        return new Author(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Author rename(Name name) {
        return new Author(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Author rename(Table<?> name) {
        return new Author(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Author where(Condition condition) {
        return new Author(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Author where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Author where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Author where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Author where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Author where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Author where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Author where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Author whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Author whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
