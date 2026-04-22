ALTER TABLE chat_messages
    ADD COLUMN attachment_urls text[];

-- Снимаем NOT NULL с content, потому что сообщение может состоять только из вложений
ALTER TABLE chat_messages
    ALTER COLUMN content DROP NOT NULL;

-- Гарантируем, что в сообщении есть хотя бы что-то — текст или вложения
ALTER TABLE chat_messages
    ADD CONSTRAINT chk_chat_messages_content_or_attachments
        CHECK (
            content IS NOT NULL
                OR (attachment_urls IS NOT NULL AND array_length(attachment_urls, 1) > 0)
            );
