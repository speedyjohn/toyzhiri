package org.example.toy_zhiri.attribute.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.attribute.dto.AttributeSchemaItemResponse;
import org.example.toy_zhiri.attribute.entity.AttributeDefinition;
import org.example.toy_zhiri.attribute.entity.CategoryAttribute;
import org.example.toy_zhiri.attribute.enums.AttributeType;
import org.example.toy_zhiri.attribute.enums.MatchStrategy;
import org.example.toy_zhiri.attribute.repository.CategoryAttributeRepository;
import org.example.toy_zhiri.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для работы со схемой атрибутов категории.
 * Отвечает за:
 * - выдачу схемы фронту (для построения форм и чекбоксов);
 * - валидацию значений атрибутов варианта услуги при создании/обновлении партнёром;
 * - матчинг клиентских фильтров с атрибутами варианта при поиске.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeSchemaService {

    private final CategoryAttributeRepository categoryAttributeRepository;

    /**
     * Возвращает полную схему атрибутов для категории (и обязательные, и опциональные).
     * Используется партнёром при создании/редактировании варианта услуги.
     */
    public List<AttributeSchemaItemResponse> getFullSchema(UUID categoryId) {
        return categoryAttributeRepository
                .findByCategoryIdOrderBySortOrderAsc(categoryId)
                .stream()
                .map(this::mapToSchemaItem)
                .toList();
    }

    /**
     * Возвращает клиент-видимую схему атрибутов категории (только is_filterable=true).
     * Используется клиентом в форме бронирования как чекбоксы и в фильтрах каталога.
     */
    public List<AttributeSchemaItemResponse> getClientSchema(UUID categoryId) {
        return categoryAttributeRepository
                .findByCategoryIdAndIsFilterableTrueOrderBySortOrderAsc(categoryId)
                .stream()
                .map(this::mapToSchemaItem)
                .toList();
    }

    /**
     * Валидирует значения атрибутов варианта услуги.
     * Проверяет:
     * - все required-атрибуты категории присутствуют в значениях;
     * - нет неизвестных ключей (которых нет в схеме);
     * - типы значений соответствуют типу атрибута;
     * - значения проходят validation_rules.
     *
     * @param categoryId идентификатор категории родительской услуги
     * @param values     значения атрибутов из запроса партнёра (в формате "хранения": capacity_min, capacity_max и т.д.)
     * @throws BadRequestException при любой ошибке валидации
     */
    public void validateVariantAttributes(UUID categoryId, Map<String, Object> values) {
        List<CategoryAttribute> schema = categoryAttributeRepository
                .findByCategoryIdOrderBySortOrderAsc(categoryId);

        Map<String, Object> safeValues = values == null ? Map.of() : values;

        // Собираем все допустимые storage-ключи из схемы
        Map<String, CategoryAttribute> storageKeyToBinding = new HashMap<>();
        for (CategoryAttribute binding : schema) {
            AttributeDefinition def = binding.getAttribute();
            for (String storageKey : def.getStorageKeys().values()) {
                storageKeyToBinding.put(storageKey, binding);
            }
        }

        // Проверяем, что нет неизвестных ключей
        for (String key : safeValues.keySet()) {
            if (!storageKeyToBinding.containsKey(key)) {
                throw new BadRequestException(
                        "Неизвестный атрибут '" + key + "' для данной категории услуги");
            }
        }

        // Проверяем required + типы + validation_rules
        for (CategoryAttribute binding : schema) {
            AttributeDefinition def = binding.getAttribute();
            Map<String, String> storageKeys = def.getStorageKeys();

            boolean anyPresent = storageKeys.values().stream().anyMatch(safeValues::containsKey);
            boolean allPresent = storageKeys.values().stream().allMatch(safeValues::containsKey);

            if (binding.getIsRequired() && !allPresent) {
                throw new BadRequestException(
                        "Обязательный атрибут '" + def.getKey() + "' не заполнен");
            }

            // Если часть ключей передана, а часть нет — это ошибка (актуально для RANGE_CONTAINS)
            if (anyPresent && !allPresent) {
                throw new BadRequestException(
                        "Атрибут '" + def.getKey() + "' должен быть заполнен полностью " +
                                "(ожидаются ключи: " + storageKeys.values() + ")");
            }

            if (!anyPresent) {
                // Атрибут опционален и не передан — ок
                continue;
            }

            validateAttributeValues(def, safeValues);
        }
    }

    /**
     * Валидирует значения одного атрибута по его типу и validation_rules.
     */
    private void validateAttributeValues(AttributeDefinition def, Map<String, Object> values) {
        for (String storageKey : def.getStorageKeys().values()) {
            Object value = values.get(storageKey);
            validateSingleValue(def, storageKey, value);
        }

        // Для RANGE_CONTAINS дополнительно проверяем, что min <= max
        if (def.getMatchStrategy() == MatchStrategy.RANGE_CONTAINS) {
            String minKey = def.getStorageKeys().get("min");
            String maxKey = def.getStorageKeys().get("max");
            if (minKey != null && maxKey != null) {
                Number minVal = (Number) values.get(minKey);
                Number maxVal = (Number) values.get(maxKey);
                if (minVal != null && maxVal != null && minVal.doubleValue() > maxVal.doubleValue()) {
                    throw new BadRequestException(
                            "Атрибут '" + def.getKey() + "': минимальное значение не может превышать максимальное");
                }
            }
        }
    }

    /**
     * Валидирует одно значение по типу атрибута и его validation_rules.
     */
    private void validateSingleValue(AttributeDefinition def, String storageKey, Object value) {
        if (value == null) {
            throw new BadRequestException(
                    "Значение атрибута '" + storageKey + "' не может быть null");
        }

        AttributeType type = def.getType();
        Map<String, Object> rules = def.getValidationRules();

        switch (type) {
            case INTEGER -> {
                if (!(value instanceof Number)) {
                    throw new BadRequestException(
                            "Атрибут '" + storageKey + "' должен быть числом");
                }
                long intValue = ((Number) value).longValue();
                if (rules != null) {
                    if (rules.get("min") instanceof Number min && intValue < min.longValue()) {
                        throw new BadRequestException(
                                "Атрибут '" + storageKey + "' не должен быть меньше " + min);
                    }
                    if (rules.get("max") instanceof Number max && intValue > max.longValue()) {
                        throw new BadRequestException(
                                "Атрибут '" + storageKey + "' не должен превышать " + max);
                    }
                }
            }
            case STRING -> {
                if (!(value instanceof String strValue)) {
                    throw new BadRequestException(
                            "Атрибут '" + storageKey + "' должен быть строкой");
                }
                if (rules != null) {
                    if (rules.get("maxLength") instanceof Number maxLen
                            && strValue.length() > maxLen.intValue()) {
                        throw new BadRequestException(
                                "Атрибут '" + storageKey + "' превышает максимальную длину " + maxLen);
                    }
                    if (rules.get("options") instanceof Collection<?> options
                            && !options.contains(strValue)) {
                        throw new BadRequestException(
                                "Атрибут '" + storageKey + "' имеет недопустимое значение. " +
                                        "Ожидается одно из: " + options);
                    }
                }
            }
            case BOOLEAN -> {
                if (!(value instanceof Boolean)) {
                    throw new BadRequestException(
                            "Атрибут '" + storageKey + "' должен быть булевым значением");
                }
            }
            case STRING_ARRAY -> {
                if (!(value instanceof Collection<?> arrValue)) {
                    throw new BadRequestException(
                            "Атрибут '" + storageKey + "' должен быть массивом строк");
                }
                for (Object item : arrValue) {
                    if (!(item instanceof String)) {
                        throw new BadRequestException(
                                "Элементы массива атрибута '" + storageKey + "' должны быть строками");
                    }
                }
                if (rules != null && rules.get("options") instanceof Collection<?> options) {
                    for (Object item : arrValue) {
                        if (!options.contains(item)) {
                            throw new BadRequestException(
                                    "Атрибут '" + storageKey + "' содержит недопустимое значение '" +
                                            item + "'. Ожидаются значения из: " + options);
                        }
                    }
                }
            }
        }
    }

    /**
     * Проверяет, удовлетворяет ли вариант услуги клиентскому фильтру.
     * Применяется в сервисном слое при поиске вариантов.
     * <p>
     * Ключи в clientFilters — это логические ключи атрибутов (из схемы).
     * Значения соответствуют match_strategy каждого атрибута.
     *
     * @param variantAttributes значения атрибутов варианта (из JSONB)
     * @param clientFilters     фильтры клиента (чекбоксы)
     * @param schema            полная схема атрибутов категории
     * @return true, если вариант удовлетворяет всем клиентским фильтрам
     */
    public boolean matchesFilters(Map<String, Object> variantAttributes,
                                  Map<String, Object> clientFilters,
                                  List<CategoryAttribute> schema) {
        if (clientFilters == null || clientFilters.isEmpty()) {
            return true;
        }

        Map<String, Object> safeVariantAttrs = variantAttributes == null ? Map.of() : variantAttributes;

        // Индексируем схему по логическому ключу для быстрого доступа
        Map<String, AttributeDefinition> defsByKey = new HashMap<>();
        for (CategoryAttribute binding : schema) {
            defsByKey.put(binding.getAttribute().getKey(), binding.getAttribute());
        }

        for (Map.Entry<String, Object> filter : clientFilters.entrySet()) {
            String logicalKey = filter.getKey();
            Object clientValue = filter.getValue();

            AttributeDefinition def = defsByKey.get(logicalKey);
            if (def == null) {
                // Клиент отправил фильтр по атрибуту, которого нет в схеме — игнорируем
                log.debug("Клиент прислал неизвестный фильтр '{}' — игнорируем", logicalKey);
                continue;
            }

            if (clientValue == null) {
                continue;
            }

            if (!matchesSingleFilter(def, clientValue, safeVariantAttrs)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Проверяет соответствие одного фильтра атрибутам варианта по match_strategy атрибута.
     */
    private boolean matchesSingleFilter(AttributeDefinition def,
                                        Object clientValue,
                                        Map<String, Object> variantAttrs) {
        Map<String, String> storageKeys = def.getStorageKeys();

        return switch (def.getMatchStrategy()) {
            case SINGLE_EQ -> {
                Object variantValue = variantAttrs.get(storageKeys.get("value"));
                yield variantValue != null && variantValue.equals(clientValue);
            }
            case SINGLE_GTE -> {
                Object variantValue = variantAttrs.get(storageKeys.get("value"));
                if (!(variantValue instanceof Number vn) || !(clientValue instanceof Number cn)) {
                    yield false;
                }
                yield vn.doubleValue() >= cn.doubleValue();
            }
            case SINGLE_LTE -> {
                Object variantValue = variantAttrs.get(storageKeys.get("value"));
                if (!(variantValue instanceof Number vn) || !(clientValue instanceof Number cn)) {
                    yield false;
                }
                yield vn.doubleValue() <= cn.doubleValue();
            }
            case RANGE_CONTAINS -> {
                Object minV = variantAttrs.get(storageKeys.get("min"));
                Object maxV = variantAttrs.get(storageKeys.get("max"));
                if (!(minV instanceof Number mn) || !(maxV instanceof Number mx)
                        || !(clientValue instanceof Number cn)) {
                    yield false;
                }
                double c = cn.doubleValue();
                yield c >= mn.doubleValue() && c <= mx.doubleValue();
            }
            case BOOLEAN_MATCH -> {
                Object variantValue = variantAttrs.get(storageKeys.get("value"));
                yield variantValue instanceof Boolean && variantValue.equals(clientValue);
            }
            case ARRAY_CONTAINS -> {
                Object variantValue = variantAttrs.get(storageKeys.get("value"));
                if (!(variantValue instanceof Collection<?> arr)) {
                    yield false;
                }
                yield arr.contains(clientValue);
            }
            case ARRAY_INTERSECTS -> {
                Object variantValue = variantAttrs.get(storageKeys.get("value"));
                if (!(variantValue instanceof Collection<?> arr)) {
                    yield false;
                }
                Collection<?> clientArr = clientValue instanceof Collection<?> c
                        ? c
                        : List.of(clientValue);
                for (Object item : clientArr) {
                    if (arr.contains(item)) {
                        yield true;
                    }
                }
                yield false;
            }
        };
    }

    /**
     * Маппит привязку атрибута к категории в DTO схемы для фронта.
     */
    private AttributeSchemaItemResponse mapToSchemaItem(CategoryAttribute binding) {
        AttributeDefinition def = binding.getAttribute();
        return AttributeSchemaItemResponse.builder()
                .attributeId(def.getId())
                .key(def.getKey())
                .type(def.getType().name())
                .matchStrategy(def.getMatchStrategy().name())
                .storageKeys(def.getStorageKeys())
                .labelRu(def.getLabelRu())
                .labelKk(def.getLabelKk())
                .unit(def.getUnit())
                .validationRules(def.getValidationRules())
                .isRequired(binding.getIsRequired())
                .isFilterable(binding.getIsFilterable())
                .sortOrder(binding.getSortOrder())
                .build();
    }
}