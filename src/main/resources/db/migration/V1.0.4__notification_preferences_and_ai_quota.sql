ALTER TABLE app_schema.subscribers
    ADD COLUMN get_weather BOOLEAN,
    ADD COLUMN get_places BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN get_coaches BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN get_joint_trainings BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE app_schema.subscribers SET get_weather = COALESCE(get_events, FALSE);
ALTER TABLE app_schema.subscribers ALTER COLUMN get_weather SET NOT NULL;
ALTER TABLE app_schema.subscribers ALTER COLUMN get_weather SET DEFAULT TRUE;

CREATE TABLE app_schema.ai_training_quota (
    subscriber_id BIGINT PRIMARY KEY REFERENCES app_schema.subscribers(id) ON DELETE CASCADE,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL
);
