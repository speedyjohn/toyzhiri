package org.example.toy_zhiri.user.enums;

/**
 * Провайдер авторизации пользователя.
 * LOCAL — регистрация по email/паролю в нашей системе.
 * GOOGLE — вход через Google OAuth 2.0.
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE
}