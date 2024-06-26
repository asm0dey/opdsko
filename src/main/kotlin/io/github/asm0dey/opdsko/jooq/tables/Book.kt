/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables


import io.github.asm0dey.opdsko.jooq.DefaultSchema
import io.github.asm0dey.opdsko.jooq.indexes.BOOK_ADDED
import io.github.asm0dey.opdsko.jooq.indexes.BOOK_SEQ
import io.github.asm0dey.opdsko.jooq.indexes.BOOK_SEQUENCE_INDEX
import io.github.asm0dey.opdsko.jooq.keys.BOOK_AUTHOR__FK_BOOK_AUTHOR_PK_BOOK
import io.github.asm0dey.opdsko.jooq.keys.BOOK_GENRE__FK_BOOK_GENRE_PK_BOOK
import io.github.asm0dey.opdsko.jooq.keys.BOOK__PK_BOOK
import io.github.asm0dey.opdsko.jooq.tables.Author.AuthorPath
import io.github.asm0dey.opdsko.jooq.tables.BookAuthor.BookAuthorPath
import io.github.asm0dey.opdsko.jooq.tables.BookGenre.BookGenrePath
import io.github.asm0dey.opdsko.jooq.tables.Genre.GenrePath
import io.github.asm0dey.opdsko.jooq.tables.records.BookRecord

import java.time.LocalDateTime

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
    childPath: ForeignKey<out Record, BookRecord>?,
    parentPath: InverseForeignKey<out Record, BookRecord>?,
    aliased: Table<BookRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
): TableImpl<BookRecord>(
    alias,
    DefaultSchema.DEFAULT_SCHEMA,
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
         * The reference instance of <code>book</code>
         */
        val BOOK: Book = Book()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<BookRecord> = BookRecord::class.java

    /**
     * The column <code>book.id</code>.
     */
    val ID: TableField<BookRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.identity(true), this, "")

    /**
     * The column <code>book.path</code>.
     */
    val PATH: TableField<BookRecord, String?> = createField(DSL.name("path"), SQLDataType.CLOB.nullable(false), this, "")

    /**
     * The column <code>book.name</code>.
     */
    val NAME: TableField<BookRecord, String?> = createField(DSL.name("name"), SQLDataType.CLOB.nullable(false), this, "")

    /**
     * The column <code>book.date</code>.
     */
    val DATE: TableField<BookRecord, String?> = createField(DSL.name("date"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>book.added</code>.
     */
    val ADDED: TableField<BookRecord, LocalDateTime?> = createField(DSL.name("added"), SQLDataType.LOCALDATETIME(0).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP"), SQLDataType.LOCALDATETIME)), this, "")

    /**
     * The column <code>book.sequence</code>.
     */
    val SEQUENCE: TableField<BookRecord, String?> = createField(DSL.name("sequence"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>book.sequence_number</code>.
     */
    val SEQUENCE_NUMBER: TableField<BookRecord, Int?> = createField(DSL.name("sequence_number"), SQLDataType.INTEGER, this, "")

    /**
     * The column <code>book.lang</code>.
     */
    val LANG: TableField<BookRecord, String?> = createField(DSL.name("lang"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>book.zip_file</code>.
     */
    val ZIP_FILE: TableField<BookRecord, String?> = createField(DSL.name("zip_file"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>book.seqid</code>.
     */
    val SEQID: TableField<BookRecord, Int?> = createField(DSL.name("seqid"), SQLDataType.INTEGER, this, "")

    private constructor(alias: Name, aliased: Table<BookRecord>?): this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<BookRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, null, aliased, parameters, null)
    private constructor(alias: Name, aliased: Table<BookRecord>?, where: Condition?): this(alias, null, null, null, aliased, null, where)

    /**
     * Create an aliased <code>book</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>book</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>book</code> table reference
     */
    constructor(): this(DSL.name("book"), null)

    constructor(path: Table<out Record>, childPath: ForeignKey<out Record, BookRecord>?, parentPath: InverseForeignKey<out Record, BookRecord>?): this(Internal.createPathAlias(path, childPath, parentPath), path, childPath, parentPath, BOOK, null, null)

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    open class BookPath : Book, Path<BookRecord> {
        constructor(path: Table<out Record>, childPath: ForeignKey<out Record, BookRecord>?, parentPath: InverseForeignKey<out Record, BookRecord>?): super(path, childPath, parentPath)
        private constructor(alias: Name, aliased: Table<BookRecord>): super(alias, aliased)
        override fun `as`(alias: String): BookPath = BookPath(DSL.name(alias), this)
        override fun `as`(alias: Name): BookPath = BookPath(alias, this)
        override fun `as`(alias: Table<*>): BookPath = BookPath(alias.qualifiedName, this)
    }
    override fun getSchema(): Schema? = if (aliased()) null else DefaultSchema.DEFAULT_SCHEMA
    override fun getIndexes(): List<Index> = listOf(BOOK_ADDED, BOOK_SEQ, BOOK_SEQUENCE_INDEX)
    override fun getIdentity(): Identity<BookRecord, Long?> = super.getIdentity() as Identity<BookRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<BookRecord> = BOOK__PK_BOOK

    private lateinit var _bookAuthor: BookAuthorPath

    /**
     * Get the implicit to-many join path to the <code>book_author</code> table
     */
    fun bookAuthor(): BookAuthorPath {
        if (!this::_bookAuthor.isInitialized)
            _bookAuthor = BookAuthorPath(this, null, BOOK_AUTHOR__FK_BOOK_AUTHOR_PK_BOOK.inverseKey)

        return _bookAuthor;
    }

    val bookAuthor: BookAuthorPath
        get(): BookAuthorPath = bookAuthor()

    private lateinit var _bookGenre: BookGenrePath

    /**
     * Get the implicit to-many join path to the <code>book_genre</code> table
     */
    fun bookGenre(): BookGenrePath {
        if (!this::_bookGenre.isInitialized)
            _bookGenre = BookGenrePath(this, null, BOOK_GENRE__FK_BOOK_GENRE_PK_BOOK.inverseKey)

        return _bookGenre;
    }

    val bookGenre: BookGenrePath
        get(): BookGenrePath = bookGenre()

    /**
     * Get the implicit many-to-many join path to the <code>author</code> table
     */
    val author: AuthorPath
        get(): AuthorPath = bookAuthor().author()

    /**
     * Get the implicit many-to-many join path to the <code>genre</code> table
     */
    val genre: GenrePath
        get(): GenrePath = bookGenre().genre()
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
