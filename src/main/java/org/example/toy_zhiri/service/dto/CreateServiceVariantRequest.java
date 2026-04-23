package org.example.toy_zhiri.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO запроса на создание варианта услуги партнёром.
 * Значения в поле attributes должны соответствовать схеме атрибутов категории услуги.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceVariantRequest {

    @NotBlank(message = "Название варианта обязательно")
    @Size(max = 200, message = "Название не должно превышать 200 символов")
    private String name;

    private String description;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.0", message = "Цена не может быть отрицательной")
    private BigDecimal price;

    /**
     * Значения атрибутов варианта.
     * Ключи и типы значений должны соответствовать схеме, возвращаемой
     * GET /api/v1/categories/{slug}/attribute-schema для категории родительской услуги.
     */
    @NotNull(message = "Атрибуты обязательны (может быть пустой объект, если схема пуста)")
    private Map<String, Object> attributes;

    private List<String> imageUrls;

    private Integer sortOrder;
}