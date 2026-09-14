CREATE TABLE experiment_signatures (
    id              UUID PRIMARY KEY,
    experiment_id   UUID NOT NULL REFERENCES experiments (id),
    results_hash    VARCHAR(64) NOT NULL,
    signed_at       TIMESTAMP NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_experiment_signatures_experiment_id ON experiment_signatures (experiment_id);
