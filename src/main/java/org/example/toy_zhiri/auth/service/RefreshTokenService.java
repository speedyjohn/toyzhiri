package org.example.toy_zhiri.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.auth.entity.RefreshToken;
import org.example.toy_zhiri.auth.repository.RefreshTokenRepository;
import org.example.toy_zhiri.exception.AuthException;
import org.example.toy_zhiri.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления refresh токенами.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    /**
     * Создаёт новый refresh токен для пользователя.
     * Перед созданием удаляет все предыдущие refresh токены пользователя.
     *
     * @param user пользователь
     * @return созданный refresh токен
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Удаляем старые refresh токены пользователя
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Находит refresh токен по значению.
     *
     * @param token значение токена
     * @return Optional с найденным токеном
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Проверяет срок действия refresh токена.
     * Если токен истёк, удаляет его и выбрасывает исключение.
     *
     * @param refreshToken refresh токен для проверки
     * @return проверенный refresh токен
     * @throws AuthException если токен истёк
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException(
                    "Refresh токен истёк. Пожалуйста, выполните повторный вход.");
        }
        return refreshToken;
    }

    /**
     * Удаляет все refresh токены пользователя (при logout).
     *
     * @param userId идентификатор пользователя
     */
    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * Периодическая очистка истекших refresh токенов.
     * Выполняется каждый час.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired refresh tokens", deleted);
        }
    }
}