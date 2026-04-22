CREATE TABLE reviews
(
    id         UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    booking_id UUID      NOT NULL UNIQUE,
    user_id    UUID      NOT NULL,
    service_id UUID      NOT NULL,
    partner_id UUID      NOT NULL,
    rating     SMALLINT  NOT NULL,
    comment    TEXT      NOT NULL,
    image_urls TEXT[],
    is_visible BOOLEAN   NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_service FOREIGN KEY (service_id)
        REFERENCES services (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_partner FOREIGN KEY (partner_id)
        REFERENCES partners (id) ON DELETE CASCADE,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_reviews_service_id ON reviews (service_id);
CREATE INDEX idx_reviews_partner_id ON reviews (partner_id);
CREATE INDEX idx_reviews_user_id ON reviews (user_id);
CREATE INDEX idx_reviews_is_visible ON reviews (is_visible) WHERE is_visible = true;