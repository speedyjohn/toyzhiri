package org.example.toy_zhiri.attribute.enums;

/**
 * Тип значения атрибута.
 * Определяет, как значение хранится в JSONB и как валидируется.
 */
public enum AttributeType {

    /**
     * Целое число. Хранится как number в JSONB.
     */
    INTEGER,

    /**
     * Строка. Хранится как string в JSONB.
     */
    STRING,

    /**
     * Булево значение. Хранится как boolean в JSONB.
     */
    BOOLEAN,

    /**
     * Массив строк. Хранится как array of strings в JSONB.
     */
    STRING_ARRAY
}