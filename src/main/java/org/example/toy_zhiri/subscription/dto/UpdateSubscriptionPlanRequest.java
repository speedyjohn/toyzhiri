package org.example.toy_zhiri.subscription.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionPlanRequest {

    private String name;

    private String description;

    @Min(value = 0, message = "Цена не может быть отрицательной")
    private BigDecimal price;

    @Min(value = 1, message = "Длительность должна быть не менее 1 дня")
    private Integer durationDays;

    private Boolean isFree;

    private Integer displayOrder;
}