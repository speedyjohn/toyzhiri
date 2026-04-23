package org.example.toy_zhiri.attribute.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO запроса на обновление параметров привязки атрибута к категории.
 * Сам атрибут и категория не меняются — меняются только флаги и порядок.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryAttributeRequest {

    @NotNull(message = "Флаг обязательности обязателен")
    private Boolean isRequired;

    @NotNull(message = "Флаг фильтрации обязателен")
    private Boolean isFilterable;

    @NotNull(message = "Порядок сортировки обязателен")
    private Integer sortOrder;
}