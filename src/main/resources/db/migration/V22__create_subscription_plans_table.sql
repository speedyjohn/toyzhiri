CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    duration_days INT NOT NULL,
    is_free BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    display_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_plan_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_plan_price CHECK (price >= 0),
    CONSTRAINT chk_plan_duration CHECK (duration_days >= 1)
);

CREATE INDEX idx_subscription_plans_slug ON subscription_plans(slug);
CREATE INDEX idx_subscription_plans_status ON subscription_plans(status);