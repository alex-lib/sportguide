ALTER TABLE app_schema.places
    ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);