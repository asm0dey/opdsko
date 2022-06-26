CREATE INDEX IF NOT EXISTS book_seq ON book (sequence) WHERE sequence IS NOT NULL AND trim(sequence) <> '';
