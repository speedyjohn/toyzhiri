CREATE TABLE service_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name_ru VARCHAR(100) NOT NULL,
    name_kz VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(255),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Категории из ТЗ
INSERT INTO service_categories (name_ru, name_kz, slug, display_order) VALUES
    ('Рестораны и банкетные залы', 'Мейрамханалар және банкет залдары', 'restaurants', 1),
    ('Кортежи и транспорт', 'Көліктер және транспорт', 'transport', 2),
    ('Тамады, ведущие, музыканты, диджеи', 'Тамадалар, жүргізушілер, музыканттар, диджейлер', 'hosts-musicians', 3),
    ('Декораторы и флористы', 'Декораторлар және гүлшілер', 'decorators', 4),
    ('Фото- и видеосъёмка', 'Фото және бейне түсіру', 'photo-video', 5),
    ('Кейтеринг и доставка еды', 'Кейтеринг және тамақ жеткізу', 'catering', 6);