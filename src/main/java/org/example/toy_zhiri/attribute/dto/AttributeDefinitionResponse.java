package org.example.toy_zhiri.attribute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO определения атрибута для административного API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDefinitionResponse {

    private UUID id;
    private String key;
    private String type;
    private String matchStrategy;
    private Map<String, String> storageKeys;
    private String labelRu;
    private String labelKk;
    private String unit;
    private Map<String, Object> validationRules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}