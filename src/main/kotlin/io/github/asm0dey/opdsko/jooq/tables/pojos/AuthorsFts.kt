/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.pojos


import io.github.asm0dey.opdsko.jooq.tables.interfaces.IAuthorsFts


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class AuthorsFts(
    override val lastName: String?,
    override val firstName: String?,
    override val middleName: String?,
    override val nickname: String?
): IAuthorsFts {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: AuthorsFts = other as AuthorsFts
        if (this.lastName == null) {
            if (o.lastName != null)
                return false
        }
        else if (this.lastName != o.lastName)
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
        if (this.nickname == null) {
            if (o.nickname != null)
                return false
        }
        else if (this.nickname != o.nickname)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.lastName == null) 0 else this.lastName.hashCode())
        result = prime * result + (if (this.firstName == null) 0 else this.firstName.hashCode())
        result = prime * result + (if (this.middleName == null) 0 else this.middleName.hashCode())
        result = prime * result + (if (this.nickname == null) 0 else this.nickname.hashCode())
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("AuthorsFts (")

        sb.append(lastName)
        sb.append(", ").append(firstName)
        sb.append(", ").append(middleName)
        sb.append(", ").append(nickname)

        sb.append(")")
        return sb.toString()
    }
}
