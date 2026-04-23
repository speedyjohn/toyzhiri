package org.example.toy_zhiri.attribute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO ответа по привязке атрибута к категории.
 * Включает вложенный объект самого атрибута для удобства отображения в админке.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAttributeResponse {

    private UUID id;
    private UUID categoryId;
    private AttributeDefinitionResponse attribute;
    private Boolean isRequired;
    private Boolean isFilterable;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}