package org.example.toy_zhiri.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO ответа со сторис.
 * Используется для всех представлений: публичный фид, ответ партнёру, админ-панель.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponse {
    private UUID id;
    private UUID serviceId;
    private String serviceName;
    private UUID partnerId;
    private String partnerCompanyName;
    private UUID categoryId;
    private String categoryName;
    private String mediaUrl;
    private String mediaType;
    private String caption;
    private String status;
    private BigDecimal paidAmount;
    private String paymentMethod;
    private String paymentId;
    private Integer viewsCount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}