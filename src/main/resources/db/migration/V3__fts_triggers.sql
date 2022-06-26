DROP TABLE IF EXISTS books_fts;
CREATE VIRTUAL TABLE books_fts USING fts5
(
    name,
    sequence,
    content=book,
    content_rowid=id,
    tokenize="trigram"
);

DROP TABLE IF EXISTS authors_fts;
CREATE VIRTUAL TABLE authors_fts USING fts5
(
    last_name,
    first_name,
    middle_name,
    nickname,
    content=author,
    content_rowid=id,
    tokenize="trigram"
);

DROP TRIGGER IF EXISTS tbl_bi;
DROP TRIGGER IF EXISTS tbl_bd;
DROP TRIGGER IF EXISTS tbl_bu;
CREATE TRIGGER tbl_bi
    AFTER INSERT
    ON main.book
BEGIN
    INSERT INTO books_fts(rowid, name, sequence) VALUES (new.id, new.name, new.sequence);
END;
CREATE TRIGGER tbl_bd
    AFTER DELETE
    ON main.book
BEGIN
    INSERT INTO books_fts(books_fts, rowid, name, sequence) VALUES ('delete', old.id, old.name, old.sequence);
END;
CREATE TRIGGER tbl_bu
    AFTER UPDATE
    ON main.book
BEGIN
    INSERT INTO books_fts(books_fts, rowid, name, sequence) VALUES ('delete', old.id, old.name, old.sequence);
    INSERT INTO books_fts(rowid, name, sequence) VALUES (new.id, new.name, new.sequence);
END;

DROP TRIGGER IF EXISTS tbl_ai;
DROP TRIGGER IF EXISTS tbl_ad;
DROP TRIGGER IF EXISTS tbl_au;
CREATE TRIGGER tbl_ai
    AFTER INSERT
    ON main.author
BEGIN
    INSERT INTO authors_fts(rowid, nickname, middle_name, last_name, first_name)
    VALUES (new.id, new.nickname, new.middle_name, new.last_name, new.first_name);
END;
CREATE TRIGGER tbl_ad
    AFTER DELETE
    ON main.author
BEGIN
    INSERT INTO authors_fts(authors_fts, rowid, nickname, middle_name, last_name, first_name)
    VALUES ('delete', old.id, old.nickname, old.middle_name, old.last_name, old.first_name);
END;
CREATE TRIGGER tbl_au
    AFTER UPDATE
    ON main.author
BEGIN
    INSERT INTO authors_fts(authors_fts, rowid, nickname, middle_name, last_name, first_name)
    VALUES ('delete', old.id, old.nickname, old.middle_name, old.last_name, old.first_name);
    INSERT INTO authors_fts(rowid, nickname, middle_name, last_name, first_name)
    VALUES (new.id, new.nickname, new.middle_name, new.last_name, new.first_name);
END;
