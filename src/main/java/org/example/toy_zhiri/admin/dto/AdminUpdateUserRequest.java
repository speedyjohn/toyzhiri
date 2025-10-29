package org.example.toy_zhiri.admin.dto;

import jakarta.validation.constraints.Email;
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

    @Size(min = 2, max = 255, message = "Имя должно быть от 2 до 255 символов")
    private String fullName;

    @Pattern(
            regexp = "^77\\d{9}$",
            message = "Неверный формат телефона. Ожидается формат: 77XXXXXXXXX"
    )
    private String phone;

    private String city;
}