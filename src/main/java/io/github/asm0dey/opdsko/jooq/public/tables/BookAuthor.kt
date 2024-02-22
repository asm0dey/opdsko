/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.`public`.tables


import kotlin.collections.Collection
import kotlin.collections.List

import org.jooq.Condition
import org.jooq.Field
import org.jooq.ForeignKey
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
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class BookAuthor(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?,
    parentPath: InverseForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?,
    aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
): TableImpl<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>(
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
         * The reference instance of <code>public.book_author</code>
         */
        val BOOK_AUTHOR: BookAuthor = BookAuthor()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord> = io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord::class.java

    /**
     * The column <code>public.book_author.book_id</code>.
     */
    val BOOK_ID: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord, Long?> = createField(DSL.name("book_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>public.book_author.author_id</code>.
     */
    val AUTHOR_ID: TableField<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord, Long?> = createField(DSL.name("author_id"), SQLDataType.BIGINT.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?): this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, null, aliased, parameters, null)
    private constructor(alias: Name, aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?, where: Condition?): this(alias, null, null, null, aliased, null, where)

    /**
     * Create an aliased <code>public.book_author</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>public.book_author</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>public.book_author</code> table reference
     */
    constructor(): this(DSL.name("book_author"), null)

    constructor(path: Table<out Record>, childPath: ForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?, parentPath: InverseForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?): this(Internal.createPathAlias(path, childPath, parentPath), path, childPath, parentPath, BOOK_AUTHOR, null, null)

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    open class BookAuthorPath : BookAuthor, Path<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord> {
        constructor(path: Table<out Record>, childPath: ForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?, parentPath: InverseForeignKey<out Record, io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>?): super(path, childPath, parentPath)
        private constructor(alias: Name, aliased: Table<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord>): super(alias, aliased)
        override fun `as`(alias: String): BookAuthorPath = BookAuthorPath(DSL.name(alias), this)
        override fun `as`(alias: Name): BookAuthorPath = BookAuthorPath(alias, this)
        override fun `as`(alias: Table<*>): BookAuthorPath = BookAuthorPath(alias.qualifiedName, this)
    }
    override fun getSchema(): Schema? = if (aliased()) null else io.github.asm0dey.opdsko.jooq.`public`.Public.PUBLIC
    override fun getIndexes(): List<Index> = listOf(io.github.asm0dey.opdsko.jooq.`public`.indexes.BOOK_AUTHOR_AUTHOR_IDX, io.github.asm0dey.opdsko.jooq.`public`.indexes.BOOK_AUTHOR_BOOK_AUTHOR_IDX, io.github.asm0dey.opdsko.jooq.`public`.indexes.BOOK_AUTHOR_BOOK_IDX)
    override fun getReferences(): List<ForeignKey<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookAuthorRecord, *>> = listOf(io.github.asm0dey.opdsko.jooq.`public`.keys.BOOK_AUTHOR__BOOK_AUTHOR_BOOK_ID_FKEY, io.github.asm0dey.opdsko.jooq.`public`.keys.BOOK_AUTHOR__BOOK_AUTHOR_AUTHOR_ID_FKEY)

    private lateinit var _book: io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BookPath

    /**
     * Get the implicit join path to the <code>public.book</code> table.
     */
    fun book(): io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BookPath {
        if (!this::_book.isInitialized)
            _book = io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BookPath(this, io.github.asm0dey.opdsko.jooq.`public`.keys.BOOK_AUTHOR__BOOK_AUTHOR_BOOK_ID_FKEY, null)

        return _book;
    }

    val book: io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BookPath
        get(): io.github.asm0dey.opdsko.jooq.`public`.tables.Book.BookPath = book()

    private lateinit var _author: io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AuthorPath

    /**
     * Get the implicit join path to the <code>public.author</code> table.
     */
    fun author(): io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AuthorPath {
        if (!this::_author.isInitialized)
            _author = io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AuthorPath(this, io.github.asm0dey.opdsko.jooq.`public`.keys.BOOK_AUTHOR__BOOK_AUTHOR_AUTHOR_ID_FKEY, null)

        return _author;
    }

    val author: io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AuthorPath
        get(): io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AuthorPath = author()
    override fun `as`(alias: String): BookAuthor = BookAuthor(DSL.name(alias), this)
    override fun `as`(alias: Name): BookAuthor = BookAuthor(alias, this)
    override fun `as`(alias: Table<*>): BookAuthor = BookAuthor(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): BookAuthor = BookAuthor(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): BookAuthor = BookAuthor(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): BookAuthor = BookAuthor(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition?): BookAuthor = BookAuthor(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): BookAuthor = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition?): BookAuthor = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>?): BookAuthor = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(condition: SQL): BookAuthor = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String): BookAuthor = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg binds: Any?): BookAuthor = where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): BookAuthor = where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): BookAuthor = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): BookAuthor = where(DSL.notExists(select))
}
