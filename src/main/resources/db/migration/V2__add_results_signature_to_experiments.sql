ALTER TABLE experiments
    ADD COLUMN results_hash VARCHAR(64),
    ADD COLUMN results_signed_at TIMESTAMP;
