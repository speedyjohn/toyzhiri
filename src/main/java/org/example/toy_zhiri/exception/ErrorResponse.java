package org.example.toy_zhiri.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Единый формат ответа для всех ошибок API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String error;
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Ошибки валидации полей (только для MethodArgumentNotValidException).
     * Ключ — имя поля, значение — сообщение об ошибке.
     */
    private Map<String, String> fieldErrors;
}