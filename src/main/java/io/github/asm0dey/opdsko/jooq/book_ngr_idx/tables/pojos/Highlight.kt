/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.pojos


import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.interfaces.IHighlight


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class Highlight(
    override val id: Long? = null,
    override val highlightBm25: String? = null
): IHighlight {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: Highlight = other as Highlight
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.highlightBm25 == null) {
            if (o.highlightBm25 != null)
                return false
        }
        else if (this.highlightBm25 != o.highlightBm25)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + (if (this.highlightBm25 == null) 0 else this.highlightBm25.hashCode())
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("Highlight (")

        sb.append(id)
        sb.append(", ").append(highlightBm25)

        sb.append(")")
        return sb.toString()
    }
}
