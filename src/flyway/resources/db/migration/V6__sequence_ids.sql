ALTER TABLE book
    ADD COLUMN seqid INT;

CREATE INDEX book_sequence_index
    ON book (sequence);

UPDATE book
SET seqid=numbers.seqrow
FROM (SELECT ROW_NUMBER() OVER () seqrow, seqname
      FROM (SELECT DISTINCT(sequence) seqname FROM book WHERE book.sequence IS NOT NULL ORDER BY sequence)) AS numbers
WHERE book.sequence = numbers.seqname;
