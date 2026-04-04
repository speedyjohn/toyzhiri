package org.example.toy_zhiri.auth.repository;

import org.example.toy_zhiri.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с refresh токенами.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Находит refresh токен по значению токена.
     *
     * @param token значение токена
     * @return Optional с найденным токеном
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Удаляет все refresh токены пользователя.
     *
     * @param userId идентификатор пользователя
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Удаляет истекшие refresh токены.
     *
     * @param now текущее время
     * @return количество удалённых записей
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}