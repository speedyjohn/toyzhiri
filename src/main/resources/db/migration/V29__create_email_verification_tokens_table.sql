CREATE TABLE email_verification_tokens
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    token       VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verification_tokens_token ON email_verification_tokens (token);
CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens (user_id);
CREATE INDEX idx_email_verification_tokens_expiry_date ON email_verification_tokens (expiry_date);

-- Все существующие пользователи считаются верифицированными, чтобы не сломать текущий флоу
UPDATE users
SET emailverified = true
WHERE emailverified = false;