-- Пароль у OAuth-пользователей отсутствует
ALTER TABLE users
    ALTER COLUMN password DROP NOT NULL;

-- Телефон Google не возвращает, заполнит пользователь позже
ALTER TABLE users
    ALTER COLUMN phone DROP NOT NULL;

-- Город Google не возвращает, заполнит пользователь позже
ALTER TABLE users
    ALTER COLUMN city DROP NOT NULL;

-- Провайдер авторизации (LOCAL / GOOGLE)
ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

-- Флаг завершённости профиля.
-- Существующие пользователи регистрировались через обычную форму, у них профиль полный.
ALTER TABLE users
    ADD COLUMN profile_completed BOOLEAN NOT NULL DEFAULT true;

CREATE INDEX idx_users_auth_provider ON users (auth_provider);