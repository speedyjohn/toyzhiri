package org.example.toy_zhiri.auth.repository;

import org.example.toy_zhiri.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с токенами подтверждения email.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    /**
     * Находит токен подтверждения по значению.
     *
     * @param token значение токена
     * @return Optional с найденным токеном
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Удаляет все токены подтверждения пользователя.
     *
     * @param userId идентификатор пользователя
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Удаляет истекшие токены подтверждения.
     *
     * @param now текущее время
     * @return количество удалённых записей
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}