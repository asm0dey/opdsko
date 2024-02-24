--liquibase formatted sql

--changeset asm0dey:5
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS newone ON book USING gin (sequence gin_trgm_ops);
