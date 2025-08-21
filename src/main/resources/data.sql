INSERT INTO app_schema.subscribers (id, username, first_name, last_name, get_events, role)
VALUES (455247964, 'exceedspace', 'alex', 'sigov', TRUE, 'ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO app_schema.subscribers (id, username, first_name, last_name, get_events, role)
VALUES (1303265124, 'dmtkrv', 'dmt', 'krv', TRUE, 'ADMIN')
ON CONFLICT DO NOTHING;