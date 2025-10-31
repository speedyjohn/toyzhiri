package org.example.toy_zhiri.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.auth.dto.RegisterRequest;
import org.example.toy_zhiri.auth.dto.RegisterResponse;
import org.example.toy_zhiri.auth.dto.AuthRequest;
import org.example.toy_zhiri.auth.dto.AuthResponse;
import org.example.toy_zhiri.auth.service.AuthService;
import org.example.toy_zhiri.auth.service.LoginHistoryService;
import org.example.toy_zhiri.auth.service.TokenBlacklistService;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для обработки запросов аутентификации и регистрации.
 */
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "API для аутентификации")
public class AuthController {
    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginHistoryService loginHistoryService;
    private final UserService userService;

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request данные для регистрации
     * @return RegisterResponse информация о зарегистрированном пользователе
     */
    @Operation(
        summary = "Регистрация",
        description = "Отправить запрос на регистрацию"
    )
    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.registerUser(request);
    }

    /**
     * Авторизует пользователя в системе.
     *
     * @param request данные для авторизации
     * @return AuthResponse JWT токен для доступа
     */
    @Operation(
        summary = "Авторизация",
        description = "Отправить запрос на авторизацию"
    )
    @PostMapping("/login")
    public AuthResponse login(
        @Valid @RequestBody AuthRequest request,
        HttpServletRequest httpRequest)
    {
        return authService.login(request, httpRequest);
    }

    /**
     * Выход из системы (отзыв токена).
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param request HTTP запрос для извлечения токена
     * @return сообщение об успешном выходе
     */
    @Operation(
            summary = "Выход из системы",
            description = "Отзывает текущий JWT токен и логирует выход",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        // Извлекаем токен из заголовка
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // Добавляем токен в blacklist
            tokenBlacklistService.blacklistToken(token);

            // Логируем выход
            User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
            loginHistoryService.logLogout(user, request);

            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Вы успешно вышли из системы")
                    .build());
        }

        return ResponseEntity.badRequest().body(MessageResponse.builder()
                .message("Токен не найден")
                .build());
    }
}