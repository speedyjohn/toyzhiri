-- Диалог между клиентом и партнёром (одна запись на пару)
CREATE TABLE chats (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    partner_id      UUID NOT NULL,
    last_message_at TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_chats_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chats_partner FOREIGN KEY (partner_id)
        REFERENCES partners(id) ON DELETE CASCADE,
    CONSTRAINT uq_chats_user_partner UNIQUE (user_id, partner_id)
);

CREATE INDEX idx_chats_user_id ON chats(user_id);
CREATE INDEX idx_chats_partner_id ON chats(partner_id);
CREATE INDEX idx_chats_last_message_at ON chats(last_message_at DESC);

-- Сообщения чата
CREATE TABLE chat_messages (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id    UUID NOT NULL,
    sender_id  UUID NOT NULL,
    content    TEXT NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT false,
    read_at    TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_chat_messages_chat FOREIGN KEY (chat_id)
        REFERENCES chats(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_messages_chat_id ON chat_messages(chat_id);
CREATE INDEX idx_chat_messages_chat_created ON chat_messages(chat_id, created_at DESC);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_unread ON chat_messages(chat_id, is_read) WHERE is_read = false;