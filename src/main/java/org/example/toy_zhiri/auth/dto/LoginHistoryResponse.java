package org.example.toy_zhiri.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для ответа с информацией о входе/выходе.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryResponse {
    private UUID id;
    private String email;
    private String ipAddress;
    private String userAgent;
    private String loginType;
    private Boolean success;
    private String failureReason;
    private LocalDateTime createdAt;
}