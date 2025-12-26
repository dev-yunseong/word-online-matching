CREATE TABLE decorations (
    id BIGSERIAL PRIMARY KEY,
    deco_type VARCHAR(10) NOT NULL , -- Hat, Cape
    name VARCHAR(20) NOT NULL
);

CREATE TABLE user_decorations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    decoration_id BIGINT REFERENCES decorations(id)
);

ALTER TABLE user_decorations ADD COLUMN is_equipped BOOLEAN DEFAULT FALSE;

ALTER TABLE cards
    ADD COLUMN IF NOT EXISTS unlock_condition_type VARCHAR(31),
    ADD COLUMN IF NOT EXISTS unlock_required_value INT;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS total_wins INT NOT NULL DEFAULT 0;

UPDATE cards
SET unlock_condition_type = 'WIN_COUNT',
    unlock_required_value = 5
WHERE name = 'Wind';

UPDATE cards
SET unlock_condition_type = 'WIN_COUNT',
    unlock_required_value = 10
WHERE name = 'Drop';