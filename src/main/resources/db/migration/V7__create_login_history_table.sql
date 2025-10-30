CREATE TABLE login_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    email VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    login_type VARCHAR(20) NOT NULL CHECK (login_type IN ('LOGIN', 'LOGOUT')),
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_login_history_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);