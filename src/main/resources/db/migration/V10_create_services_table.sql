CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id UUID NOT NULL,
    category_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    short_description VARCHAR(500),
    full_description TEXT,
    price_from DECIMAL(12,2),
    price_to DECIMAL(12,2),
    price_type VARCHAR(50) DEFAULT 'FIXED',
    city VARCHAR(100),
    address TEXT,
    rating DECIMAL(3,2) DEFAULT 0,
    reviews_count INT DEFAULT 0,
    views_count INT DEFAULT 0,
    bookings_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    is_approved BOOLEAN DEFAULT false,
    thumbnail VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_service_partner FOREIGN KEY (partner_id)
        REFERENCES partners(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_category FOREIGN KEY (category_id)
        REFERENCES service_categories(id) ON DELETE RESTRICT,
    CONSTRAINT chk_price_type CHECK (price_type IN ('FIXED', 'RANGE', 'NEGOTIABLE'))
);