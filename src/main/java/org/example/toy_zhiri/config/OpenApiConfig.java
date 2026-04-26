package org.example.toy_zhiri.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Настройки и описание для Swagger.
 * <p>
 * Группы:
 * - Public        — публичные эндпоинты (каталог услуг, партнёры, тарифы, публичные отзывы, stories)
 * - Auth          — регистрация, вход, refresh токен
 * - Client        — действия клиента (бронирования, корзина, избранное, отзывы, профиль, файлы)
 * - Partner       — личный кабинет партнёра (услуги, бронирования, подписки, оплата, файлы)
 * - Notifications — уведомления и настройки уведомлений
 * - Chat          — чаты и сообщения между клиентами и партнёрами
 * - Admin         — административная панель
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Toy Zhiri API",
                version = "1.0",
                description = "REST API для платформы Toy Zhiri",
                contact = @Contact(
                        name = "Toy Zhiri Support",
                        email = "support@toyzhiri.kz"
                )
        ),
        servers = {
                @Server(
                        description = "Local Environment",
                        url = "http://localhost:8080"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT авторизация. Введите токен в формате: Bearer {token}",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("1. Public")
                .pathsToMatch(
                        "/api/v1/services/**",
                        "/api/v1/partners/**",
                        "/api/v1/subscription-plans/**",
                        "/api/v1/reviews/service/**",
                        "/api/v1/reviews/partner/**",
                        "/api/v1/reviews/partner/**",
                        "/api/v1/stories/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("2. Auth")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi clientApi() {
        return GroupedOpenApi.builder()
                .group("3. Client")
                .pathsToMatch(
                        "/api/v1/users/**",
                        "/api/v1/bookings/**",
                        "/api/v1/cart/**",
                        "/api/v1/favorites/**",
                        "/api/v1/reviews/**",
                        "/api/v1/files/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi partnerApi() {
        return GroupedOpenApi.builder()
                .group("4. Partner")
                .pathsToMatch(
                        "/api/v1/partner/**",
                        "/api/v1/subscriptions/**",
                        "/api/v1/payments/**",
                        "/api/v1/files/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi notificationsApi() {
        return GroupedOpenApi.builder()
                .group("5. Notifications")
                .pathsToMatch("/api/v1/notifications/**")
                .build();
    }


    @Bean
    public GroupedOpenApi chatApi() {
        return GroupedOpenApi.builder()
                .group("6. Chat")
                .pathsToMatch("/api/v1/chats/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("7. Admin")
                .pathsToMatch("/api/v1/admin/**")
                .build();
    }
}