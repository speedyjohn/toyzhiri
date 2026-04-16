package org.example.toy_zhiri.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO события «сообщения прочитаны».
 * Отправляется через STOMP в персональную очередь отправителю,
 * чьи сообщения только что были прочитаны другой стороной.
 *
 * Фронт по этому событию меняет галочки с одной (отправлено) на две (прочитано).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReadEvent {

    /** ID чата, в котором сообщения были прочитаны */
    private UUID chatId;

    /** ID пользователя, который прочитал (получатель сообщений) */
    private UUID readBy;

    /** Когда были прочитаны */
    private LocalDateTime readAt;

    /** Сколько сообщений было помечено прочитанными */
    private Integer markedCount;
}