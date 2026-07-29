DROP TABLE perfume_occasion;
DROP TABLE occasion_tag;

CREATE TABLE perfume_occasion_score (
    id BIGSERIAL PRIMARY KEY,
    perfume_id BIGINT NOT NULL REFERENCES perfume (id) ON DELETE CASCADE,
    occasion VARCHAR(20) NOT NULL,
    score INTEGER NOT NULL,
    CONSTRAINT uq_perfume_occasion UNIQUE (perfume_id, occasion)
);

CREATE INDEX idx_perfume_occasion_perfume ON perfume_occasion_score (perfume_id);

ALTER SEQUENCE perfume_occasion_score_id_seq INCREMENT BY 50;
