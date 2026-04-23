CREATE TABLE attribute_definitions
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    key              VARCHAR(100) NOT NULL UNIQUE,
    type             VARCHAR(50)  NOT NULL,
    match_strategy   VARCHAR(50)  NOT NULL,
    storage_keys     JSONB        NOT NULL,
    label_ru         VARCHAR(255) NOT NULL,
    label_kk         VARCHAR(255) NOT NULL,
    unit             VARCHAR(50),
    validation_rules JSONB,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attribute_definitions_key ON attribute_definitions (key);

CREATE TABLE category_attributes
(
    id            UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    category_id   UUID      NOT NULL,
    attribute_id  UUID      NOT NULL,
    is_required   BOOLEAN   NOT NULL DEFAULT false,
    is_filterable BOOLEAN   NOT NULL DEFAULT true,
    sort_order    INTEGER   NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_category_attributes_category FOREIGN KEY (category_id)
        REFERENCES service_categories (id) ON DELETE CASCADE,
    CONSTRAINT fk_category_attributes_attribute FOREIGN KEY (attribute_id)
        REFERENCES attribute_definitions (id) ON DELETE RESTRICT,
    CONSTRAINT uq_category_attribute UNIQUE (category_id, attribute_id)
);

CREATE INDEX idx_category_attributes_category_id ON category_attributes (category_id);
CREATE INDEX idx_category_attributes_attribute_id ON category_attributes (attribute_id);