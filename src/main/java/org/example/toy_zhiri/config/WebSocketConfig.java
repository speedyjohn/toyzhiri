package org.example.toy_zhiri.config;

import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.auth.security.JwtChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket поверх STOMP для реалтайм-функциональности.
 *
 * Используется чатом, в перспективе — push-уведомлениями.
 *
 * Эндпоинт подключения:    /ws  (с поддержкой SockJS-фолбэка)
 * Топики (broadcast):      /topic/chats/{chatId}
 * Персональные очереди:    /user/queue/...
 * Префикс для @MessageMapping в контроллерах: /app
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    /**
     * Регистрирует эндпоинт, к которому подключается клиент.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Настраивает брокер сообщений.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Префиксы топиков для подписок
        registry.enableSimpleBroker("/topic", "/queue");

        // Префикс для сообщений, которые идут в @MessageMapping контроллеры
        registry.setApplicationDestinationPrefixes("/app");

        // Префикс для адресации конкретному пользователю (/user/queue/...)
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Регистрирует канальный интерсептор для аутентификации STOMP-фреймов через JWT.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}