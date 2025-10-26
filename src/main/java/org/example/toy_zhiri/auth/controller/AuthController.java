package org.example.toy_zhiri.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.auth.dto.RegisterRequest;
import org.example.toy_zhiri.auth.dto.RegisterResponse;
import org.example.toy_zhiri.auth.dto.AuthRequest;
import org.example.toy_zhiri.auth.dto.AuthResponse;
import org.example.toy_zhiri.auth.service.AuthService;
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
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }
}