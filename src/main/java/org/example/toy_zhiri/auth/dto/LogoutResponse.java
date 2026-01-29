package org.example.toy_zhiri.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа с JWT токеном после авторизации.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponse {
    private String token;
}
