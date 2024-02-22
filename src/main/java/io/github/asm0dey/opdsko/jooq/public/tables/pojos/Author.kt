/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.`public`.tables.pojos


import java.time.OffsetDateTime


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class Author(
    override val id: Long? = null,
    override val fb2id: Int? = null,
    override val firstName: String? = null,
    override val middleName: String? = null,
    override val lastName: String? = null,
    override val nickname: String? = null,
    override val added: OffsetDateTime? = null,
    override val fullName: String? = null
): io.github.asm0dey.opdsko.jooq.`public`.tables.interfaces.IAuthor {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: Author = other as Author
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.fb2id == null) {
            if (o.fb2id != null)
                return false
        }
        else if (this.fb2id != o.fb2id)
            return false
        if (this.firstName == null) {
            if (o.firstName != null)
                return false
        }
        else if (this.firstName != o.firstName)
            return false
        if (this.middleName == null) {
            if (o.middleName != null)
                return false
        }
        else if (this.middleName != o.middleName)
            return false
        if (this.lastName == null) {
            if (o.lastName != null)
                return false
        }
        else if (this.lastName != o.lastName)
            return false
        if (this.nickname == null) {
            if (o.nickname != null)
                return false
        }
        else if (this.nickname != o.nickname)
            return false
        if (this.added == null) {
            if (o.added != null)
                return false
        }
        else if (this.added != o.added)
            return false
        if (this.fullName == null) {
            if (o.fullName != null)
                return false
        }
        else if (this.fullName != o.fullName)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + (if (this.fb2id == null) 0 else this.fb2id.hashCode())
        result = prime * result + (if (this.firstName == null) 0 else this.firstName.hashCode())
        result = prime * result + (if (this.middleName == null) 0 else this.middleName.hashCode())
        result = prime * result + (if (this.lastName == null) 0 else this.lastName.hashCode())
        result = prime * result + (if (this.nickname == null) 0 else this.nickname.hashCode())
        result = prime * result + (if (this.added == null) 0 else this.added.hashCode())
        result = prime * result + (if (this.fullName == null) 0 else this.fullName.hashCode())
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("Author (")

        sb.append(id)
        sb.append(", ").append(fb2id)
        sb.append(", ").append(firstName)
        sb.append(", ").append(middleName)
        sb.append(", ").append(lastName)
        sb.append(", ").append(nickname)
        sb.append(", ").append(added)
        sb.append(", ").append(fullName)

        sb.append(")")
        return sb.toString()
    }
}
