CREATE TABLE brand (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    country VARCHAR(100),
    description TEXT
);

CREATE TABLE note (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    image_url VARCHAR(500)
);

CREATE TABLE accord (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    color_hex VARCHAR(7)
);

CREATE TABLE occasion_tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE perfume (
    id BIGSERIAL PRIMARY KEY,
    brand_id BIGINT NOT NULL REFERENCES brand (id),
    name VARCHAR(255) NOT NULL,
    release_year INTEGER,
    concentration VARCHAR(20),
    price NUMERIC(10, 2),
    description TEXT,
    image_url VARCHAR(500),
    gender VARCHAR(10),
    rating_value DOUBLE PRECISION,
    rating_count INTEGER,
    CONSTRAINT uq_perfume_brand_name UNIQUE (brand_id, name)
);

CREATE INDEX idx_perfume_brand ON perfume (brand_id);

CREATE TABLE perfume_note (
    id BIGSERIAL PRIMARY KEY,
    perfume_id BIGINT NOT NULL REFERENCES perfume (id) ON DELETE CASCADE,
    note_id BIGINT NOT NULL REFERENCES note (id),
    pyramid_position VARCHAR(10) NOT NULL,
    CONSTRAINT uq_perfume_note_position UNIQUE (perfume_id, note_id, pyramid_position)
);

CREATE INDEX idx_perfume_note_perfume ON perfume_note (perfume_id);
CREATE INDEX idx_perfume_note_note ON perfume_note (note_id);

CREATE TABLE perfume_accord (
    id BIGSERIAL PRIMARY KEY,
    perfume_id BIGINT NOT NULL REFERENCES perfume (id) ON DELETE CASCADE,
    accord_id BIGINT NOT NULL REFERENCES accord (id),
    strength INTEGER NOT NULL,
    CONSTRAINT uq_perfume_accord UNIQUE (perfume_id, accord_id)
);

CREATE INDEX idx_perfume_accord_perfume ON perfume_accord (perfume_id);
CREATE INDEX idx_perfume_accord_accord ON perfume_accord (accord_id);

CREATE TABLE perfume_occasion (
    perfume_id BIGINT NOT NULL REFERENCES perfume (id) ON DELETE CASCADE,
    occasion_tag_id BIGINT NOT NULL REFERENCES occasion_tag (id) ON DELETE CASCADE,
    PRIMARY KEY (perfume_id, occasion_tag_id)
);

CREATE TABLE perfume_season_score (
    id BIGSERIAL PRIMARY KEY,
    perfume_id BIGINT NOT NULL REFERENCES perfume (id) ON DELETE CASCADE,
    season VARCHAR(10) NOT NULL,
    score INTEGER NOT NULL,
    CONSTRAINT uq_perfume_season UNIQUE (perfume_id, season)
);

CREATE INDEX idx_perfume_season_perfume ON perfume_season_score (perfume_id);
