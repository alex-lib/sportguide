CREATE TABLE IF NOT EXISTS app_schema.tours (
    id BIGSERIAL PRIMARY KEY,
    route VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS app_schema.tooltips (
    id BIGSERIAL PRIMARY KEY,
    tour_id BIGINT NOT NULL REFERENCES app_schema.tours(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    target VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    placement VARCHAR(20) NOT NULL DEFAULT 'bottom',
    is_primary BOOLEAN DEFAULT FALSE,
    UNIQUE (tour_id, position)
);

CREATE TABLE IF NOT EXISTS app_schema.tour_records (
    id BIGSERIAL PRIMARY KEY,
    subscriber_id BIGINT NOT NULL,
    route VARCHAR(255) NOT NULL,
    showed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    UNIQUE (subscriber_id, route)
);

CREATE INDEX idx_tour_records_subscriber ON app_schema.tour_records(subscriber_id);