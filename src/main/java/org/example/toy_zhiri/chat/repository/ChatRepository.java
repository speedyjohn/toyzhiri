package org.example.toy_zhiri.chat.repository;

import org.example.toy_zhiri.chat.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с диалогами между клиентами и партнёрами.
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    /**
     * Находит диалог по паре (user, partner).
     *
     * @param userId    идентификатор пользователя-клиента
     * @param partnerId идентификатор партнёра
     * @return Optional с найденным диалогом
     */
    Optional<Chat> findByUserIdAndPartnerId(UUID userId, UUID partnerId);

    /**
     * Возвращает все диалоги клиента, отсортированные по последнему сообщению.
     *
     * @param userId   идентификатор пользователя-клиента
     * @param pageable параметры пагинации
     * @return Page<Chat> страница с диалогами
     */
    @Query("""
            SELECT c FROM Chat c
            WHERE c.user.id = :userId
            ORDER BY COALESCE(c.lastMessageAt, c.createdAt) DESC
            """)
    Page<Chat> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Возвращает все диалоги партнёра, отсортированные по последнему сообщению.
     *
     * @param partnerId идентификатор партнёра
     * @param pageable  параметры пагинации
     * @return Page<Chat> страница с диалогами
     */
    @Query("""
            SELECT c FROM Chat c
            WHERE c.partner.id = :partnerId
            ORDER BY COALESCE(c.lastMessageAt, c.createdAt) DESC
            """)
    Page<Chat> findAllByPartnerId(@Param("partnerId") UUID partnerId, Pageable pageable);
}