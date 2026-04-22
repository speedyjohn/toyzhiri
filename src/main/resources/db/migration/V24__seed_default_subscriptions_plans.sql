-- Начальный бесплатный тариф (публикация объявлений без оплаты)
INSERT INTO subscription_plans (id, name, slug, description, price, duration_days, is_free, status, display_order)
VALUES (gen_random_uuid(),
        'Бесплатный',
        'free',
        'Базовый тариф — бесплатная публикация объявления на 30 дней',
        0.00,
        30,
        true,
        'ACTIVE',
        0);

-- Тариф PRO (платный, пока ничего дополнительного не даёт, но его нужно оплатить)
INSERT INTO subscription_plans (id, name, slug, description, price, duration_days, is_free, status, display_order)
VALUES (gen_random_uuid(),
        'PRO',
        'pro',
        'Расширенный тариф — продвижение услуг, приоритет в поиске, выделение карточки',
        9990.00,
        30,
        false,
        'ACTIVE',
        1);