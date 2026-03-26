package org.example.toy_zhiri.payment.dto;

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
public class PaymentResponse {
    private UUID paymentId;
    private UUID subscriptionId;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal amount;
    private String message;
    private LocalDateTime processedAt;
}