CREATE TABLE token_blacklist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(500) NOT NULL UNIQUE,
    user_email VARCHAR(50) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    blacklisted_at TIMESTAMP NOT NULL DEFAULT NOW()
);