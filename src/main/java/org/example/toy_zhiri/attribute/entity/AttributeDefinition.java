package org.example.toy_zhiri.attribute.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.attribute.enums.AttributeType;
import org.example.toy_zhiri.attribute.enums.MatchStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Справочник определений атрибутов вариантов услуг.
 * Каждая запись описывает один атрибут: его ключ, тип, стратегию сравнения
 * при фильтрации и правила валидации значений.
 */
@Entity
@Table(name = "attribute_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Логический уникальный ключ атрибута (например, "capacity", "car_model").
     * Используется как идентификатор атрибута во всех API-запросах.
     */
    @Column(name = "key", length = 100, nullable = false, unique = true)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private AttributeType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_strategy", length = 50, nullable = false)
    private MatchStrategy matchStrategy;

    /**
     * Ключи, под которыми значение хранится в JSONB-колонке attributes варианта услуги.
     * Для одиночных значений: {"value": "car_year"}.
     * Для диапазонов: {"min": "capacity_min", "max": "capacity_max"}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "storage_keys", columnDefinition = "jsonb", nullable = false)
    private Map<String, String> storageKeys;

    @Column(name = "label_ru", length = 255, nullable = false)
    private String labelRu;

    @Column(name = "label_kk", length = 255, nullable = false)
    private String labelKk;

    @Column(name = "unit", length = 50)
    private String unit;

    /**
     * Правила валидации значений.
     * Поддерживаемые ключи: min, max, options, maxLength, pattern.
     * Пример: {"min": 1, "max": 10000} или {"options": ["WHITE","BLACK","RED"]}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}