package org.example.toy_zhiri.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса регистрации нового пользователя.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
            message = "Пароль должен содержать: минимум 1 строчную букву, 1 заглавную, 1 цифру и 1 спецсимвол (@$!%*?&#)"
    )
    private String password;

    @NotBlank(message = "Полное имя обязательно")
    @Size(min = 2, max = 255, message = "Имя должно быть от 2 до 255 символов")
    private String fullName;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(
            regexp = "^77\\d{9}$",
            message = "Неверный формат телефона. Ожидается формат: 77XXXXXXXXX"
    )
    private String phone;

    @NotBlank(message = "Город обязателен")
    private String city;
}
