package org.example.toy_zhiri.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для ошибок аутентификации (неверные учётные данные, истёкший токен и т.п.).
 * HTTP 401 Unauthorized.
 */
public class AuthException extends BaseException {

    public AuthException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}