/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables;


import io.github.asm0dey.opdsko.jooq.DefaultSchema;
import io.github.asm0dey.opdsko.jooq.Indexes;
import io.github.asm0dey.opdsko.jooq.Keys;
import io.github.asm0dey.opdsko.jooq.tables.records.BookGenreRecord;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function2;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row2;
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
public class BookGenre extends TableImpl<BookGenreRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>book_genre</code>
     */
    public static final BookGenre BOOK_GENRE = new BookGenre();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BookGenreRecord> getRecordType() {
        return BookGenreRecord.class;
    }

    /**
     * The column <code>book_genre.book_id</code>.
     */
    public final TableField<BookGenreRecord, Long> BOOK_ID = createField(DSL.name("book_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>book_genre.genre_id</code>.
     */
    public final TableField<BookGenreRecord, Long> GENRE_ID = createField(DSL.name("genre_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private BookGenre(Name alias, Table<BookGenreRecord> aliased) {
        this(alias, aliased, null);
    }

    private BookGenre(Name alias, Table<BookGenreRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>book_genre</code> table reference
     */
    public BookGenre(String alias) {
        this(DSL.name(alias), BOOK_GENRE);
    }

    /**
     * Create an aliased <code>book_genre</code> table reference
     */
    public BookGenre(Name alias) {
        this(alias, BOOK_GENRE);
    }

    /**
     * Create a <code>book_genre</code> table reference
     */
    public BookGenre() {
        this(DSL.name("book_genre"), null);
    }

    public <O extends Record> BookGenre(Table<O> child, ForeignKey<O, BookGenreRecord> key) {
        super(child, key, BOOK_GENRE);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.BOOK_GENRE_BOOK_ID, Indexes.BOOK_GENRE_GENRE_ID);
    }

    @Override
    public UniqueKey<BookGenreRecord> getPrimaryKey() {
        return Keys.BOOK_GENRE__PK_BOOK_GENRE;
    }

    @Override
    public List<ForeignKey<BookGenreRecord, ?>> getReferences() {
        return Arrays.asList(Keys.BOOK_GENRE__FK_BOOK_GENRE_PK_BOOK, Keys.BOOK_GENRE__FK_BOOK_GENRE_PK_GENRE);
    }

    private transient Book _book;
    private transient Genre _genre;

    /**
     * Get the implicit join path to the <code>book</code> table.
     */
    public Book book() {
        if (_book == null)
            _book = new Book(this, Keys.BOOK_GENRE__FK_BOOK_GENRE_PK_BOOK);

        return _book;
    }

    /**
     * Get the implicit join path to the <code>genre</code> table.
     */
    public Genre genre() {
        if (_genre == null)
            _genre = new Genre(this, Keys.BOOK_GENRE__FK_BOOK_GENRE_PK_GENRE);

        return _genre;
    }

    @Override
    public BookGenre as(String alias) {
        return new BookGenre(DSL.name(alias), this);
    }

    @Override
    public BookGenre as(Name alias) {
        return new BookGenre(alias, this);
    }

    @Override
    public BookGenre as(Table<?> alias) {
        return new BookGenre(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public BookGenre rename(String name) {
        return new BookGenre(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BookGenre rename(Name name) {
        return new BookGenre(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public BookGenre rename(Table<?> name) {
        return new BookGenre(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Long, Long> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function2<? super Long, ? super Long, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function2<? super Long, ? super Long, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
