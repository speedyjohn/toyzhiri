package org.example.toy_zhiri.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для превью диалога в списке чатов.
 * Содержит данные собеседника, последнее сообщение и счётчик непрочитанных.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private UUID id;

    // Клиент
    private UUID userId;
    private String userFullName;

    // Партнёр
    private UUID partnerId;
    private String partnerCompanyName;
    private String partnerLogoUrl;

    // Превью последнего сообщения
    private String lastMessageContent;
    private UUID lastMessageSenderId;
    private LocalDateTime lastMessageAt;

    // Количество непрочитанных сообщений для текущего пользователя
    private Long unreadCount;

    private LocalDateTime createdAt;
}