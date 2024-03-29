/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.`public`.tables


import java.time.OffsetDateTime

import kotlin.collections.Collection
import kotlin.collections.List

import org.jooq.Condition
import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Identity
import org.jooq.Index
import org.jooq.InverseForeignKey
import org.jooq.Name
import org.jooq.Path
import org.jooq.PlainSQL
import org.jooq.QueryPart
import org.jooq.Record
import org.jooq.SQL
import org.jooq.Schema
import org.jooq.Select
import org.jooq.Stringly
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Book(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?,
    parentPath: InverseForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?,
    aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
): TableImpl<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>(
    alias,
    io.github.asm0dey.opdsko.jooq.`public`.Public.PUBLIC,
    path,
    childPath,
    parentPath,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table(),
    where,
) {
    companion object {

        /**
         * The reference instance of <code>public.book</code>
         */
        val BOOK: Book = Book()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord> = io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord::class.java

    /**
     * The column <code>public.book.id</code>.
     */
    val ID: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "")

    /**
     * The column <code>public.book.path</code>.
     */
    val PATH: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, String?> = createField(DSL.name("path"), SQLDataType.CLOB.nullable(false), this, "")

    /**
     * The column <code>public.book.name</code>.
     */
    val NAME: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, String?> = createField(DSL.name("name"), SQLDataType.CLOB.nullable(false), this, "")

    /**
     * The column <code>public.book.date</code>.
     */
    val DATE: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, String?> = createField(DSL.name("date"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>public.book.added</code>.
     */
    val ADDED: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, OffsetDateTime?> = createField(DSL.name("added"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP"), SQLDataType.TIMESTAMPWITHTIMEZONE)), this, "")

    /**
     * The column <code>public.book.sequence</code>.
     */
    val SEQUENCE: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, String?> = createField(DSL.name("sequence"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>public.book.sequence_number</code>.
     */
    val SEQUENCE_NUMBER: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, Long?> = createField(DSL.name("sequence_number"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>public.book.lang</code>.
     */
    val LANG: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, String?> = createField(DSL.name("lang"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>public.book.zip_file</code>.
     */
    val ZIP_FILE: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, String?> = createField(DSL.name("zip_file"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>public.book.seqid</code>.
     */
    val SEQID: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, Int?> = createField(DSL.name("seqid"), SQLDataType.INTEGER, this, "")

    private constructor(alias: Name, aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?): this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, null, aliased, parameters, null)
    private constructor(alias: Name, aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?, where: Condition?): this(alias, null, null, null, aliased, null, where)

    /**
     * Create an aliased <code>public.book</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>public.book</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>public.book</code> table reference
     */
    constructor(): this(DSL.name("book"), null)

    constructor(path: Table<out Record>, childPath: ForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?, parentPath: InverseForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?): this(Internal.createPathAlias(path, childPath, parentPath), path, childPath, parentPath, BOOK, null, null)

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    open class BookPath : Book, Path<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord> {
        constructor(path: Table<out Record>, childPath: ForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?, parentPath: InverseForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>?): super(path, childPath, parentPath)
        private constructor(alias: Name, aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord>): super(alias, aliased)
        override fun `as`(alias: String): BookPath = BookPath(DSL.name(alias), this)
        override fun `as`(alias: Name): BookPath = BookPath(alias, this)
        override fun `as`(alias: Table<*>): BookPath = BookPath(alias.qualifiedName, this)
    }
    override fun getSchema(): Schema? = if (aliased()) null else io.github.asm0dey.opdsko.jooq.`public`.Public.PUBLIC
    override fun getIndexes(): List<Index> = listOf(io.github.asm0dey.opdsko.jooq.`public`.indexes.BOOK_ADDED_IDX, io.github.asm0dey.opdsko.jooq.`public`.indexes.BOOK_PATH_ARCHIVE, io.github.asm0dey.opdsko.jooq.`public`.indexes.BOOK_SEQUENCE_IDX, io.github.asm0dey.opdsko.jooq.`public`.indexes.NEWONE)
    override fun getIdentity(): Identity<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, Long?> = super.getIdentity() as Identity<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord> = io.github.asm0dey.opdsko.jooq.`public`.keys.BOOK_PKEY

    private lateinit var _bookAuthor: io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BookAuthorPath

    /**
     * Get the implicit to-many join path to the <code>public.book_author</code>
     * table
     */
    fun bookAuthor(): io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BookAuthorPath {
        if (!this::_bookAuthor.isInitialized)
            _bookAuthor = io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BookAuthorPath(this, null, io.github.asm0dey.opdsko.jooq.`public`.keys.BOOK_AUTHOR__BOOK_AUTHOR_BOOK_ID_FKEY.inverseKey)

        return _bookAuthor;
    }

    val bookAuthor: io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BookAuthorPath
        get(): io.github.asm0dey.opdsko.jooq.`public`.tables.BookAuthor.BookAuthorPath = bookAuthor()

    private lateinit var _bookGenre: io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BookGenrePath

    /**
     * Get the implicit to-many join path to the <code>public.book_genre</code>
     * table
     */
    fun bookGenre(): io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BookGenrePath {
        if (!this::_bookGenre.isInitialized)
            _bookGenre = io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BookGenrePath(this, null, io.github.asm0dey.opdsko.jooq.`public`.keys.BOOK_GENRE__BOOK_GENRE_BOOK_ID_FKEY.inverseKey)

        return _bookGenre;
    }

    val bookGenre: io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BookGenrePath
        get(): io.github.asm0dey.opdsko.jooq.`public`.tables.BookGenre.BookGenrePath = bookGenre()
    override fun `as`(alias: String): Book = Book(DSL.name(alias), this)
    override fun `as`(alias: Name): Book = Book(alias, this)
    override fun `as`(alias: Table<*>): Book = Book(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Book = Book(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Book = Book(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): Book = Book(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition?): Book = Book(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): Book = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition?): Book = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>?): Book = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(condition: SQL): Book = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String): Book = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg binds: Any?): Book = where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): Book = where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): Book = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): Book = where(DSL.notExists(select))
}
