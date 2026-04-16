package org.example.toy_zhiri.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для запроса создания (или получения существующего) диалога с партнёром.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRequest {

    @NotNull(message = "ID партнёра обязателен")
    private UUID partnerId;
}