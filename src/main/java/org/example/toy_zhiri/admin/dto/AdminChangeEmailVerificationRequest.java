package org.example.toy_zhiri.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для изменения статуса верификации email пользователя.
 * Администратор может вручную верифицировать или снять верификацию email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeEmailVerificationRequest {
    @NotNull(message = "Статус верификации обязателен")
    private Boolean emailVerified;
}