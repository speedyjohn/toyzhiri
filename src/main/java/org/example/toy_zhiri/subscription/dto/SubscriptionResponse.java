package org.example.toy_zhiri.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private UUID id;
    private UUID partnerId;
    private String partnerCompanyName;
    private UUID serviceId;
    private String serviceName;
    private SubscriptionPlanResponse plan;
    private String status;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private BigDecimal paidAmount;
    private String paymentMethod;
    private String paymentId;
    private LocalDateTime createdAt;
}