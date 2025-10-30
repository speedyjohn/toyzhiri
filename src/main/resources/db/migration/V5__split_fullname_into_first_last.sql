ALTER TABLE users ADD COLUMN first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN last_name VARCHAR(100);

-- Разделяем fullName по первому пробелу
UPDATE users
SET
    first_name = SPLIT_PART(fullname, ' ', 1),
    last_name = CASE
                    WHEN POSITION(' ' IN fullname) > 0
                        THEN SUBSTRING(fullname FROM POSITION(' ' IN fullname) + 1)
                    ELSE ''
        END
WHERE fullname IS NOT NULL;

-- 3. Делаем поля обязательными
ALTER TABLE users ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE users ALTER COLUMN last_name SET NOT NULL;

-- 4. Удаляем старую колонку fullName
ALTER TABLE users DROP COLUMN fullname;