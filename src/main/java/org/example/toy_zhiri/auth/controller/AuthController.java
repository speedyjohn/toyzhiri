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
     * @param request данные для авторизации
     * @return ResponseEntity<AuthResponse> JWT токен для доступа
     */
    @Operation(
        summary = "Авторизация",
        description = "Отправить запрос на авторизацию"
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody AuthRequest request,
        HttpServletRequest httpRequest)
    {
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Выход из системы (отзыв токена).
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param request HTTP запрос для извлечения токена
     * @return ResponseEntity<LogoutResponse> сообщение об успешном выходе
     */
    @Operation(
            summary = "Выход из системы",
            description = "Отзывает текущий JWT токен и логирует выход",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request)
    {
        LogoutResponse response = authService.logout(userDetails, request);
        return ResponseEntity.ok(response);
    }
}