/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables;


import io.github.asm0dey.opdsko.jooq.DefaultSchema;
import io.github.asm0dey.opdsko.jooq.tables.records.AuthorsFtsRecord;

import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function4;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.SelectField;
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
public class AuthorsFts extends TableImpl<AuthorsFtsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>authors_fts</code>
     */
    public static final AuthorsFts AUTHORS_FTS = new AuthorsFts();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AuthorsFtsRecord> getRecordType() {
        return AuthorsFtsRecord.class;
    }

    /**
     * The column <code>authors_fts.last_name</code>.
     */
    public final TableField<AuthorsFtsRecord, String> LAST_NAME = createField(DSL.name("last_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>authors_fts.first_name</code>.
     */
    public final TableField<AuthorsFtsRecord, String> FIRST_NAME = createField(DSL.name("first_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>authors_fts.middle_name</code>.
     */
    public final TableField<AuthorsFtsRecord, String> MIDDLE_NAME = createField(DSL.name("middle_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>authors_fts.nickname</code>.
     */
    public final TableField<AuthorsFtsRecord, String> NICKNAME = createField(DSL.name("nickname"), SQLDataType.CLOB, this, "");

    private AuthorsFts(Name alias, Table<AuthorsFtsRecord> aliased) {
        this(alias, aliased, null);
    }

    private AuthorsFts(Name alias, Table<AuthorsFtsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>authors_fts</code> table reference
     */
    public AuthorsFts(String alias) {
        this(DSL.name(alias), AUTHORS_FTS);
    }

    /**
     * Create an aliased <code>authors_fts</code> table reference
     */
    public AuthorsFts(Name alias) {
        this(alias, AUTHORS_FTS);
    }

    /**
     * Create a <code>authors_fts</code> table reference
     */
    public AuthorsFts() {
        this(DSL.name("authors_fts"), null);
    }

    public <O extends Record> AuthorsFts(Table<O> child, ForeignKey<O, AuthorsFtsRecord> key) {
        super(child, key, AUTHORS_FTS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public AuthorsFts as(String alias) {
        return new AuthorsFts(DSL.name(alias), this);
    }

    @Override
    public AuthorsFts as(Name alias) {
        return new AuthorsFts(alias, this);
    }

    @Override
    public AuthorsFts as(Table<?> alias) {
        return new AuthorsFts(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public AuthorsFts rename(String name) {
        return new AuthorsFts(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AuthorsFts rename(Name name) {
        return new AuthorsFts(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public AuthorsFts rename(Table<?> name) {
        return new AuthorsFts(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<String, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function4<? super String, ? super String, ? super String, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function4<? super String, ? super String, ? super String, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
