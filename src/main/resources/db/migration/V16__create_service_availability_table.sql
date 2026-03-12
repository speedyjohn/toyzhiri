CREATE TABLE service_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    note VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_availability_service FOREIGN KEY (service_id)
        REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT chk_availability_status CHECK (status IN ('AVAILABLE', 'BLOCKED')),
    CONSTRAINT uq_service_date UNIQUE (service_id, date)
);

CREATE INDEX idx_availability_service_id ON service_availability(service_id);
CREATE INDEX idx_availability_date ON service_availability(date);
CREATE INDEX idx_availability_service_date_status ON service_availability(service_id, date, status);