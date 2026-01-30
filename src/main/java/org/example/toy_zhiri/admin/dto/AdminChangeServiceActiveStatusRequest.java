package org.example.toy_zhiri.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для изменения статуса активности услуги администратором.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeServiceActiveStatusRequest {
    @NotNull(message = "Статус активности обязателен")
    private Boolean isActive;
}