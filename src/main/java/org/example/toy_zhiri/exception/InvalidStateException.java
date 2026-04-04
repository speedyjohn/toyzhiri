package org.example.toy_zhiri.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для операций, невозможных в текущем состоянии сущности.
 * Например, попытка подтвердить уже отменённое бронирование.
 * HTTP 422 Unprocessable Entity.
 */
public class InvalidStateException extends BaseException {

    public InvalidStateException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}