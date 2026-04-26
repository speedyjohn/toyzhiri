package org.example.toy_zhiri.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.auth.dto.*;
import org.example.toy_zhiri.auth.service.AuthService;
import org.example.toy_zhiri.auth.service.EmailVerificationService;
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
    private final EmailVerificationService emailVerificationService;

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request данные для регистрации
     * @return ResponseEntity<RegisterResponse> информация о зарегистрированном пользователе
     */
    @Operation(
            summary = "Регистрация",
            description = "Отправить запрос на регистрацию"
    )
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.registerUser(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Авторизует пользователя в системе.
     *
     * @param request     данные для авторизации
     * @param httpRequest HTTP запрос для логирования
     * @return ResponseEntity<AuthResponse> access и refresh токены
     */
    @Operation(
            summary = "Авторизация",
            description = "Отправить запрос на авторизацию. Возвращает access и refresh токены"
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Авторизует пользователя через Google ID Token.
     * Если пользователя нет в системе — создаёт новый аккаунт автоматически.
     *
     * @param request     запрос с Google ID Token
     * @param httpRequest HTTP запрос для логирования
     * @return ResponseEntity<AuthResponse> access и refresh токены
     */
    @Operation(
            summary = "Авторизация через Google",
            description = "Принимает Google ID Token от фронтенда, верифицирует его " +
                    "и возвращает пару access + refresh токенов. Автоматически создаёт " +
                    "аккаунт, если пользователь входит впервые. " +
                    "В ответе поле profileCompleted указывает, нужно ли дозаполнить профиль"
    )
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.loginWithGoogle(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновляет access токен по refresh токену.
     *
     * @param request запрос с refresh токеном
     * @return ResponseEntity<AuthResponse> новая пара access + refresh токенов
     */
    @Operation(
            summary = "Обновление токена",
            description = "Обновляет access токен по refresh токену. Не требует авторизации"
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Выход из системы (отзыв токена).
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param request     HTTP запрос для извлечения токена
     * @return ResponseEntity<LogoutResponse> сообщение об успешном выходе
     */
    @Operation(
            summary = "Выход из системы",
            description = "Отзывает текущий JWT токен и удаляет refresh токен",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        LogoutResponse response = authService.logout(userDetails, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Подтверждает email пользователя по токену из письма.
     *
     * @param request запрос с токеном подтверждения
     * @return ResponseEntity<MessageResponse> сообщение об успешном подтверждении
     */
    @Operation(
            summary = "Подтверждение email",
            description = "Подтверждает email пользователя по токену из письма. " +
                    "Токен действителен 24 часа. После подтверждения пользователь может войти в систему"
    )
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request.getToken());
        MessageResponse response = MessageResponse.builder()
                .message("Email успешно подтверждён. Теперь вы можете войти в систему")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Повторно отправляет письмо с подтверждением email.
     *
     * @param request запрос с email пользователя
     * @return ResponseEntity<MessageResponse> сообщение об успешной отправке
     */
    @Operation(
            summary = "Повторная отправка письма подтверждения",
            description = "Повторно отправляет письмо с ссылкой для подтверждения email. " +
                    "Используется, если предыдущее письмо не пришло или токен истёк"
    )
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.resendVerificationEmail(request.getEmail());
        MessageResponse response = MessageResponse.builder()
                .message("Письмо с подтверждением отправлено повторно на " + request.getEmail())
                .build();
        return ResponseEntity.ok(response);
    }
}