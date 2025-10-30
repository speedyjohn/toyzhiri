package org.example.toy_zhiri.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для статистики логинов пользователя.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryStatsResponse {
    private UUID userId;
    private Long totalSuccessfulLogins;
}
