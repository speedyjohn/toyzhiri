ALTER TABLE users ADD COLUMN role_temp VARCHAR(50);

UPDATE users SET role_temp = role::text;

ALTER TABLE users DROP COLUMN role;

ALTER TABLE users RENAME COLUMN role_temp TO role;

ALTER TABLE users ALTER COLUMN role SET NOT NULL;

DROP TYPE IF EXISTS user_role;