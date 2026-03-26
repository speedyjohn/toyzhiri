package org.example.toy_zhiri.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Boolean isFree;
    private String status;
    private Integer displayOrder;
}