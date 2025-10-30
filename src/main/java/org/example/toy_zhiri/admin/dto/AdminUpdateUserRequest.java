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
 * DTO для обновления информации о пользователе администратором.
 * Все поля опциональны - обновляются только те, которые переданы.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {
    @Email(message = "Неверный формат email")
    private String email;

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

    @Pattern(
            regexp = "^77\\d{9}$",
            message = "Неверный формат телефона. Ожидается формат: 77XXXXXXXXX"
    )
    private String phone;

    private String city;
}