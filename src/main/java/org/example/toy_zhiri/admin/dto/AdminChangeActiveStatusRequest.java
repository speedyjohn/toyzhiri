package org.example.toy_zhiri.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для изменения статуса активности пользователя (блокировка/разблокировка).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeActiveStatusRequest {
    @NotNull(message = "Статус активности обязателен")
    private Boolean isActive;
}