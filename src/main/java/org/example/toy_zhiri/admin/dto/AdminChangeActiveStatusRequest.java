package org.example.toy_zhiri.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для изменения статуса активности пользователя (блокировка/разблокировка).
 *
 * ВНИМАНИЕ: Для работы этой функции необходимо:
 * 1. Добавить поле isActive в таблицу users (миграция БД)
 * 2. Добавить поле isActive в сущность User
 * 3. Проверять это поле при аутентификации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeActiveStatusRequest {
    @NotNull(message = "Статус активности обязателен")
    private Boolean isActive;
}