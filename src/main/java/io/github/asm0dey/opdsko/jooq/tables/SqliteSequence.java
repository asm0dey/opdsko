/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables;


import io.github.asm0dey.opdsko.jooq.DefaultSchema;
import io.github.asm0dey.opdsko.jooq.tables.records.SqliteSequenceRecord;

import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function2;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row2;
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
public class SqliteSequence extends TableImpl<SqliteSequenceRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>sqlite_sequence</code>
     */
    public static final SqliteSequence SQLITE_SEQUENCE = new SqliteSequence();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SqliteSequenceRecord> getRecordType() {
        return SqliteSequenceRecord.class;
    }

    /**
     * @deprecated Unknown data type. If this is a qualified, user-defined type,
     * it may have been excluded from code generation. If this is a built-in
     * type, you can define an explicit {@link org.jooq.Binding} to specify how
     * this type should be handled. Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @Deprecated
    public final TableField<SqliteSequenceRecord, Object> NAME = createField(DSL.name("name"), SQLDataType.OTHER, this, "");

    /**
     * @deprecated Unknown data type. If this is a qualified, user-defined type,
     * it may have been excluded from code generation. If this is a built-in
     * type, you can define an explicit {@link org.jooq.Binding} to specify how
     * this type should be handled. Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @Deprecated
    public final TableField<SqliteSequenceRecord, Object> SEQ = createField(DSL.name("seq"), SQLDataType.OTHER, this, "");

    private SqliteSequence(Name alias, Table<SqliteSequenceRecord> aliased) {
        this(alias, aliased, null);
    }

    private SqliteSequence(Name alias, Table<SqliteSequenceRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>sqlite_sequence</code> table reference
     */
    public SqliteSequence(String alias) {
        this(DSL.name(alias), SQLITE_SEQUENCE);
    }

    /**
     * Create an aliased <code>sqlite_sequence</code> table reference
     */
    public SqliteSequence(Name alias) {
        this(alias, SQLITE_SEQUENCE);
    }

    /**
     * Create a <code>sqlite_sequence</code> table reference
     */
    public SqliteSequence() {
        this(DSL.name("sqlite_sequence"), null);
    }

    public <O extends Record> SqliteSequence(Table<O> child, ForeignKey<O, SqliteSequenceRecord> key) {
        super(child, key, SQLITE_SEQUENCE);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public SqliteSequence as(String alias) {
        return new SqliteSequence(DSL.name(alias), this);
    }

    @Override
    public SqliteSequence as(Name alias) {
        return new SqliteSequence(alias, this);
    }

    @Override
    public SqliteSequence as(Table<?> alias) {
        return new SqliteSequence(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public SqliteSequence rename(String name) {
        return new SqliteSequence(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SqliteSequence rename(Name name) {
        return new SqliteSequence(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public SqliteSequence rename(Table<?> name) {
        return new SqliteSequence(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Object, Object> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function2<? super Object, ? super Object, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function2<? super Object, ? super Object, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
