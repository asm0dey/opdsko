/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.records;


import io.github.asm0dey.opdsko.jooq.tables.Author;
import io.github.asm0dey.opdsko.jooq.tables.interfaces.IAuthor;

import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AuthorRecord extends UpdatableRecordImpl<AuthorRecord> implements Record7<Long, String, String, String, String, String, LocalDateTime>, IAuthor {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>author.id</code>.
     */
    public AuthorRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>author.id</code>.
     */
    @Override
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>author.fb2id</code>.
     */
    public AuthorRecord setFb2id(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>author.fb2id</code>.
     */
    @Override
    public String getFb2id() {
        return (String) get(1);
    }

    /**
     * Setter for <code>author.first_name</code>.
     */
    public AuthorRecord setFirstName(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>author.first_name</code>.
     */
    @Override
    public String getFirstName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>author.middle_name</code>.
     */
    public AuthorRecord setMiddleName(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>author.middle_name</code>.
     */
    @Override
    public String getMiddleName() {
        return (String) get(3);
    }

    /**
     * Setter for <code>author.last_name</code>.
     */
    public AuthorRecord setLastName(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>author.last_name</code>.
     */
    @Override
    public String getLastName() {
        return (String) get(4);
    }

    /**
     * Setter for <code>author.nickname</code>.
     */
    public AuthorRecord setNickname(String value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>author.nickname</code>.
     */
    @Override
    public String getNickname() {
        return (String) get(5);
    }

    /**
     * Setter for <code>author.added</code>.
     */
    public AuthorRecord setAdded(LocalDateTime value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>author.added</code>.
     */
    @Override
    public LocalDateTime getAdded() {
        return (LocalDateTime) get(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row7<Long, String, String, String, String, String, LocalDateTime> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    @Override
    public Row7<Long, String, String, String, String, String, LocalDateTime> valuesRow() {
        return (Row7) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Author.AUTHOR.ID;
    }

    @Override
    public Field<String> field2() {
        return Author.AUTHOR.FB2ID;
    }

    @Override
    public Field<String> field3() {
        return Author.AUTHOR.FIRST_NAME;
    }

    @Override
    public Field<String> field4() {
        return Author.AUTHOR.MIDDLE_NAME;
    }

    @Override
    public Field<String> field5() {
        return Author.AUTHOR.LAST_NAME;
    }

    @Override
    public Field<String> field6() {
        return Author.AUTHOR.NICKNAME;
    }

    @Override
    public Field<LocalDateTime> field7() {
        return Author.AUTHOR.ADDED;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getFb2id();
    }

    @Override
    public String component3() {
        return getFirstName();
    }

    @Override
    public String component4() {
        return getMiddleName();
    }

    @Override
    public String component5() {
        return getLastName();
    }

    @Override
    public String component6() {
        return getNickname();
    }

    @Override
    public LocalDateTime component7() {
        return getAdded();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getFb2id();
    }

    @Override
    public String value3() {
        return getFirstName();
    }

    @Override
    public String value4() {
        return getMiddleName();
    }

    @Override
    public String value5() {
        return getLastName();
    }

    @Override
    public String value6() {
        return getNickname();
    }

    @Override
    public LocalDateTime value7() {
        return getAdded();
    }

    @Override
    public AuthorRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public AuthorRecord value2(String value) {
        setFb2id(value);
        return this;
    }

    @Override
    public AuthorRecord value3(String value) {
        setFirstName(value);
        return this;
    }

    @Override
    public AuthorRecord value4(String value) {
        setMiddleName(value);
        return this;
    }

    @Override
    public AuthorRecord value5(String value) {
        setLastName(value);
        return this;
    }

    @Override
    public AuthorRecord value6(String value) {
        setNickname(value);
        return this;
    }

    @Override
    public AuthorRecord value7(LocalDateTime value) {
        setAdded(value);
        return this;
    }

    @Override
    public AuthorRecord values(Long value1, String value2, String value3, String value4, String value5, String value6, LocalDateTime value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    public void from(IAuthor from) {
        setId(from.getId());
        setFb2id(from.getFb2id());
        setFirstName(from.getFirstName());
        setMiddleName(from.getMiddleName());
        setLastName(from.getLastName());
        setNickname(from.getNickname());
        setAdded(from.getAdded());
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached AuthorRecord
     */
    public AuthorRecord() {
        super(Author.AUTHOR);
    }

    /**
     * Create a detached, initialised AuthorRecord
     */
    public AuthorRecord(Long id, String fb2id, String firstName, String middleName, String lastName, String nickname, LocalDateTime added) {
        super(Author.AUTHOR);

        setId(id);
        setFb2id(fb2id);
        setFirstName(firstName);
        setMiddleName(middleName);
        setLastName(lastName);
        setNickname(nickname);
        setAdded(added);
    }

    /**
     * Create a detached, initialised AuthorRecord
     */
    public AuthorRecord(io.github.asm0dey.opdsko.jooq.tables.pojos.Author value) {
        super(Author.AUTHOR);

        if (value != null) {
            setId(value.getId());
            setFb2id(value.getFb2id());
            setFirstName(value.getFirstName());
            setMiddleName(value.getMiddleName());
            setLastName(value.getLastName());
            setNickname(value.getNickname());
            setAdded(value.getAdded());
        }
    }
}
