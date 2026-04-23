package org.example.toy_zhiri.attribute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO запроса на обновление определения атрибута.
 * Меняются только "мягкие" поля: лейблы, единица измерения, правила валидации.
 * Поля key, type, matchStrategy, storageKeys — неизменяемы после создания,
 * чтобы не ломать существующие значения в вариантах услуг.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttributeDefinitionRequest {

    @NotBlank(message = "Русский лейбл обязателен")
    @Size(max = 255)
    private String labelRu;

    @NotBlank(message = "Казахский лейбл обязателен")
    @Size(max = 255)
    private String labelKk;

    @Size(max = 50)
    private String unit;

    private Map<String, Object> validationRules;
}