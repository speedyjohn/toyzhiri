package org.example.toy_zhiri.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO ответа по варианту услуги.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceVariantResponse {

    private UUID id;
    private UUID serviceId;
    private String name;
    private String description;
    private BigDecimal price;
    private Map<String, Object> attributes;
    private List<String> imageUrls;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}