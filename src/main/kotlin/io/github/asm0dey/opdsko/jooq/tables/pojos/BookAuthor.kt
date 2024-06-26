/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.pojos


import io.github.asm0dey.opdsko.jooq.tables.interfaces.IBookAuthor


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class BookAuthor(
    override val bookId: Long,
    override val authorId: Long
): IBookAuthor {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: BookAuthor = other as BookAuthor
        if (this.bookId != o.bookId)
            return false
        if (this.authorId != o.authorId)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + this.bookId.hashCode()
        result = prime * result + this.authorId.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("BookAuthor (")

        sb.append(bookId)
        sb.append(", ").append(authorId)

        sb.append(")")
        return sb.toString()
    }
}
