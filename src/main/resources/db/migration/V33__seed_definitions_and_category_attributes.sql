-- ATTRIBUTE DEFINITIONS

-- Вместимость (ресторан) — диапазон
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('capacity', 'INTEGER', 'RANGE_CONTAINS',
        '{"min": "capacity_min", "max": "capacity_max"}'::jsonb,
        'Вместимость', 'Сыйымдылық', 'чел',
        '{"min": 1, "max": 10000}'::jsonb);

-- Площадь (ресторан) — одиночное значение >=
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('area_sqm', 'INTEGER', 'SINGLE_GTE',
        '{"value": "area_sqm"}'::jsonb,
        'Площадь', 'Ауданы', 'м²',
        '{"min": 1, "max": 100000}'::jsonb);

-- Сцена (ресторан) — boolean
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('has_stage', 'BOOLEAN', 'BOOLEAN_MATCH',
        '{"value": "has_stage"}'::jsonb,
        'Наличие сцены', 'Сахнаның болуы', NULL, NULL);

-- Парковка (ресторан) — boolean
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('has_parking', 'BOOLEAN', 'BOOLEAN_MATCH',
        '{"value": "has_parking"}'::jsonb,
        'Наличие парковки', 'Тұрақтың болуы', NULL, NULL);

-- Типы кухни (ресторан + кейтеринг) — массив с опциями
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('cuisine_types', 'STRING_ARRAY', 'ARRAY_INTERSECTS',
        '{"value": "cuisine_types"}'::jsonb,
        'Типы кухни', 'Ас үй түрлері', NULL,
        '{"options": ["EUROPEAN", "ASIAN", "KAZAKH", "ITALIAN", "JAPANESE", "CHINESE", "MIXED", "HALAL"]}'::jsonb);

-- Модель авто (транспорт) — точное совпадение строки
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('car_model', 'STRING', 'SINGLE_EQ',
        '{"value": "car_model"}'::jsonb,
        'Модель автомобиля', 'Автокөлік үлгісі', NULL,
        '{"maxLength": 100}'::jsonb);

-- Год выпуска авто (транспорт) — не старше
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('car_year', 'INTEGER', 'SINGLE_GTE',
        '{"value": "car_year"}'::jsonb,
        'Год выпуска', 'Шығарылған жылы', NULL,
        '{"min": 1950, "max": 2030}'::jsonb);

-- Количество мест авто (транспорт) — не меньше
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('car_seats', 'INTEGER', 'SINGLE_GTE',
        '{"value": "car_seats"}'::jsonb,
        'Количество мест', 'Орын саны', 'мест',
        '{"min": 1, "max": 100}'::jsonb);

-- Цвет авто (транспорт) — точное совпадение с опциями
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('car_color', 'STRING', 'SINGLE_EQ',
        '{"value": "car_color"}'::jsonb,
        'Цвет', 'Түсі', NULL,
        '{"options": ["WHITE", "BLACK", "SILVER", "RED", "BLUE", "GRAY", "OTHER"]}'::jsonb);

-- Языки ведения (тамада) — массив с пересечением
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('languages', 'STRING_ARRAY', 'ARRAY_INTERSECTS',
        '{"value": "languages"}'::jsonb,
        'Языки ведения', 'Жүргізу тілдері', NULL,
        '{"options": ["KK", "RU", "EN"]}'::jsonb);

-- Опыт работы в годах (универсальный: тамада, декоратор, фото)
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('experience_years', 'INTEGER', 'SINGLE_GTE',
        '{"value": "experience_years"}'::jsonb,
        'Опыт работы', 'Жұмыс өтілі', 'лет',
        '{"min": 0, "max": 80}'::jsonb);

-- Типы мероприятий (тамада) — массив с пересечением
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('event_types', 'STRING_ARRAY', 'ARRAY_INTERSECTS',
        '{"value": "event_types"}'::jsonb,
        'Типы мероприятий', 'Іс-шара түрлері', NULL,
        '{"options": ["WEDDING", "CORPORATE", "BIRTHDAY", "ANNIVERSARY", "GRADUATION", "CONFERENCE", "OTHER"]}'::jsonb);

-- Стили декора (декораторы) — массив с пересечением
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('decoration_styles', 'STRING_ARRAY', 'ARRAY_INTERSECTS',
        '{"value": "decoration_styles"}'::jsonb,
        'Стили декора', 'Декор стильдері', NULL,
        '{"options": ["CLASSIC", "BOHO", "RUSTIC", "MODERN", "MINIMALIST", "ROMANTIC", "VINTAGE"]}'::jsonb);

-- Типы услуг фото/видео — массив с пересечением
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('service_types', 'STRING_ARRAY', 'ARRAY_INTERSECTS',
        '{"value": "service_types"}'::jsonb,
        'Типы услуг', 'Қызмет түрлері', NULL,
        '{"options": ["PHOTO", "VIDEO", "PHOTO_AND_VIDEO", "DRONE"]}'::jsonb);

-- Длительность съёмки (фото/видео) — не меньше
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('duration_hours', 'INTEGER', 'SINGLE_GTE',
        '{"value": "duration_hours"}'::jsonb,
        'Длительность съёмки', 'Түсіру ұзақтығы', 'часов',
        '{"min": 1, "max": 48}'::jsonb);

-- Минимальное количество персон (кейтеринг) — <=
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('min_persons', 'INTEGER', 'SINGLE_LTE',
        '{"value": "min_persons"}'::jsonb,
        'Минимальный заказ', 'Ең аз тапсырыс', 'чел',
        '{"min": 1, "max": 10000}'::jsonb);

-- Доставка (кейтеринг) — boolean
INSERT INTO attribute_definitions (key, type, match_strategy, storage_keys, label_ru, label_kk, unit, validation_rules)
VALUES ('has_delivery', 'BOOLEAN', 'BOOLEAN_MATCH',
        '{"value": "has_delivery"}'::jsonb,
        'Доставка', 'Жеткізу', NULL, NULL);


-- CATEGORY <-> ATTRIBUTE BINDINGS

-- РЕСТОРАНЫ
INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, true, true, 1
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'restaurants'
  AND ad.key = 'capacity';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 2
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'restaurants'
  AND ad.key = 'area_sqm';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 3
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'restaurants'
  AND ad.key = 'has_stage';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 4
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'restaurants'
  AND ad.key = 'has_parking';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 5
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'restaurants'
  AND ad.key = 'cuisine_types';

-- ТРАНСПОРТ
INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, true, true, 1
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'transport'
  AND ad.key = 'car_model';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, true, true, 2
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'transport'
  AND ad.key = 'car_seats';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 3
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'transport'
  AND ad.key = 'car_year';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 4
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'transport'
  AND ad.key = 'car_color';

-- ТАМАДЫ, ВЕДУЩИЕ, МУЗЫКАНТЫ, ДИДЖЕИ
INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, true, true, 1
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'hosts-musicians'
  AND ad.key = 'languages';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 2
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'hosts-musicians'
  AND ad.key = 'experience_years';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 3
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'hosts-musicians'
  AND ad.key = 'event_types';

-- ДЕКОРАТОРЫ И ФЛОРИСТЫ
INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 1
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'decorators'
  AND ad.key = 'decoration_styles';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 2
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'decorators'
  AND ad.key = 'experience_years';

-- ФОТО/ВИДЕО
INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, true, true, 1
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'photo-video'
  AND ad.key = 'service_types';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 2
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'photo-video'
  AND ad.key = 'duration_hours';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 3
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'photo-video'
  AND ad.key = 'experience_years';

-- КЕЙТЕРИНГ
INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, true, true, 1
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'catering'
  AND ad.key = 'min_persons';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 2
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'catering'
  AND ad.key = 'cuisine_types';

INSERT INTO category_attributes (category_id, attribute_id, is_required, is_filterable, sort_order)
SELECT sc.id, ad.id, false, true, 3
FROM service_categories sc,
     attribute_definitions ad
WHERE sc.slug = 'catering'
  AND ad.key = 'has_delivery';