--liquibase formatted sql

--changeset asm0dey:3

CALL paradedb.drop_bm25('book_ngr_idx');

CALL paradedb.create_bm25(
        index_name => 'book_ngr_idx',
        TABLE_NAME => 'book',
        key_field => 'id',
        text_fields => '{name: {}, sequence: {}}'
     );
