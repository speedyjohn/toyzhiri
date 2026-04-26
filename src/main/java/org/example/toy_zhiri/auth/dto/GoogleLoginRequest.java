package org.example.toy_zhiri.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса входа через Google.
 * Содержит ID Token, полученный фронтендом от Google Sign-In.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {
    @NotBlank(message = "Google ID Token обязателен")
    private String idToken;
}