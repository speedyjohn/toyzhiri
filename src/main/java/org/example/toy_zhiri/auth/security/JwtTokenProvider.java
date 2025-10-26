package org.example.toy_zhiri.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * Провайдер для работы с JWT токенами.
 * Обеспечивает генерацию, вавидацию, извлечение данных из JWT токена.
 */
@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Получает ключ для подписи JWT токенов.
     *
     * @return ключ для подписи на основе секретной строки
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Генерирует JWT токен для пользователя.
     *
     * @param userId идентификатор пользователя
     * @param email email пользователя
     * @param role роль пользователя
     * @return сгенерированный JWT токен в виде строки
     */
    public String generateToken(UUID userId, String email, String role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Извлекает claims (данные) из JWT токена.
     *
     * @param token JWT токен
     * @return объект Claims с данными из токена
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Извлекает идентификатор пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return идентификатор пользователя
     */
    public UUID getUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    /**
     * Извлекает email пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return email пользователя
     */
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /**
     * Извлекает роль пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return роль пользователя
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Проверяет валидность JWT токена.
     *
     * @param token JWT токен для проверки
     * @return true если токен валиден, false в противном случае
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}