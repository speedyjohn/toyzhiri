package org.example.toy_zhiri.attribute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO запроса на создание определения атрибута.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttributeDefinitionRequest {

    @NotBlank(message = "Ключ атрибута обязателен")
    @Size(max = 100, message = "Ключ не должен превышать 100 символов")
    @Pattern(
            regexp = "^[a-z][a-z0-9_]*$",
            message = "Ключ должен начинаться с буквы и содержать только строчные латинские буквы, цифры и _"
    )
    private String key;

    @NotBlank(message = "Тип обязателен")
    @Pattern(
            regexp = "^(INTEGER|STRING|BOOLEAN|STRING_ARRAY)$",
            message = "Тип должен быть одним из: INTEGER, STRING, BOOLEAN, STRING_ARRAY"
    )
    private String type;

    @NotBlank(message = "Стратегия сравнения обязательна")
    @Pattern(
            regexp = "^(SINGLE_EQ|SINGLE_GTE|SINGLE_LTE|RANGE_CONTAINS|BOOLEAN_MATCH|ARRAY_CONTAINS|ARRAY_INTERSECTS)$",
            message = "Недопустимая стратегия сравнения"
    )
    private String matchStrategy;

    @NotNull(message = "Ключи хранения обязательны")
    @NotEmpty(message = "Ключи хранения не могут быть пустыми")
    private Map<String, String> storageKeys;

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