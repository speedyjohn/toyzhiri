package org.example.toy_zhiri.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Базовый класс для всех кастомных исключений приложения.
 * Содержит HTTP-статус, который будет возвращён клиенту.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final HttpStatus httpStatus;

    protected BaseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}