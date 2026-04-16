package org.example.toy_zhiri.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.auth.security.StompPrincipal;
import org.example.toy_zhiri.chat.dto.ChatMessageResponse;
import org.example.toy_zhiri.chat.dto.SendMessageRequest;
import org.example.toy_zhiri.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * STOMP-контроллер для отправки сообщений через WebSocket.
 * <p>
 * Клиент отправляет сообщения на:
 * /app/chat.send/{chatId}
 * <p>
 * Сервер публикует их подписчикам в:
 * /topic/chats/{chatId}
 * <p>
 * Сама публикация в топик происходит внутри ChatService — это значит,
 * что REST-эндпоинт POST /api/v1/chats/{chatId}/messages тоже автоматически
 * рассылает сообщения по WebSocket. Единая точка записи в БД.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * Принимает сообщение от клиента через STOMP, сохраняет и публикует в топик.
     * Возвращает результат отправителю в персональную очередь /user/queue/chat-ack —
     * это позволяет фронту понять, что сообщение действительно сохранилось,
     * и получить его реальный ID и timestamp.
     */
    @MessageMapping("/chat.send/{chatId}")
    @SendToUser("/queue/chat-ack")
    public ChatMessageResponse sendMessage(
            @DestinationVariable UUID chatId,
            @Valid @Payload SendMessageRequest request,
            Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new IllegalStateException("WebSocket-сессия не аутентифицирована");
        }

        UUID senderId = stompPrincipal.getUserId();
        log.debug("WebSocket-сообщение в чат {} от пользователя {}", chatId, senderId);

        return chatService.sendMessage(
                chatId, senderId, request.getContent(), request.getAttachmentUrls());
    }
}