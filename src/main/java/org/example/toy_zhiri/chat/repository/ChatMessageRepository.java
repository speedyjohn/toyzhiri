package org.example.toy_zhiri.chat.repository;

import org.example.toy_zhiri.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сообщениями в чатах.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Возвращает историю сообщений чата с пагинацией (новые сверху).
     *
     * @param chatId идентификатор чата
     * @param pageable параметры пагинации
     * @return Page<ChatMessage> страница сообщений
     */
    Page<ChatMessage> findByChatIdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);

    /**
     * Находит последнее сообщение в чате (для превью в списке диалогов).
     *
     * @param chatId идентификатор чата
     * @return Optional с последним сообщением
     */
    Optional<ChatMessage> findFirstByChatIdOrderByCreatedAtDesc(UUID chatId);

    /**
     * Подсчитывает количество непрочитанных сообщений в чате
     * для конкретного получателя (сообщения, отправленные НЕ им).
     *
     * @param chatId идентификатор чата
     * @param recipientId идентификатор получателя
     * @return количество непрочитанных сообщений
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
     * Подсчитывает общее количество непрочитанных сообщений пользователя
     * по всем его чатам (для бейджика в шапке).
     *
     * @param userId идентификатор пользователя
     * @return общее количество непрочитанных сообщений
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
     *
     * @param chatId идентификатор чата
     * @param recipientId идентификатор получателя (тот, кто читает)
     * @param readAt время прочтения
     * @return количество обновлённых записей
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