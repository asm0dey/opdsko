drop index book_added;

create index book_added
    on book (added desc);
