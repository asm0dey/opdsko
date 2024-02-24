--liquibase formatted sql

--changeset asm0dey:4
ALTER TABLE author DROP COLUMN IF EXISTS full_name;

ALTER TABLE author
    ADD COLUMN full_name text GENERATED ALWAYS AS ( TRIM((
        ((CASE WHEN last_name IS NOT NULL THEN (last_name || ' ') ELSE '' END ||
          CASE WHEN first_name IS NOT NULL THEN (first_name || ' ') ELSE '' END) ||
         CASE WHEN middle_name IS NOT NULL THEN (middle_name || ' ') ELSE '' END) || CASE
                                                                                         WHEN nickname IS NULL
                                                                                             THEN ''
                                                                                         ELSE (
                                                                                             (CASE
                                                                                                  WHEN (
                                                                                                      last_name IS NULL AND
                                                                                                      middle_name IS NULL AND
                                                                                                      first_name IS NULL)
                                                                                                      THEN ''
                                                                                                  ELSE '(' END ||
                                                                                              nickname) ||
                                                                                             CASE
                                                                                                 WHEN (
                                                                                                     last_name IS NULL AND
                                                                                                     middle_name IS NULL AND
                                                                                                     first_name IS NULL)
                                                                                                     THEN ''
                                                                                                 ELSE ')' END) END)) ) STORED;
