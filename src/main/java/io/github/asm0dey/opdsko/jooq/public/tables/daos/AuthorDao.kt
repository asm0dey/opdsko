/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.`public`.tables.daos


import java.time.OffsetDateTime

import kotlin.collections.List

import org.jooq.Configuration
import org.jooq.impl.DAOImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class AuthorDao(configuration: Configuration?) : DAOImpl<io.github.asm0dey.opdsko.jooq.`public`.tables.records.AuthorRecord, io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author, Long>(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR, io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author::class.java, configuration) {

    /**
     * Create a new AuthorDao without any configuration
     */
    constructor(): this(null)

    override fun getId(o: io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author): Long? = o.id

    /**
     * Fetch records that have <code>id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    fun fetchRangeOfId(lowerInclusive: Long?, upperInclusive: Long?): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetchRange(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.ID, lowerInclusive, upperInclusive)

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    fun fetchById(vararg values: Long): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetch(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.ID, *values.toTypedArray())

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    fun fetchOneById(value: Long): io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author? = fetchOne(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.ID, value)

    /**
     * Fetch records that have <code>fb2id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    fun fetchRangeOfFb2id(lowerInclusive: Int?, upperInclusive: Int?): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetchRange(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.FB2ID, lowerInclusive, upperInclusive)

    /**
     * Fetch records that have <code>fb2id IN (values)</code>
     */
    fun fetchByFb2id(vararg values: Int): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetch(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.FB2ID, *values.toTypedArray())

    /**
     * Fetch records that have <code>first_name BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    fun fetchRangeOfFirstName(lowerInclusive: String?, upperInclusive: String?): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetchRange(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.FIRST_NAME, lowerInclusive, upperInclusive)

    /**
     * Fetch records that have <code>first_name IN (values)</code>
     */
    fun fetchByFirstName(vararg values: String): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetch(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.FIRST_NAME, *values)

    /**
     * Fetch records that have <code>middle_name BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    fun fetchRangeOfMiddleName(lowerInclusive: String?, upperInclusive: String?): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetchRange(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.MIDDLE_NAME, lowerInclusive, upperInclusive)

    /**
     * Fetch records that have <code>middle_name IN (values)</code>
     */
    fun fetchByMiddleName(vararg values: String): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetch(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.MIDDLE_NAME, *values)

    /**
     * Fetch records that have <code>last_name BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    fun fetchRangeOfLastName(lowerInclusive: String?, upperInclusive: String?): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetchRange(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.LAST_NAME, lowerInclusive, upperInclusive)

    /**
     * Fetch records that have <code>last_name IN (values)</code>
     */
    fun fetchByLastName(vararg values: String): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetch(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.LAST_NAME, *values)

    /**
     * Fetch records that have <code>nickname BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    fun fetchRangeOfNickname(lowerInclusive: String?, upperInclusive: String?): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetchRange(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.NICKNAME, lowerInclusive, upperInclusive)

    /**
     * Fetch records that have <code>nickname IN (values)</code>
     */
    fun fetchByNickname(vararg values: String): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetch(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.NICKNAME, *values)

    /**
     * Fetch records that have <code>added BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    fun fetchRangeOfAdded(lowerInclusive: OffsetDateTime?, upperInclusive: OffsetDateTime?): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetchRange(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.ADDED, lowerInclusive, upperInclusive)

    /**
     * Fetch records that have <code>added IN (values)</code>
     */
    fun fetchByAdded(vararg values: OffsetDateTime): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetch(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.ADDED, *values)

    /**
     * Fetch records that have <code>full_name BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    fun fetchRangeOfFullName(lowerInclusive: String?, upperInclusive: String?): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetchRange(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.FULL_NAME, lowerInclusive, upperInclusive)

    /**
     * Fetch records that have <code>full_name IN (values)</code>
     */
    fun fetchByFullName(vararg values: String): List<io.github.asm0dey.opdsko.jooq.`public`.tables.pojos.Author> = fetch(io.github.asm0dey.opdsko.jooq.`public`.tables.Author.AUTHOR.FULL_NAME, *values)
}
