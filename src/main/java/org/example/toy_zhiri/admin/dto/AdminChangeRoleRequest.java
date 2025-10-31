package org.example.toy_zhiri.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для изменения роли пользователя.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeRoleRequest {
    @NotBlank(message = "Роль обязательна")
    @Pattern(
        regexp = "^(USER|PARTNER|ADMIN)$",
        message = "Роль должна быть одной из: USER, PARTNER, ADMIN"
    )
    private String role;
}