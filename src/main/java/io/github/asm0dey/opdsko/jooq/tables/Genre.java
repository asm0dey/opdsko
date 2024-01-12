/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables;


import io.github.asm0dey.opdsko.jooq.DefaultSchema;
import io.github.asm0dey.opdsko.jooq.Indexes;
import io.github.asm0dey.opdsko.jooq.Keys;
import io.github.asm0dey.opdsko.jooq.tables.Book.BookPath;
import io.github.asm0dey.opdsko.jooq.tables.BookGenre.BookGenrePath;
import io.github.asm0dey.opdsko.jooq.tables.records.GenreRecord;

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
public class Genre extends TableImpl<GenreRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>genre</code>
     */
    public static final Genre GENRE = new Genre();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<GenreRecord> getRecordType() {
        return GenreRecord.class;
    }

    /**
     * The column <code>genre.id</code>.
     */
    public final TableField<GenreRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.identity(true), this, "");

    /**
     * The column <code>genre.name</code>.
     */
    public final TableField<GenreRecord, String> NAME = createField(DSL.name("name"), SQLDataType.CLOB.nullable(false), this, "");

    private Genre(Name alias, Table<GenreRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Genre(Name alias, Table<GenreRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>genre</code> table reference
     */
    public Genre(String alias) {
        this(DSL.name(alias), GENRE);
    }

    /**
     * Create an aliased <code>genre</code> table reference
     */
    public Genre(Name alias) {
        this(alias, GENRE);
    }

    /**
     * Create a <code>genre</code> table reference
     */
    public Genre() {
        this(DSL.name("genre"), null);
    }

    public <O extends Record> Genre(Table<O> path, ForeignKey<O, GenreRecord> childPath, InverseForeignKey<O, GenreRecord> parentPath) {
        super(path, childPath, parentPath, GENRE);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class GenrePath extends Genre implements Path<GenreRecord> {
        public <O extends Record> GenrePath(Table<O> path, ForeignKey<O, GenreRecord> childPath, InverseForeignKey<O, GenreRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private GenrePath(Name alias, Table<GenreRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public GenrePath as(String alias) {
            return new GenrePath(DSL.name(alias), this);
        }

        @Override
        public GenrePath as(Name alias) {
            return new GenrePath(alias, this);
        }

        @Override
        public GenrePath as(Table<?> alias) {
            return new GenrePath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.GENRE_NAME);
    }

    @Override
    public Identity<GenreRecord, Long> getIdentity() {
        return (Identity<GenreRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<GenreRecord> getPrimaryKey() {
        return Keys.GENRE__PK_GENRE;
    }

    @Override
    public List<UniqueKey<GenreRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.GENRE__UK_GENRE_113431810);
    }

    private transient BookGenrePath _bookGenre;

    /**
     * Get the implicit to-many join path to the <code>book_genre</code> table
     */
    public BookGenrePath bookGenre() {
        if (_bookGenre == null)
            _bookGenre = new BookGenrePath(this, null, Keys.BOOK_GENRE__FK_BOOK_GENRE_PK_GENRE.getInverseKey());

        return _bookGenre;
    }

    /**
     * Get the implicit many-to-many join path to the <code>book</code> table
     */
    public BookPath book() {
        return bookGenre().book();
    }

    @Override
    public Genre as(String alias) {
        return new Genre(DSL.name(alias), this);
    }

    @Override
    public Genre as(Name alias) {
        return new Genre(alias, this);
    }

    @Override
    public Genre as(Table<?> alias) {
        return new Genre(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Genre rename(String name) {
        return new Genre(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Genre rename(Name name) {
        return new Genre(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Genre rename(Table<?> name) {
        return new Genre(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Genre where(Condition condition) {
        return new Genre(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Genre where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Genre where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Genre where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Genre where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Genre where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Genre where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Genre where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Genre whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Genre whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
