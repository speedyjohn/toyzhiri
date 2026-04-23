package org.example.toy_zhiri.attribute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Элемент схемы атрибутов для категории.
 * Возвращается фронту при запросе GET /api/v1/categories/{slug}/attribute-schema.
 * Используется как для построения формы партнёра (создание варианта услуги),
 * так и для построения чекбоксов клиента в форме бронирования.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeSchemaItemResponse {

    private UUID attributeId;
    private String key;
    private String type;
    private String matchStrategy;
    private Map<String, String> storageKeys;
    private String labelRu;
    private String labelKk;
    private String unit;
    private Map<String, Object> validationRules;
    private Boolean isRequired;
    private Boolean isFilterable;
    private Integer sortOrder;
}