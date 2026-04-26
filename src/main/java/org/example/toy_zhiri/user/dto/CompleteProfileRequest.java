package org.example.toy_zhiri.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для дозаполнения профиля пользователями, вошедшими через OAuth-провайдера.
 * Содержит только поля, которые провайдер не отдаёт (телефон, город).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileRequest {
    @NotBlank(message = "Телефон обязателен")
    @Pattern(
            regexp = "^77\\d{9}$",
            message = "Неверный формат телефона. Ожидается формат: 77XXXXXXXXX"
    )
    private String phone;

    @NotBlank(message = "Город обязателен")
    private String city;
}