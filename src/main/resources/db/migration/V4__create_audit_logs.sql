CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY,
    action          VARCHAR(100) NOT NULL,
    result          VARCHAR(20) NOT NULL,
    user_id         UUID,
    user_email      VARCHAR(255),
    ip_address      VARCHAR(45),
    http_method     VARCHAR(10),
    endpoint        VARCHAR(500),
    status_code     INTEGER,
    details         TEXT,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_ip_address ON audit_logs (ip_address);
CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
