package org.example.toy_zhiri.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания нового пользователя администратором.
 * Администратор может сразу установить роль и статус верификации email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateUserRequest {
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

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
    @Pattern(
        regexp = "^[a-zA-Zа-яА-ЯёЁ\\s-]+$",
        message = "Имя может содержать только буквы, пробелы и дефисы"
    )
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(min = 2, max = 100, message = "Фамилия должна быть от 2 до 100 символов")
    @Pattern(
        regexp = "^[a-zA-Zа-яА-ЯёЁ\\s-]+$",
        message = "Фамилия может содержать только буквы, пробелы и дефисы"
    )
    private String lastName;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(
        regexp = "^77\\d{9}$",
        message = "Неверный формат телефона. Ожидается формат: 77XXXXXXXXX"
    )
    private String phone;

    @NotBlank(message = "Город обязателен")
    private String city;

    @NotBlank(message = "Роль обязательна")
    @Pattern(
        regexp = "^(USER|PARTNER|ADMIN)$",
        message = "Роль должна быть одной из: USER, PARTNER, ADMIN"
    )
    private String role;

    // Опционально: администратор может сразу верифицировать email
    private Boolean emailVerified;
}