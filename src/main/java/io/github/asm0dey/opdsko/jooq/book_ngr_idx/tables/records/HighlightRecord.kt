/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.records


import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.Highlight
import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.interfaces.IHighlight

import org.jooq.impl.TableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class HighlightRecord private constructor() : TableRecordImpl<HighlightRecord>(Highlight.HIGHLIGHT), IHighlight {

    open override var id: Long?
        set(value): Unit = set(0, value)
        get(): Long? = get(0) as Long?

    open override var highlightBm25: String?
        set(value): Unit = set(1, value)
        get(): String? = get(1) as String?

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    fun from(from: IHighlight) {
        this.id = from.id
        this.highlightBm25 = from.highlightBm25
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised HighlightRecord
     */
    constructor(id: Long? = null, highlightBm25: String? = null): this() {
        this.id = id
        this.highlightBm25 = highlightBm25
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised HighlightRecord
     */
    constructor(value: io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.pojos.Highlight?): this() {
        if (value != null) {
            this.id = value.id
            this.highlightBm25 = value.highlightBm25
            resetChangedOnNotNull()
        }
    }
}