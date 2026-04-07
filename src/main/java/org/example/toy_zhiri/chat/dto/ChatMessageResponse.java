package org.example.toy_zhiri.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private UUID id;
    private UUID chatId;

    private UUID senderId;
    private String senderFullName;

    private String content;
    private List<String> attachmentUrls;

    private Boolean isRead;
    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}