CREATE VIRTUAL TABLE authors_fts USING fts5
(
    last_name,
    first_name,
    middle_name,
    nickname,
    id unindexed,
    tokenize="trigram"
);

insert into authors_fts
select last_name, first_name, middle_name, nickname, id
from author;

create virtual table books_fts USING fts5
(
    name,
    sequence,
    id unindexed,
    tokenize="trigram"
);


insert into books_fts
select name, sequence, id
from book;
