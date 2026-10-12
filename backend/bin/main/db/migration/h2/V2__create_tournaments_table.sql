-- tournaments table
CREATE TABLE tournaments (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT,
    start_date DATE,
    venue TEXT,
    address TEXT,
    region_id BIGINT,
    entry_deadline DATE,
    summary TEXT,
    outline_pdf_url TEXT,
    entry_pdf_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournaments_region FOREIGN KEY (region_id) REFERENCES regions(id)
);
