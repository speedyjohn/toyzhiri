package org.example.toy_zhiri.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для ошибок загрузки файлов.
 * HTTP 400 Bad Request.
 */
public class FileUploadException extends BaseException {

    public FileUploadException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}