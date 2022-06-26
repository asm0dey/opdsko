create table author
(
    id          INTEGER primary key autoincrement,
    fb2id       TEXT,
    first_name  TEXT,
    middle_name TEXT,
    last_name   TEXT,
    nickname    TEXT,
    added       TEXT not null
);

create index author_added on author (added);

create index author_names on author (middle_name, last_name, first_name, nickname);

create table book
(
    id              INTEGER primary key autoincrement,
    path            TEXT not null unique,
    name            TEXT not null,
    date            TEXT,
    added           TEXT default CURRENT_TIMESTAMP not null,
    sequence        TEXT,
    sequence_number INTEGER,
    lang            TEXT
);

create index book_added on book (added);

create unique index book_path on book (path);

create unique index book_path_name on book (path, name);

create table book_author
(
    book_id   INTEGER not null references book on update restrict on delete cascade,
    author_id INTEGER not null references author on update restrict on delete cascade,
    constraint pk_book_author primary key (book_id, author_id)
);

create index book_author_author_id on book_author (author_id);

create index book_author_book_id on book_author (book_id);

create table genre
(
    id   INTEGER primary key autoincrement,
    name TEXT not null unique
);

create table book_genre
(
    book_id  INTEGER not null references book on delete cascade,
    genre_id INTEGER not null references genre on delete cascade,
    constraint pk_book_genre primary key (book_id, genre_id)
);

create index book_genre_book_id on book_genre (book_id);

create index book_genre_genre_id on book_genre (genre_id);

create unique index genre_name on genre (name);
