CREATE INDEX author_name_1 ON author (substr(TRIM((
    ((CASE WHEN last_name IS NOT NULL THEN (last_name || ' ') ELSE '' END ||
      CASE WHEN first_name IS NOT NULL THEN (first_name || ' ') ELSE '' END) ||
     CASE WHEN middle_name IS NOT NULL THEN (middle_name || ' ') ELSE '' END) ||
    CASE
        WHEN nickname IS NULL THEN ''
        ELSE
            ((CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE '(' END ||
              nickname) ||
             CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE ')' END) END)),
                                             1, 1));

CREATE INDEX author_name_2 ON author (substr(TRIM((
    ((CASE WHEN last_name IS NOT NULL THEN (last_name || ' ') ELSE '' END ||
      CASE WHEN first_name IS NOT NULL THEN (first_name || ' ') ELSE '' END) ||
     CASE WHEN middle_name IS NOT NULL THEN (middle_name || ' ') ELSE '' END) ||
    CASE
        WHEN nickname IS NULL THEN ''
        ELSE
            ((CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE '(' END ||
              nickname) ||
             CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE ')' END) END)),
                                             1, 2));
CREATE INDEX author_name_3 ON author (substr(TRIM((
    ((CASE WHEN last_name IS NOT NULL THEN (last_name || ' ') ELSE '' END ||
      CASE WHEN first_name IS NOT NULL THEN (first_name || ' ') ELSE '' END) ||
     CASE WHEN middle_name IS NOT NULL THEN (middle_name || ' ') ELSE '' END) ||
    CASE
        WHEN nickname IS NULL THEN ''
        ELSE
            ((CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE '(' END ||
              nickname) ||
             CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE ')' END) END)),
                                             1, 3));
CREATE INDEX author_name_4 ON author (substr(TRIM((
    ((CASE WHEN last_name IS NOT NULL THEN (last_name || ' ') ELSE '' END ||
      CASE WHEN first_name IS NOT NULL THEN (first_name || ' ') ELSE '' END) ||
     CASE WHEN middle_name IS NOT NULL THEN (middle_name || ' ') ELSE '' END) ||
    CASE
        WHEN nickname IS NULL THEN ''
        ELSE
            ((CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE '(' END ||
              nickname) ||
             CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE ')' END) END)),
                                             1, 4));
CREATE INDEX author_name_5 ON author (substr(TRIM((
    ((CASE WHEN last_name IS NOT NULL THEN (last_name || ' ') ELSE '' END ||
      CASE WHEN first_name IS NOT NULL THEN (first_name || ' ') ELSE '' END) ||
     CASE WHEN middle_name IS NOT NULL THEN (middle_name || ' ') ELSE '' END) ||
    CASE
        WHEN nickname IS NULL THEN ''
        ELSE
            ((CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE '(' END ||
              nickname) ||
             CASE WHEN (last_name IS NULL AND middle_name IS NULL AND first_name IS NULL) THEN '' ELSE ')' END) END)),
                                             1, 5));

CREATE INDEX sequence_1 ON book(substr(sequence, 1, 1));
CREATE INDEX sequence_2 ON book(substr(sequence, 1, 2));
CREATE INDEX sequence_3 ON book(substr(sequence, 1, 3));
CREATE INDEX sequence_4 ON book(substr(sequence, 1, 4));
CREATE INDEX sequence_5 ON book(substr(sequence, 1, 5));