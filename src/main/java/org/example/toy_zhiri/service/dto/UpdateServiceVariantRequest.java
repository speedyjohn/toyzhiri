package org.example.toy_zhiri.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO запроса на обновление варианта услуги.
 * Все поля опциональны — обновляются только переданные.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceVariantRequest {

    @Size(max = 200, message = "Название не должно превышать 200 символов")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", message = "Цена не может быть отрицательной")
    private BigDecimal price;

    private Map<String, Object> attributes;

    private List<String> imageUrls;

    private Integer sortOrder;

    private Boolean isActive;
}