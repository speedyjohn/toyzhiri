CREATE TABLE service_variants
(
    id          UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    service_id  UUID           NOT NULL,
    name        VARCHAR(200)   NOT NULL,
    description TEXT,
    price       NUMERIC(12, 2) NOT NULL,
    attributes  JSONB          NOT NULL DEFAULT '{}'::jsonb,
    is_active   BOOLEAN        NOT NULL DEFAULT true,
    sort_order  INTEGER        NOT NULL DEFAULT 0,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_service_variants_service FOREIGN KEY (service_id)
        REFERENCES services (id) ON DELETE CASCADE,
    CONSTRAINT chk_variant_price_positive CHECK (price >= 0)
);

CREATE INDEX idx_service_variants_service_id ON service_variants (service_id);
CREATE INDEX idx_service_variants_is_active ON service_variants (is_active);
CREATE INDEX idx_service_variants_attributes ON service_variants USING GIN (attributes);

CREATE TABLE service_variant_images
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    variant_id    UUID         NOT NULL,
    image_url     VARCHAR(500) NOT NULL,
    display_order INT                   DEFAULT 0,
    is_primary    BOOLEAN               DEFAULT false,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_variant_image_variant FOREIGN KEY (variant_id)
        REFERENCES service_variants (id) ON DELETE CASCADE
);

CREATE INDEX idx_service_variant_images_variant_id ON service_variant_images (variant_id);