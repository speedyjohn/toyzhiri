package org.example.toy_zhiri.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для удаления собственного аккаунта.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {
    @NotBlank(message = "Пароль обязателен для подтверждения")
    private String password;

    private String reason;
}