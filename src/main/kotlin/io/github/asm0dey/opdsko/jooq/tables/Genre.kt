/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables


import io.github.asm0dey.opdsko.jooq.DefaultSchema
import io.github.asm0dey.opdsko.jooq.indexes.GENRE_NAME
import io.github.asm0dey.opdsko.jooq.keys.BOOK_GENRE__FK_BOOK_GENRE_PK_GENRE
import io.github.asm0dey.opdsko.jooq.keys.GENRE__PK_GENRE
import io.github.asm0dey.opdsko.jooq.keys.GENRE__UK_GENRE_1_134034859
import io.github.asm0dey.opdsko.jooq.tables.Book.BookPath
import io.github.asm0dey.opdsko.jooq.tables.BookGenre.BookGenrePath
import io.github.asm0dey.opdsko.jooq.tables.records.GenreRecord

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
open class Genre(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, GenreRecord>?,
    parentPath: InverseForeignKey<out Record, GenreRecord>?,
    aliased: Table<GenreRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
): TableImpl<GenreRecord>(
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
         * The reference instance of <code>genre</code>
         */
        val GENRE: Genre = Genre()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<GenreRecord> = GenreRecord::class.java

    /**
     * The column <code>genre.id</code>.
     */
    val ID: TableField<GenreRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.identity(true), this, "")

    /**
     * The column <code>genre.name</code>.
     */
    val NAME: TableField<GenreRecord, String?> = createField(DSL.name("name"), SQLDataType.CLOB.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<GenreRecord>?): this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<GenreRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, null, aliased, parameters, null)
    private constructor(alias: Name, aliased: Table<GenreRecord>?, where: Condition?): this(alias, null, null, null, aliased, null, where)

    /**
     * Create an aliased <code>genre</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>genre</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>genre</code> table reference
     */
    constructor(): this(DSL.name("genre"), null)

    constructor(path: Table<out Record>, childPath: ForeignKey<out Record, GenreRecord>?, parentPath: InverseForeignKey<out Record, GenreRecord>?): this(Internal.createPathAlias(path, childPath, parentPath), path, childPath, parentPath, GENRE, null, null)

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    open class GenrePath : Genre, Path<GenreRecord> {
        constructor(path: Table<out Record>, childPath: ForeignKey<out Record, GenreRecord>?, parentPath: InverseForeignKey<out Record, GenreRecord>?): super(path, childPath, parentPath)
        private constructor(alias: Name, aliased: Table<GenreRecord>): super(alias, aliased)
        override fun `as`(alias: String): GenrePath = GenrePath(DSL.name(alias), this)
        override fun `as`(alias: Name): GenrePath = GenrePath(alias, this)
        override fun `as`(alias: Table<*>): GenrePath = GenrePath(alias.qualifiedName, this)
    }
    override fun getSchema(): Schema? = if (aliased()) null else DefaultSchema.DEFAULT_SCHEMA
    override fun getIndexes(): List<Index> = listOf(GENRE_NAME)
    override fun getIdentity(): Identity<GenreRecord, Long?> = super.getIdentity() as Identity<GenreRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<GenreRecord> = GENRE__PK_GENRE
    override fun getUniqueKeys(): List<UniqueKey<GenreRecord>> = listOf(GENRE__UK_GENRE_1_134034859)

    private lateinit var _bookGenre: BookGenrePath

    /**
     * Get the implicit to-many join path to the <code>book_genre</code> table
     */
    fun bookGenre(): BookGenrePath {
        if (!this::_bookGenre.isInitialized)
            _bookGenre = BookGenrePath(this, null, BOOK_GENRE__FK_BOOK_GENRE_PK_GENRE.inverseKey)

        return _bookGenre;
    }

    val bookGenre: BookGenrePath
        get(): BookGenrePath = bookGenre()

    /**
     * Get the implicit many-to-many join path to the <code>book</code> table
     */
    val book: BookPath
        get(): BookPath = bookGenre().book()
    override fun `as`(alias: String): Genre = Genre(DSL.name(alias), this)
    override fun `as`(alias: Name): Genre = Genre(alias, this)
    override fun `as`(alias: Table<*>): Genre = Genre(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Genre = Genre(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Genre = Genre(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): Genre = Genre(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition?): Genre = Genre(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): Genre = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition?): Genre = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>?): Genre = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(condition: SQL): Genre = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String): Genre = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg binds: Any?): Genre = where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): Genre = where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): Genre = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): Genre = where(DSL.notExists(select))
}
