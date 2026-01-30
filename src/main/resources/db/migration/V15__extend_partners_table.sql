ALTER TABLE partners ADD COLUMN company_name VARCHAR(255);
ALTER TABLE partners ADD COLUMN description TEXT;
ALTER TABLE partners ADD COLUMN address TEXT;
ALTER TABLE partners ADD COLUMN city VARCHAR(100);
ALTER TABLE partners ADD COLUMN region VARCHAR(100);
ALTER TABLE partners ADD COLUMN phone VARCHAR(20);
ALTER TABLE partners ADD COLUMN email VARCHAR(100);
ALTER TABLE partners ADD COLUMN whatsapp VARCHAR(20);
ALTER TABLE partners ADD COLUMN telegram VARCHAR(100);
ALTER TABLE partners ADD COLUMN instagram VARCHAR(100);
ALTER TABLE partners ADD COLUMN website VARCHAR(255);
ALTER TABLE partners ADD COLUMN logo_url VARCHAR(500);

-- Для существующих партнеров заполняем базовые данные из таблицы users
UPDATE partners p
SET
    phone = u.phone,
    email = u.email,
    city = u.city
    FROM users u
WHERE p.user_id = u.id AND p.phone IS NULL;

-- Индексы для улучшения производительности поиска
CREATE INDEX idx_partners_city ON partners(city);
CREATE INDEX idx_partners_status ON partners(status);
CREATE INDEX idx_partners_company_name ON partners(company_name);