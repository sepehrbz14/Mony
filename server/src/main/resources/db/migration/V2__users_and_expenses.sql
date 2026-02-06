CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    phone TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

INSERT INTO users (phone, password_hash)
SELECT 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users);

ALTER TABLE expenses
    ADD COLUMN IF NOT EXISTS user_id INTEGER REFERENCES users(id);

UPDATE expenses
SET user_id = (
    SELECT id FROM users ORDER BY id LIMIT 1
)
WHERE user_id IS NULL;

ALTER TABLE expenses
    ALTER COLUMN user_id SET NOT NULL;
