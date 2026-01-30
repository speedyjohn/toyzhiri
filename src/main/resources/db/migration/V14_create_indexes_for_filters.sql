-- V14__create_indexes_for_filters.sql
-- Создание индексов для оптимизации работы фильтров

-- Индекс для фильтрации по цене
CREATE INDEX IF NOT EXISTS idx_services_price_from ON services(price_from);
CREATE INDEX IF NOT EXISTS idx_services_price_to ON services(price_to);

-- Индекс для фильтрации по рейтингу
CREATE INDEX IF NOT EXISTS idx_services_rating ON services(rating);

-- Индекс для фильтрации по городу
CREATE INDEX IF NOT EXISTS idx_services_city ON services(city);

-- Составной индекс для часто используемых комбинаций
-- Активные и одобренные услуги (базовое условие всех запросов)
CREATE INDEX IF NOT EXISTS idx_services_active_approved
    ON services(is_active, is_approved)
    WHERE is_active = true AND is_approved = true;

-- Составной индекс для фильтрации по категории и статусу
CREATE INDEX IF NOT EXISTS idx_services_category_active
    ON services(category_id, is_active, is_approved);

-- Индекс для сортировки по популярности
CREATE INDEX IF NOT EXISTS idx_services_bookings_count ON services(bookings_count DESC);
CREATE INDEX IF NOT EXISTS idx_services_views_count ON services(views_count DESC);
CREATE INDEX IF NOT EXISTS idx_services_reviews_count ON services(reviews_count DESC);

-- Индекс для фильтрации по количеству отзывов
CREATE INDEX IF NOT EXISTS idx_services_reviews_rating
    ON services(reviews_count, rating)
    WHERE reviews_count > 0;

-- Полнотекстовый индекс для поиска (PostgreSQL)
-- Для полей name, short_description, full_description
CREATE INDEX IF NOT EXISTS idx_services_search
    ON services USING gin(
    to_tsvector('russian',
    coalesce(name, '') || ' ' ||
    coalesce(short_description, '') || ' ' ||
    coalesce(full_description, '')
    )
    );

-- Комментарии для документирования
COMMENT ON INDEX idx_services_price_from IS 'Индекс для фильтрации по минимальной цене';
COMMENT ON INDEX idx_services_rating IS 'Индекс для фильтрации и сортировки по рейтингу';
COMMENT ON INDEX idx_services_city IS 'Индекс для фильтрации по городу';
COMMENT ON INDEX idx_services_active_approved IS 'Частичный индекс для активных и одобренных услуг';
COMMENT ON INDEX idx_services_search IS 'Полнотекстовый индекс для поиска по названию и описаниям';