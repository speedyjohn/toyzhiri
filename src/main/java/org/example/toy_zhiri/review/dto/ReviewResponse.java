package org.example.toy_zhiri.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID bookingId;

    // Автор
    private UUID userId;
    private String userFullName;

    // Услуга / партнёр
    private UUID serviceId;
    private String serviceName;
    private UUID partnerId;
    private String partnerCompanyName;

    // Контент
    private Short rating;
    private String comment;
    private List<String> imageUrls;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}