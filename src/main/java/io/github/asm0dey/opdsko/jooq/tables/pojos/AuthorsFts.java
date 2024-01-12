/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.pojos;


import io.github.asm0dey.opdsko.jooq.tables.interfaces.IAuthorsFts;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class AuthorsFts implements IAuthorsFts {

    private static final long serialVersionUID = 1L;

    private final String lastName;
    private final String firstName;
    private final String middleName;
    private final String nickname;

    public AuthorsFts(IAuthorsFts value) {
        this.lastName = value.getLastName();
        this.firstName = value.getFirstName();
        this.middleName = value.getMiddleName();
        this.nickname = value.getNickname();
    }

    public AuthorsFts(
        String lastName,
        String firstName,
        String middleName,
        String nickname
    ) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.nickname = nickname;
    }

    /**
     * Getter for <code>authors_fts.last_name</code>.
     */
    @Override
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Getter for <code>authors_fts.first_name</code>.
     */
    @Override
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Getter for <code>authors_fts.middle_name</code>.
     */
    @Override
    public String getMiddleName() {
        return this.middleName;
    }

    /**
     * Getter for <code>authors_fts.nickname</code>.
     */
    @Override
    public String getNickname() {
        return this.nickname;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AuthorsFts other = (AuthorsFts) obj;
        if (this.lastName == null) {
            if (other.lastName != null)
                return false;
        }
        else if (!this.lastName.equals(other.lastName))
            return false;
        if (this.firstName == null) {
            if (other.firstName != null)
                return false;
        }
        else if (!this.firstName.equals(other.firstName))
            return false;
        if (this.middleName == null) {
            if (other.middleName != null)
                return false;
        }
        else if (!this.middleName.equals(other.middleName))
            return false;
        if (this.nickname == null) {
            if (other.nickname != null)
                return false;
        }
        else if (!this.nickname.equals(other.nickname))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.lastName == null) ? 0 : this.lastName.hashCode());
        result = prime * result + ((this.firstName == null) ? 0 : this.firstName.hashCode());
        result = prime * result + ((this.middleName == null) ? 0 : this.middleName.hashCode());
        result = prime * result + ((this.nickname == null) ? 0 : this.nickname.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AuthorsFts (");

        sb.append(lastName);
        sb.append(", ").append(firstName);
        sb.append(", ").append(middleName);
        sb.append(", ").append(nickname);

        sb.append(")");
        return sb.toString();
    }
}
