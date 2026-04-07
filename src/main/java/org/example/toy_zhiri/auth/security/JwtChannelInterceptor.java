package org.example.toy_zhiri.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.auth.service.TokenBlacklistService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

/**
 * Канальный интерсептор для аутентификации STOMP-фреймов через JWT.
 *
 * При получении CONNECT-фрейма извлекает токен из заголовка Authorization,
 * валидирует его тем же провайдером, что и REST, и устанавливает Principal
 * в STOMP-сессию. Все последующие фреймы (SUBSCRIBE, SEND и т.д.) уже
 * привязаны к этой сессии и не требуют повторной аутентификации.
 *
 * Концептуально это «брат» JwtAuthenticationFilter — оба валидируют JWT,
 * только один для HTTP, другой для STOMP.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Аутентифицируем только при подключении — дальше используется сессия
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT без JWT токена");
                return message;
            }

            String token = authHeader.substring(7);

            if (!tokenProvider.validateToken(token)) {
                log.warn("WebSocket CONNECT с невалидным JWT токеном");
                return message;
            }

            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("WebSocket CONNECT с отозванным JWT токеном");
                return message;
            }

            UUID userId = tokenProvider.getUserId(token);
            String email = tokenProvider.getEmail(token);

            // Принципалом будет email — это согласуется с REST,
            // где UserDetails.getUsername() тоже возвращает email
            Principal principal = new StompPrincipal(email, userId);
            accessor.setUser(principal);

            log.debug("WebSocket подключение аутентифицировано: userId={}, email={}", userId, email);
        }

        return message;
    }
}