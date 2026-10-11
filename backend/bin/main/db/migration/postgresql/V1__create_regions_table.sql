-- regions table
CREATE TABLE regions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
);
