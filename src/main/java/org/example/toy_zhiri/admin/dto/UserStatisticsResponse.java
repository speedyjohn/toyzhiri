package org.example.toy_zhiri.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для статистики по пользователям.
 * Содержит агрегированную информацию о количестве пользователей по различным категориям.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private Long totalUsers;
    private Long userRoleCount;
    private Long partnerRoleCount;
    private Long adminRoleCount;
    private Long verifiedEmailCount;
    private Long unverifiedEmailCount;
}