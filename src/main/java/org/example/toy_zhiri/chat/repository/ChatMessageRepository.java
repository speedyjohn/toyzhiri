package org.example.toy_zhiri.chat.repository;

import org.example.toy_zhiri.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * История сообщений чата с пагинацией (новые сверху).
     */
    Page<ChatMessage> findByChatIdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);

    /**
     * Последнее сообщение в чате (для превью в списке диалогов).
     */
    Optional<ChatMessage> findFirstByChatIdOrderByCreatedAtDesc(UUID chatId);

    /**
     * Количество непрочитанных сообщений в чате для конкретного получателя
     * (сообщения, отправленные НЕ им).
     */
    @Query("""
            SELECT COUNT(m) FROM ChatMessage m
            WHERE m.chat.id = :chatId
              AND m.sender.id <> :recipientId
              AND m.isRead = false
            """)
    long countUnreadForRecipient(@Param("chatId") UUID chatId,
                                 @Param("recipientId") UUID recipientId);

    /**
     * Общее количество непрочитанных сообщений пользователя по всем его чатам
     * (для бейджика в шапке).
     */
    @Query("""
            SELECT COUNT(m) FROM ChatMessage m
            WHERE m.sender.id <> :userId
              AND m.isRead = false
              AND (m.chat.user.id = :userId OR m.chat.partner.user.id = :userId)
            """)
    long countAllUnreadForUser(@Param("userId") UUID userId);

    /**
     * Помечает все непрочитанные сообщения чата как прочитанные
     * (только те, что были отправлены НЕ текущим пользователем).
     */
    @Modifying
    @Query("""
            UPDATE ChatMessage m
               SET m.isRead = true,
                   m.readAt = :readAt
             WHERE m.chat.id = :chatId
               AND m.sender.id <> :recipientId
               AND m.isRead = false
            """)
    int markAllAsReadForRecipient(@Param("chatId") UUID chatId,
                                  @Param("recipientId") UUID recipientId,
                                  @Param("readAt") LocalDateTime readAt);
}