-- Добавляем флаги двойного подтверждения завершения сделки.
-- Обе стороны (клиент и партнёр) должны подтвердить, что услуга оказана.
-- Когда оба флага true — статус автоматически переходит в COMPLETED.

ALTER TABLE bookings
    ADD COLUMN client_confirmed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE bookings
    ADD COLUMN partner_confirmed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE bookings
    ADD COLUMN client_confirmed_at TIMESTAMP;
ALTER TABLE bookings
    ADD COLUMN partner_confirmed_at TIMESTAMP;
ALTER TABLE bookings
    ADD COLUMN completed_at TIMESTAMP;

-- Убираем PAID из допустимых статусов (оплата теперь вне системы)
ALTER TABLE bookings DROP CONSTRAINT chk_booking_status;
ALTER TABLE bookings
    ADD CONSTRAINT chk_booking_status CHECK (status IN (
                                                        'PENDING_CONFIRMATION',
                                                        'CONFIRMED',
                                                        'REJECTED',
                                                        'COMPLETED',
                                                        'CANCELLED',
                                                        'EXPIRED'
        ));