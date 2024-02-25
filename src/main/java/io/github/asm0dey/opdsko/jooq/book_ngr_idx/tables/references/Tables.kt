/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.references


import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Highlight
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Rank
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.RankHybrid
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Schema
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Search
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.records.HighlightRecord
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.records.RankHybridRecord
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.records.RankRecord
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.records.SchemaRecord

import org.jooq.Configuration
import org.jooq.Field
import org.jooq.Result



/**
 * The table <code>book_ngr_idx.highlight</code>.
 */
val HIGHLIGHT: Highlight = Highlight.HIGHLIGHT

/**
 * Call <code>book_ngr_idx.highlight</code>.
 */
fun HIGHLIGHT(
      configuration: Configuration
    , query: String?
    , offsetRows: Int?
    , limitRows: Int?
    , fuzzyFields: String?
    , distance: Int?
    , transposeCostOne: Boolean?
    , prefix: Boolean?
    , regexFields: String?
    , maxNumChars: Int?
    , highlightField: String?
): Result<HighlightRecord> = configuration.dsl().selectFrom(io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Highlight.HIGHLIGHT.call(
      query
    , offsetRows
    , limitRows
    , fuzzyFields
    , distance
    , transposeCostOne
    , prefix
    , regexFields
    , maxNumChars
    , highlightField
)).fetch()

/**
 * Get <code>book_ngr_idx.highlight</code> as a table.
 */
fun HIGHLIGHT(
      query: String?
    , offsetRows: Int?
    , limitRows: Int?
    , fuzzyFields: String?
    , distance: Int?
    , transposeCostOne: Boolean?
    , prefix: Boolean?
    , regexFields: String?
    , maxNumChars: Int?
    , highlightField: String?
): Highlight = io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Highlight.HIGHLIGHT.call(
    query,
    offsetRows,
    limitRows,
    fuzzyFields,
    distance,
    transposeCostOne,
    prefix,
    regexFields,
    maxNumChars,
    highlightField
)

/**
 * Get <code>book_ngr_idx.highlight</code> as a table.
 */
fun HIGHLIGHT(
      query: Field<String?>
    , offsetRows: Field<Int?>
    , limitRows: Field<Int?>
    , fuzzyFields: Field<String?>
    , distance: Field<Int?>
    , transposeCostOne: Field<Boolean?>
    , prefix: Field<Boolean?>
    , regexFields: Field<String?>
    , maxNumChars: Field<Int?>
    , highlightField: Field<String?>
): Highlight = io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Highlight.HIGHLIGHT.call(
    query,
    offsetRows,
    limitRows,
    fuzzyFields,
    distance,
    transposeCostOne,
    prefix,
    regexFields,
    maxNumChars,
    highlightField
)

/**
 * The table <code>book_ngr_idx.rank</code>.
 */
val RANK: Rank = Rank.RANK

/**
 * Call <code>book_ngr_idx.rank</code>.
 */
fun RANK(
      configuration: Configuration
    , query: String?
    , offsetRows: Int?
    , limitRows: Int?
    , fuzzyFields: String?
    , distance: Int?
    , transposeCostOne: Boolean?
    , prefix: Boolean?
    , regexFields: String?
    , maxNumChars: Int?
    , highlightField: String?
): Result<RankRecord> = configuration.dsl().selectFrom(io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Rank.RANK.call(
      query
    , offsetRows
    , limitRows
    , fuzzyFields
    , distance
    , transposeCostOne
    , prefix
    , regexFields
    , maxNumChars
    , highlightField
)).fetch()

/**
 * Get <code>book_ngr_idx.rank</code> as a table.
 */
fun RANK(
      query: String?
    , offsetRows: Int?
    , limitRows: Int?
    , fuzzyFields: String?
    , distance: Int?
    , transposeCostOne: Boolean?
    , prefix: Boolean?
    , regexFields: String?
    , maxNumChars: Int?
    , highlightField: String?
): Rank = io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Rank.RANK.call(
    query,
    offsetRows,
    limitRows,
    fuzzyFields,
    distance,
    transposeCostOne,
    prefix,
    regexFields,
    maxNumChars,
    highlightField
)

/**
 * Get <code>book_ngr_idx.rank</code> as a table.
 */
fun RANK(
      query: Field<String?>
    , offsetRows: Field<Int?>
    , limitRows: Field<Int?>
    , fuzzyFields: Field<String?>
    , distance: Field<Int?>
    , transposeCostOne: Field<Boolean?>
    , prefix: Field<Boolean?>
    , regexFields: Field<String?>
    , maxNumChars: Field<Int?>
    , highlightField: Field<String?>
): Rank = io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Rank.RANK.call(
    query,
    offsetRows,
    limitRows,
    fuzzyFields,
    distance,
    transposeCostOne,
    prefix,
    regexFields,
    maxNumChars,
    highlightField
)

/**
 * The table <code>book_ngr_idx.rank_hybrid</code>.
 */
val RANK_HYBRID: RankHybrid = RankHybrid.RANK_HYBRID

/**
 * Call <code>book_ngr_idx.rank_hybrid</code>.
 */
fun RANK_HYBRID(
      configuration: Configuration
    , bm25Query: String?
    , similarityQuery: String?
    , similarityLimitN: Int?
    , bm25LimitN: Int?
    , similarityWeight: Float?
    , bm25Weight: Float?
): Result<RankHybridRecord> = configuration.dsl().selectFrom(io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.RankHybrid.RANK_HYBRID.call(
      bm25Query
    , similarityQuery
    , similarityLimitN
    , bm25LimitN
    , similarityWeight
    , bm25Weight
)).fetch()

/**
 * Get <code>book_ngr_idx.rank_hybrid</code> as a table.
 */
fun RANK_HYBRID(
      bm25Query: String?
    , similarityQuery: String?
    , similarityLimitN: Int?
    , bm25LimitN: Int?
    , similarityWeight: Float?
    , bm25Weight: Float?
): RankHybrid = io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.RankHybrid.RANK_HYBRID.call(
    bm25Query,
    similarityQuery,
    similarityLimitN,
    bm25LimitN,
    similarityWeight,
    bm25Weight
)

/**
 * Get <code>book_ngr_idx.rank_hybrid</code> as a table.
 */
fun RANK_HYBRID(
      bm25Query: Field<String?>
    , similarityQuery: Field<String?>
    , similarityLimitN: Field<Int?>
    , bm25LimitN: Field<Int?>
    , similarityWeight: Field<Float?>
    , bm25Weight: Field<Float?>
): RankHybrid = io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.RankHybrid.RANK_HYBRID.call(
    bm25Query,
    similarityQuery,
    similarityLimitN,
    bm25LimitN,
    similarityWeight,
    bm25Weight
)

/**
 * The table <code>book_ngr_idx.schema</code>.
 */
val SCHEMA: Schema = Schema.SCHEMA

/**
 * Call <code>book_ngr_idx.schema</code>.
 */
fun SCHEMA(
      configuration: Configuration
): Result<SchemaRecord> = configuration.dsl().selectFrom(io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Schema.SCHEMA.call(
)).fetch()

/**
 * Get <code>book_ngr_idx.schema</code> as a table.
 */
fun SCHEMA(): Schema = io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Schema.SCHEMA.call(
)

/**
 * The table <code>book_ngr_idx.search</code>.
 */
val SEARCH: Search = Search.SEARCH

/**
 * Call <code>book_ngr_idx.search</code>.
 */
fun SEARCH(
      configuration: Configuration
    , query: String?
    , offsetRows: Int?
    , limitRows: Int?
    , fuzzyFields: String?
    , distance: Int?
    , transposeCostOne: Boolean?
    , prefix: Boolean?
    , regexFields: String?
    , maxNumChars: Int?
    , highlightField: String?
): Result<io.github.asm0dey.opdsko.jooq.`public`.tables.records.BookRecord> = configuration.dsl().selectFrom(io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Search.SEARCH.call(
      query
    , offsetRows
    , limitRows
    , fuzzyFields
    , distance
    , transposeCostOne
    , prefix
    , regexFields
    , maxNumChars
    , highlightField
)).fetch()

/**
 * Get <code>book_ngr_idx.search</code> as a table.
 */
fun SEARCH(
      query: String?
    , offsetRows: Int?
    , limitRows: Int?
    , fuzzyFields: String?
    , distance: Int?
    , transposeCostOne: Boolean?
    , prefix: Boolean?
    , regexFields: String?
    , maxNumChars: Int?
    , highlightField: String?
): Search = io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Search.SEARCH.call(
    query,
    offsetRows,
    limitRows,
    fuzzyFields,
    distance,
    transposeCostOne,
    prefix,
    regexFields,
    maxNumChars,
    highlightField
)

/**
 * Get <code>book_ngr_idx.search</code> as a table.
 */
fun SEARCH(
      query: Field<String?>
    , offsetRows: Field<Int?>
    , limitRows: Field<Int?>
    , fuzzyFields: Field<String?>
    , distance: Field<Int?>
    , transposeCostOne: Field<Boolean?>
    , prefix: Field<Boolean?>
    , regexFields: Field<String?>
    , maxNumChars: Field<Int?>
    , highlightField: Field<String?>
): Search = io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Search.SEARCH.call(
    query,
    offsetRows,
    limitRows,
    fuzzyFields,
    distance,
    transposeCostOne,
    prefix,
    regexFields,
    maxNumChars,
    highlightField
)