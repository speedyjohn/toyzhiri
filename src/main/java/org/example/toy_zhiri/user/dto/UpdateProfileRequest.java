package org.example.toy_zhiri.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления профиля пользователем.
 * Пользователь НЕ может изменять: email, role, emailVerified.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
    @Pattern(
        regexp = "^[a-zA-Zа-яА-ЯёЁ\\s-]+$",
        message = "Имя может содержать только буквы, пробелы и дефисы"
    )
    private String firstName;

    @Size(min = 2, max = 100, message = "Фамилия должна быть от 2 до 100 символов")
    @Pattern(
            regexp = "^[a-zA-Zа-яА-ЯёЁ\\s-]+$",
            message = "Фамилия может содержать только буквы, пробелы и дефисы"
    )
    private String lastName;

    @Pattern(
            regexp = "^77\\d{9}$",
            message = "Неверный формат телефона. Ожидается формат: 77XXXXXXXXX"
    )
    private String phone;

    private String city;
}