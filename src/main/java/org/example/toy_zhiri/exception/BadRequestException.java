package org.example.toy_zhiri.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для невалидных запросов и нарушений бизнес-логики.
 * HTTP 400 Bad Request.
 */
public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}