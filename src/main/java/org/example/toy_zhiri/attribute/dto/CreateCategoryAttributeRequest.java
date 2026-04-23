package org.example.toy_zhiri.attribute.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO запроса на привязку атрибута к категории.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryAttributeRequest {

    @NotNull(message = "Идентификатор атрибута обязателен")
    private UUID attributeId;

    @NotNull(message = "Флаг обязательности обязателен")
    private Boolean isRequired;

    @NotNull(message = "Флаг фильтрации обязателен")
    private Boolean isFilterable;

    @NotNull(message = "Порядок сортировки обязателен")
    private Integer sortOrder;
}