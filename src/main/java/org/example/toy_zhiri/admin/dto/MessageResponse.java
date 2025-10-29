package org.example.toy_zhiri.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для простых текстовых ответов от сервера.
 * Используется для операций, которые не возвращают объект (например, удаление).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;
}