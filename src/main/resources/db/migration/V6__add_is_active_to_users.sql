ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT true NOT NULL;

UPDATE users SET is_active = true WHERE is_active IS NULL;