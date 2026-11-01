CREATE TABLE app_schema.event_import_records (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(255) NOT NULL,
    external_key VARCHAR(1000) NOT NULL,
    source_url TEXT NOT NULL,
    event_id BIGINT,
    manual_review BOOLEAN NOT NULL DEFAULT FALSE,
    last_checked DATE,
    source_date DATE,
    source_time TIME,
    pending_report TEXT,
    delivered_report_hash VARCHAR(64),
    CONSTRAINT uq_event_import_source_key UNIQUE (source, external_key)
);
CREATE INDEX idx_event_import_due ON app_schema.event_import_records(last_checked) WHERE event_id IS NOT NULL;
ALTER TABLE app_schema.events ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE app_schema.events ALTER COLUMN description TYPE TEXT;
ALTER TABLE app_schema.events ALTER COLUMN link TYPE TEXT;

ALTER TABLE app_schema.event_import_records ADD COLUMN announcement_queued BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE app_schema.event_import_calendars (
    competition INTEGER PRIMARY KEY,
    last_attempt DATE NOT NULL
);

CREATE TABLE app_schema.event_import_messages (
    id UUID PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    queued_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_event_import_messages_pending ON app_schema.event_import_messages(created_at)
    WHERE queued_at IS NULL;

CREATE TABLE app_schema.event_import_source_checks (
    source VARCHAR(255) PRIMARY KEY,
    last_attempt DATE NOT NULL
);
