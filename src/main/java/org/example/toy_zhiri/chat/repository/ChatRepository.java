package org.example.toy_zhiri.chat.repository;

import org.example.toy_zhiri.chat.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    /**
     * Находит диалог по паре (user, partner).
     */
    Optional<Chat> findByUserIdAndPartnerId(UUID userId, UUID partnerId);

    /**
     * Все диалоги клиента, отсортированные по последнему сообщению.
     */
    @Query("""
            SELECT c FROM Chat c
            WHERE c.user.id = :userId
            ORDER BY COALESCE(c.lastMessageAt, c.createdAt) DESC
            """)
    Page<Chat> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Все диалоги партнёра, отсортированные по последнему сообщению.
     */
    @Query("""
            SELECT c FROM Chat c
            WHERE c.partner.id = :partnerId
            ORDER BY COALESCE(c.lastMessageAt, c.createdAt) DESC
            """)
    Page<Chat> findAllByPartnerId(@Param("partnerId") UUID partnerId, Pageable pageable);
}