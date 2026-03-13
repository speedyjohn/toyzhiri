CREATE TABLE notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,

    -- Каналы уведомлений
    push_enabled BOOLEAN NOT NULL DEFAULT true,
    email_enabled BOOLEAN NOT NULL DEFAULT true,
    sms_enabled BOOLEAN NOT NULL DEFAULT false,

    -- Типы событий (общие для клиентов и партнёров)
    booking_updates BOOLEAN NOT NULL DEFAULT true,   -- изменения статуса бронирования
    chat_messages BOOLEAN NOT NULL DEFAULT true,      -- новые сообщения в чате
    promotions BOOLEAN NOT NULL DEFAULT false,        -- акции и спецпредложения

    -- Типы событий только для клиентов
    event_reminders BOOLEAN NOT NULL DEFAULT true,    -- напоминания о предстоящем мероприятии

    -- Типы событий только для партнёров
    new_bookings BOOLEAN NOT NULL DEFAULT true,       -- новые входящие бронирования

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notification_settings_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_settings_user_id ON notification_settings(user_id);