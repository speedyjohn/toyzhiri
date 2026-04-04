package org.example.toy_zhiri.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для конфликтов состояния (дубликаты, занятые даты и т.п.).
 * HTTP 409 Conflict.
 */
public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}