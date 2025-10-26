package org.example.toy_zhiri.partner.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для одобрения или отклонения заявки на партнерство.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerApprovalRequest {
    @NotNull(message = "Решение обязательно")
    private Boolean approved;

    private String rejectionReason;
}