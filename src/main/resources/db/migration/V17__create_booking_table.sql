CREATE TABLE bookings
(
    id               UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id          UUID        NOT NULL,
    service_id       UUID        NOT NULL,
    partner_id       UUID        NOT NULL,
    event_date       DATE        NOT NULL,
    event_time       TIME,
    status           VARCHAR(30) NOT NULL DEFAULT 'PENDING_CONFIRMATION',
    notes            TEXT,
    total_price      DECIMAL(12, 2),
    rejection_reason VARCHAR(500),
    expires_at       TIMESTAMP,
    confirmed_at     TIMESTAMP,
    rejected_at      TIMESTAMP,
    cancelled_at     TIMESTAMP,
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_booking_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_service FOREIGN KEY (service_id)
        REFERENCES services (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_partner FOREIGN KEY (partner_id)
        REFERENCES partners (id) ON DELETE CASCADE,
    CONSTRAINT chk_booking_status CHECK (status IN (
                                                    'PENDING_CONFIRMATION',
                                                    'CONFIRMED',
                                                    'REJECTED',
                                                    'PAID',
                                                    'COMPLETED',
                                                    'CANCELLED',
                                                    'EXPIRED'
        ))
);

CREATE INDEX idx_bookings_user_id ON bookings (user_id);
CREATE INDEX idx_bookings_service_id ON bookings (service_id);
CREATE INDEX idx_bookings_partner_id ON bookings (partner_id);
CREATE INDEX idx_bookings_status ON bookings (status);
CREATE INDEX idx_bookings_event_date ON bookings (event_date);
CREATE INDEX idx_bookings_expires_at ON bookings (expires_at) WHERE status = 'PENDING_CONFIRMATION';