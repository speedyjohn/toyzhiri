package org.example.toy_zhiri.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для ответа с информацией о партнерской заявке.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userFullName;
    private String bin;
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedByEmail;
}