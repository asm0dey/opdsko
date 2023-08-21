ALTER TABLE book
    ADD COLUMN seqid INT;

UPDATE book
SET seqid=numbers.seqrow
FROM (SELECT ROW_NUMBER() OVER () seqrow, seqname
      FROM (SELECT DISTINCT(sequence) seqname FROM book WHERE book.sequence IS NOT NULL ORDER BY sequence)) AS numbers
WHERE book.sequence = numbers.seqname;
