package org.example.toy_zhiri.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.auth.entity.TokenBlacklist;
import org.example.toy_zhiri.auth.repository.TokenBlacklistRepository;
import org.example.toy_zhiri.auth.security.JwtTokenProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Сервис для управления blacklist токенов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Добавляет токен в черный список.
     *
     * @param token JWT токен
     */
    @Transactional
    public void blacklistToken(String token) {
        // Извлекаем информацию из токена
        Claims claims = jwtTokenProvider.getClaims(token);
        String email = claims.get("email", String.class);
        Date expiration = claims.getExpiration();

        LocalDateTime expiryDate = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .userEmail(email)
                .expiryDate(expiryDate)
                .build();

        tokenBlacklistRepository.save(blacklist);
        log.info("Token blacklisted for user: {}", email);
    }

    /**
     * Проверяет, находится ли токен в черном списке.
     *
     * @param token JWT токен
     * @return true если токен заблокирован
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    /**
     * Периодическая очистка истекших токенов из blacklist.
     * Выполняется каждый час.
     */
    @Scheduled(cron = "0 0 * * * *") // Каждый час
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired tokens from blacklist", deleted);
        }
    }
}