CREATE TABLE stories
(
    id             UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    service_id     UUID           NOT NULL,
    partner_id     UUID           NOT NULL,
    category_id    UUID           NOT NULL,
    media_url      VARCHAR(500)   NOT NULL,
    media_type     VARCHAR(20)    NOT NULL,
    caption        VARCHAR(200),
    status         VARCHAR(30)    NOT NULL DEFAULT 'ACTIVE',
    paid_amount    NUMERIC(12, 2) NOT NULL,
    payment_method VARCHAR(20),
    payment_id     VARCHAR(255),
    views_count    INTEGER        NOT NULL DEFAULT 0,
    expires_at     TIMESTAMP      NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_stories_service FOREIGN KEY (service_id)
        REFERENCES services (id) ON DELETE CASCADE,
    CONSTRAINT fk_stories_partner FOREIGN KEY (partner_id)
        REFERENCES partners (id) ON DELETE CASCADE,
    CONSTRAINT fk_stories_category FOREIGN KEY (category_id)
        REFERENCES service_categories (id) ON DELETE RESTRICT
);

CREATE INDEX idx_stories_status_expires_at ON stories (status, expires_at);
CREATE INDEX idx_stories_category_status ON stories (category_id, status);
CREATE INDEX idx_stories_partner_id ON stories (partner_id);
CREATE INDEX idx_stories_service_id ON stories (service_id);