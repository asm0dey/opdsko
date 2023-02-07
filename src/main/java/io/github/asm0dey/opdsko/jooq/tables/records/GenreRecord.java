/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.records;


import io.github.asm0dey.opdsko.jooq.tables.Genre;
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IGenre;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GenreRecord extends UpdatableRecordImpl<GenreRecord> implements Record2<Long, String>, IGenre {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>genre.id</code>.
     */
    public GenreRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>genre.id</code>.
     */
    @Override
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>genre.name</code>.
     */
    public GenreRecord setName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>genre.name</code>.
     */
    @Override
    public String getName() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Long, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Long, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Genre.GENRE.ID;
    }

    @Override
    public Field<String> field2() {
        return Genre.GENRE.NAME;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getName();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getName();
    }

    @Override
    public GenreRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public GenreRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public GenreRecord values(Long value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    public void from(IGenre from) {
        setId(from.getId());
        setName(from.getName());
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached GenreRecord
     */
    public GenreRecord() {
        super(Genre.GENRE);
    }

    /**
     * Create a detached, initialised GenreRecord
     */
    public GenreRecord(Long id, String name) {
        super(Genre.GENRE);

        setId(id);
        setName(name);
    }

    /**
     * Create a detached, initialised GenreRecord
     */
    public GenreRecord(io.github.asm0dey.opdsko.jooq.tables.pojos.Genre value) {
        super(Genre.GENRE);

        if (value != null) {
            setId(value.getId());
            setName(value.getName());
        }
    }
}
