package org.example.toy_zhiri.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для изменения статуса одобрения услуги администратором.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeServiceApprovalStatusRequest {
    @NotNull(message = "Статус одобрения обязателен")
    private Boolean isApproved;

    private String rejectionReason;
}