package org.example.toy_zhiri.attribute.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.attribute.dto.AttributeDefinitionResponse;
import org.example.toy_zhiri.attribute.dto.CreateAttributeDefinitionRequest;
import org.example.toy_zhiri.attribute.dto.UpdateAttributeDefinitionRequest;
import org.example.toy_zhiri.attribute.entity.AttributeDefinition;
import org.example.toy_zhiri.attribute.enums.AttributeType;
import org.example.toy_zhiri.attribute.enums.MatchStrategy;
import org.example.toy_zhiri.attribute.repository.AttributeDefinitionRepository;
import org.example.toy_zhiri.exception.BadRequestException;
import org.example.toy_zhiri.exception.ConflictException;
import org.example.toy_zhiri.exception.InvalidStateException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис административного управления определениями атрибутов.
 * Предоставляет полный CRUD с защитой от некорректных изменений,
 * способных повредить существующие варианты услуг.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeDefinitionService {

    private final AttributeDefinitionRepository repository;

    /**
     * Возвращает список определений атрибутов с пагинацией и поиском.
     */
    public Page<AttributeDefinitionResponse> list(String search, Pageable pageable) {
        return repository.search(search, pageable).map(this::mapToResponse);
    }

    /**
     * Возвращает определение атрибута по идентификатору.
     */
    public AttributeDefinitionResponse getById(UUID id) {
        return mapToResponse(findByIdOrThrow(id));
    }

    /**
     * Создаёт новое определение атрибута.
     */
    @Transactional
    public AttributeDefinitionResponse create(CreateAttributeDefinitionRequest request) {
        if (repository.existsByKey(request.getKey())) {
            throw new ConflictException("Атрибут с ключом '" + request.getKey() + "' уже существует");
        }

        AttributeType type = AttributeType.valueOf(request.getType());
        MatchStrategy strategy = MatchStrategy.valueOf(request.getMatchStrategy());

        validateTypeAndStrategyCompatibility(type, strategy);
        validateStorageKeys(strategy, request.getStorageKeys());

        AttributeDefinition definition = AttributeDefinition.builder()
                .key(request.getKey())
                .type(type)
                .matchStrategy(strategy)
                .storageKeys(request.getStorageKeys())
                .labelRu(request.getLabelRu())
                .labelKk(request.getLabelKk())
                .unit(request.getUnit())
                .validationRules(request.getValidationRules())
                .build();

        AttributeDefinition saved = repository.save(definition);
        log.info("Создано определение атрибута: key={}, id={}", saved.getKey(), saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Обновляет "мягкие" поля определения атрибута.
     * Поля key, type, matchStrategy, storageKeys неизменяемы после создания
     * и меняются только через пересоздание.
     */
    @Transactional
    public AttributeDefinitionResponse update(UUID id, UpdateAttributeDefinitionRequest request) {
        AttributeDefinition def = findByIdOrThrow(id);

        def.setLabelRu(request.getLabelRu());
        def.setLabelKk(request.getLabelKk());
        def.setUnit(request.getUnit());
        def.setValidationRules(request.getValidationRules());

        AttributeDefinition saved = repository.save(def);
        log.info("Обновлено определение атрибута: key={}", saved.getKey());
        return mapToResponse(saved);
    }

    /**
     * Удаляет определение атрибута.
     * Запрещено, если атрибут привязан хотя бы к одной категории
     * или используется хотя бы в одном варианте услуги.
     */
    @Transactional
    public void delete(UUID id) {
        AttributeDefinition def = findByIdOrThrow(id);

        if (repository.isUsedInCategoryBindings(id)) {
            throw new InvalidStateException(
                    "Нельзя удалить атрибут '" + def.getKey() +
                            "': он привязан к одной или нескольким категориям. " +
                            "Сначала отвяжите его от всех категорий");
        }

        for (String storageKey : def.getStorageKeys().values()) {
            if (repository.isUsedInVariants(storageKey)) {
                throw new InvalidStateException(
                        "Нельзя удалить атрибут '" + def.getKey() +
                                "': значения с ключом '" + storageKey +
                                "' уже используются в вариантах услуг");
            }
        }

        repository.delete(def);
        log.info("Удалено определение атрибута: key={}", def.getKey());
    }

    /**
     * Находит атрибут по идентификатору или выбрасывает NotFoundException.
     */
    public AttributeDefinition findByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Определение атрибута с ID " + id + " не найдено"));
    }

    /**
     * Проверяет совместимость типа атрибута и его стратегии сравнения.
     */
    private void validateTypeAndStrategyCompatibility(AttributeType type, MatchStrategy strategy) {
        Set<MatchStrategy> allowed = switch (type) {
            case INTEGER -> Set.of(
                    MatchStrategy.SINGLE_EQ,
                    MatchStrategy.SINGLE_GTE,
                    MatchStrategy.SINGLE_LTE,
                    MatchStrategy.RANGE_CONTAINS
            );
            case STRING -> Set.of(MatchStrategy.SINGLE_EQ);
            case BOOLEAN -> Set.of(MatchStrategy.BOOLEAN_MATCH);
            case STRING_ARRAY -> Set.of(
                    MatchStrategy.ARRAY_CONTAINS,
                    MatchStrategy.ARRAY_INTERSECTS
            );
        };

        if (!allowed.contains(strategy)) {
            throw new BadRequestException(
                    "Стратегия " + strategy + " несовместима с типом " + type +
                            ". Допустимые стратегии: " + allowed);
        }
    }

    /**
     * Проверяет корректность storageKeys в зависимости от стратегии.
     */
    private void validateStorageKeys(MatchStrategy strategy, Map<String, String> storageKeys) {
        if (storageKeys == null || storageKeys.isEmpty()) {
            throw new BadRequestException("Ключи хранения обязательны");
        }

        if (strategy == MatchStrategy.RANGE_CONTAINS) {
            if (!storageKeys.containsKey("min") || !storageKeys.containsKey("max")) {
                throw new BadRequestException(
                        "Для стратегии RANGE_CONTAINS обязательны ключи 'min' и 'max'");
            }
        } else {
            if (!storageKeys.containsKey("value")) {
                throw new BadRequestException(
                        "Для стратегии " + strategy + " обязателен ключ 'value'");
            }
        }
    }

    private AttributeDefinitionResponse mapToResponse(AttributeDefinition def) {
        return AttributeDefinitionResponse.builder()
                .id(def.getId())
                .key(def.getKey())
                .type(def.getType().name())
                .matchStrategy(def.getMatchStrategy().name())
                .storageKeys(def.getStorageKeys())
                .labelRu(def.getLabelRu())
                .labelKk(def.getLabelKk())
                .unit(def.getUnit())
                .validationRules(def.getValidationRules())
                .createdAt(def.getCreatedAt())
                .updatedAt(def.getUpdatedAt())
                .build();
    }
}