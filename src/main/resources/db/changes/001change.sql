--liquibase formatted sql

--changeset asm0dey:1

CREATE TABLE IF NOT EXISTS author
(
    id          bigserial PRIMARY KEY                              NOT NULL,
    fb2id       integer,
    first_name  text,
    middle_name text,
    last_name   text,
    nickname    text,
    added       timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS author_added_idx
    ON author (added);

CREATE INDEX IF NOT EXISTS author_names_idx
    ON author (middle_name, last_name, first_name, nickname);

CREATE TABLE IF NOT EXISTS genre
(
    id   bigserial PRIMARY KEY NOT NULL,
    name text UNIQUE           NOT NULL
);

CREATE TABLE IF NOT EXISTS book
(
    id              bigserial PRIMARY KEY                              NOT NULL,
    path            text                                               NOT NULL,
    name            text                                               NOT NULL,
    date            text,
    added           timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    sequence        text,
    sequence_number bigint,
    lang            text,
    zip_file        text,
    seqid           integer
);

CREATE INDEX IF NOT EXISTS book_sequence_idx
    ON book (sequence);

CREATE INDEX IF NOT EXISTS book_added_idx
    ON book (added);

CREATE TABLE IF NOT EXISTS book_author
(
    book_id   bigint NOT NULL REFERENCES book,
    author_id bigint NOT NULL REFERENCES author
);

CREATE UNIQUE INDEX IF NOT EXISTS book_author_book_author_idx
    ON book_author (book_id, author_id);

CREATE INDEX IF NOT EXISTS book_author_book_idx
    ON book_author (book_id);

CREATE INDEX IF NOT EXISTS book_author_author_idx
    ON book_author (author_id);

CREATE TABLE IF NOT EXISTS book_genre
(
    book_id  bigint NOT NULL REFERENCES book,
    genre_id bigint NOT NULL REFERENCES genre
);


CREATE UNIQUE INDEX IF NOT EXISTS book_genre_book_genre_idx
    ON book_genre (book_id, genre_id);

CREATE INDEX IF NOT EXISTS book_genre_book_book_idx
    ON book_genre (book_id);

CREATE INDEX IF NOT EXISTS book_genre_genre_idx
    ON book_genre (genre_id);

CALL paradedb.create_bm25(
        index_name => 'book_ngr_idx',
        table_name => 'book',
        key_field => 'id',
        text_fields => '{name: {tokenizer: {type: "ngram", min_gram: 2, max_gram: 4, prefix_only: false}}, sequence: {tokenizer: {type: "ngram", min_gram: 2, max_gram: 4, prefix_only: false}}}'
     );
