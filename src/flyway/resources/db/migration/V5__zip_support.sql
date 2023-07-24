PRAGMA foreign_keys= OFF;
ALTER TABLE book
    RENAME TO old_book;
CREATE TABLE book
(
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    path            TEXT                           NOT NULL,
    name            TEXT                           NOT NULL,
    date            TEXT,
    added           TEXT DEFAULT CURRENT_TIMESTAMP NOT NULL,
    sequence        TEXT,
    sequence_number INTEGER,
    lang            TEXT,
    zip_file        TEXT
);
DROP INDEX book_added;
CREATE INDEX book_added ON book (added);
DROP INDEX book_path_name;
CREATE UNIQUE INDEX book_path_name ON book (path, COALESCE(zip_file, ''), name);
DROP INDEX book_seq;
CREATE INDEX book_seq ON book (sequence) WHERE sequence IS NOT NULL AND TRIM(sequence) <> '';
CREATE UNIQUE INDEX fully_unique_path ON book (path, COALESCE(zip_file, ''));
CREATE TRIGGER tbl_bd_2
    AFTER DELETE
    ON book
BEGIN
    INSERT INTO books_fts(books_fts, rowid, name, sequence) VALUES ('delete', old.id, old.name, old.sequence);
END;
CREATE TRIGGER tbl_bi_2
    AFTER INSERT
    ON book
BEGIN
    INSERT INTO books_fts(rowid, name, sequence) VALUES (new.id, new.name, new.sequence);
END;
CREATE TRIGGER tbl_bu_2
    AFTER UPDATE
    ON book
BEGIN
    INSERT INTO books_fts(books_fts, rowid, name, sequence) VALUES ('delete', old.id, old.name, old.sequence);
    INSERT INTO books_fts(rowid, name, sequence) VALUES (new.id, new.name, new.sequence);
END;
INSERT INTO book (id, path, name, date, added, sequence, sequence_number, lang)
SELECT id
     , path
     , name
     , date
     , added
     , sequence
     , sequence_number
     , lang
FROM old_book;
DELETE
FROM old_book;
DROP TABLE old_book;
CREATE TABLE book_author_dg_tmp
(
    book_id   INTEGER NOT NULL REFERENCES book ON UPDATE RESTRICT ON DELETE CASCADE,
    author_id INTEGER NOT NULL REFERENCES author ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT pk_book_author PRIMARY KEY (book_id, author_id)
);
INSERT INTO book_author_dg_tmp(book_id, author_id)
SELECT book_id, author_id
FROM book_author;
DROP TABLE book_author;
ALTER TABLE book_author_dg_tmp
    RENAME TO book_author;
CREATE INDEX book_author_author_id ON book_author (author_id);
CREATE INDEX book_author_book_id ON book_author (book_id);
CREATE TABLE book_genre_dg_tmp
(
    book_id  INTEGER NOT NULL REFERENCES book ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genre ON DELETE CASCADE,
    CONSTRAINT pk_book_genre PRIMARY KEY (book_id, genre_id)
);
INSERT INTO book_genre_dg_tmp(book_id, genre_id)
SELECT book_id, genre_id
FROM book_genre;
DROP TABLE book_genre;
ALTER TABLE book_genre_dg_tmp
    RENAME TO book_genre;
CREATE INDEX book_genre_book_id ON book_genre (book_id);
CREATE INDEX book_genre_genre_id ON book_genre (genre_id);
PRAGMA foreign_keys= ON;
INSERT INTO books_fts(books_fts)
VALUES ('rebuild');