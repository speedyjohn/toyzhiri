package org.example.toy_zhiri.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для случаев, когда у пользователя нет прав на выполнение операции.
 * HTTP 403 Forbidden.
 */
public class AccessDeniedException extends BaseException {

    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}