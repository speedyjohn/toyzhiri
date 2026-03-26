package org.example.toy_zhiri.subscription.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionPlanRequest {

    @NotBlank(message = "Название тарифа обязательно")
    private String name;

    @NotBlank(message = "Slug обязателен")
    private String slug;

    private String description;

    @NotNull(message = "Цена обязательна")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private BigDecimal price;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 1, message = "Длительность должна быть не менее 1 дня")
    private Integer durationDays;

    private Boolean isFree = false;

    private Integer displayOrder = 0;
}