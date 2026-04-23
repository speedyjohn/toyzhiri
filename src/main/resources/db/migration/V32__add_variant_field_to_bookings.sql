ALTER TABLE bookings
    ADD COLUMN variant_id UUID,
    ADD COLUMN customer_notes TEXT;

ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_variant FOREIGN KEY (variant_id)
        REFERENCES service_variants (id) ON DELETE RESTRICT;

CREATE INDEX idx_bookings_variant_id ON bookings (variant_id);