/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.pojos


import io.github.asm0dey.opdsko.jooq.book_ngr_idx.tables.interfaces.IRank


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class Rank(
    override val id: Long? = null,
    override val rankBm25: Float? = null
): IRank {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: Rank = other as Rank
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.rankBm25 == null) {
            if (o.rankBm25 != null)
                return false
        }
        else if (this.rankBm25 != o.rankBm25)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + (if (this.rankBm25 == null) 0 else this.rankBm25.hashCode())
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("Rank (")

        sb.append(id)
        sb.append(", ").append(rankBm25)

        sb.append(")")
        return sb.toString()
    }
}
