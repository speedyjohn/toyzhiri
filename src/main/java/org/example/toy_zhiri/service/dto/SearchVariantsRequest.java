package org.example.toy_zhiri.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO запроса на поиск подходящих вариантов услуги.
 * Используется в форме бронирования для подбора вариантов по чекбоксам клиента.
 * <p>
 * Ключи в filters — это логические ключи атрибутов (те же, что в схеме).
 * Значения зависят от типа атрибута и его match_strategy:
 * - для RANGE_CONTAINS передаётся одно число
 * - для SINGLE_EQ/SINGLE_GTE/SINGLE_LTE — одно значение
 * - для BOOLEAN_MATCH — true/false
 * - для ARRAY_INTERSECTS/ARRAY_CONTAINS — массив строк
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchVariantsRequest {

    /**
     * Фильтры клиента, отмеченные чекбоксами.
     * null или пустой объект — вернуть все активные варианты услуги.
     */
    private Map<String, Object> filters;
}