/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.pojos;


import io.github.asm0dey.opdsko.jooq.tables.interfaces.IBook;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Book implements IBook {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String path;
    private final String name;
    private final String date;
    private final LocalDateTime added;
    private final String sequence;
    private final Integer sequenceNumber;
    private final String lang;
    private final String zipFile;
    private final Integer seqid;

    public Book(IBook value) {
        this.id = value.getId();
        this.path = value.getPath();
        this.name = value.getName();
        this.date = value.getDate();
        this.added = value.getAdded();
        this.sequence = value.getSequence();
        this.sequenceNumber = value.getSequenceNumber();
        this.lang = value.getLang();
        this.zipFile = value.getZipFile();
        this.seqid = value.getSeqid();
    }

    public Book(
        Long id,
        String path,
        String name,
        String date,
        LocalDateTime added,
        String sequence,
        Integer sequenceNumber,
        String lang,
        String zipFile,
        Integer seqid
    ) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.date = date;
        this.added = added;
        this.sequence = sequence;
        this.sequenceNumber = sequenceNumber;
        this.lang = lang;
        this.zipFile = zipFile;
        this.seqid = seqid;
    }

    /**
     * Getter for <code>book.id</code>.
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Getter for <code>book.path</code>.
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * Getter for <code>book.name</code>.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Getter for <code>book.date</code>.
     */
    @Override
    public String getDate() {
        return this.date;
    }

    /**
     * Getter for <code>book.added</code>.
     */
    @Override
    public LocalDateTime getAdded() {
        return this.added;
    }

    /**
     * Getter for <code>book.sequence</code>.
     */
    @Override
    public String getSequence() {
        return this.sequence;
    }

    /**
     * Getter for <code>book.sequence_number</code>.
     */
    @Override
    public Integer getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * Getter for <code>book.lang</code>.
     */
    @Override
    public String getLang() {
        return this.lang;
    }

    /**
     * Getter for <code>book.zip_file</code>.
     */
    @Override
    public String getZipFile() {
        return this.zipFile;
    }

    /**
     * Getter for <code>book.seqid</code>.
     */
    @Override
    public Integer getSeqid() {
        return this.seqid;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Book other = (Book) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.path == null) {
            if (other.path != null)
                return false;
        }
        else if (!this.path.equals(other.path))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.date == null) {
            if (other.date != null)
                return false;
        }
        else if (!this.date.equals(other.date))
            return false;
        if (this.added == null) {
            if (other.added != null)
                return false;
        }
        else if (!this.added.equals(other.added))
            return false;
        if (this.sequence == null) {
            if (other.sequence != null)
                return false;
        }
        else if (!this.sequence.equals(other.sequence))
            return false;
        if (this.sequenceNumber == null) {
            if (other.sequenceNumber != null)
                return false;
        }
        else if (!this.sequenceNumber.equals(other.sequenceNumber))
            return false;
        if (this.lang == null) {
            if (other.lang != null)
                return false;
        }
        else if (!this.lang.equals(other.lang))
            return false;
        if (this.zipFile == null) {
            if (other.zipFile != null)
                return false;
        }
        else if (!this.zipFile.equals(other.zipFile))
            return false;
        if (this.seqid == null) {
            if (other.seqid != null)
                return false;
        }
        else if (!this.seqid.equals(other.seqid))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.path == null) ? 0 : this.path.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.date == null) ? 0 : this.date.hashCode());
        result = prime * result + ((this.added == null) ? 0 : this.added.hashCode());
        result = prime * result + ((this.sequence == null) ? 0 : this.sequence.hashCode());
        result = prime * result + ((this.sequenceNumber == null) ? 0 : this.sequenceNumber.hashCode());
        result = prime * result + ((this.lang == null) ? 0 : this.lang.hashCode());
        result = prime * result + ((this.zipFile == null) ? 0 : this.zipFile.hashCode());
        result = prime * result + ((this.seqid == null) ? 0 : this.seqid.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Book (");

        sb.append(id);
        sb.append(", ").append(path);
        sb.append(", ").append(name);
        sb.append(", ").append(date);
        sb.append(", ").append(added);
        sb.append(", ").append(sequence);
        sb.append(", ").append(sequenceNumber);
        sb.append(", ").append(lang);
        sb.append(", ").append(zipFile);
        sb.append(", ").append(seqid);

        sb.append(")");
        return sb.toString();
    }
}
