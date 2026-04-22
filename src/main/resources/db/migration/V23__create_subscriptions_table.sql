CREATE TABLE subscriptions
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    partner_id     UUID        NOT NULL,
    service_id     UUID        NOT NULL,
    plan_id        UUID        NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    starts_at      TIMESTAMP,
    expires_at     TIMESTAMP,
    paid_amount    DECIMAL(12, 2),
    payment_method VARCHAR(20),
    payment_id     VARCHAR(255),
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_subscription_partner FOREIGN KEY (partner_id)
        REFERENCES partners (id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_service FOREIGN KEY (service_id)
        REFERENCES services (id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_plan FOREIGN KEY (plan_id)
        REFERENCES subscription_plans (id) ON DELETE RESTRICT,
    CONSTRAINT chk_subscription_status CHECK (status IN (
                                                         'PENDING', 'ACTIVE', 'EXPIRED', 'CANCELLED'
        )),
    CONSTRAINT chk_payment_method CHECK (payment_method IS NULL OR payment_method IN (
                                                                                      'KASPI', 'BANK_CARD',
                                                                                      'GOOGLE_PAY', 'APPLE_PAY'
        ))
);

CREATE INDEX idx_subscriptions_partner_id ON subscriptions (partner_id);
CREATE INDEX idx_subscriptions_service_id ON subscriptions (service_id);
CREATE INDEX idx_subscriptions_plan_id ON subscriptions (plan_id);
CREATE INDEX idx_subscriptions_status ON subscriptions (status);
CREATE INDEX idx_subscriptions_expires_at ON subscriptions (expires_at) WHERE status = 'ACTIVE';