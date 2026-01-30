package org.example.toy_zhiri.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO для детального профиля партнера в справочнике.
 * Содержит всю информацию о партнере, включая вычисляемые поля.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerProfileResponse {
    // Основные данные
    private UUID id;
    private UUID userId;
    private String bin;
    private String companyName;
    private String description;

    // Адрес и локация
    private String address;
    private String city;
    private String region;

    // Контактные данные
    private String phone;
    private String email;
    private String whatsapp;
    private String telegram;
    private String instagram;
    private String website;

    // Медиа
    private String logoUrl;

    // Статус
    private String status;
    private String rejectionReason;

    // Даты
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedByEmail;

    // Вычисляемые поля (из услуг партнера)
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer totalServices;
    private Integer activeServices;
    private List<String> businessCategories; // Типы бизнеса (категории услуг)

    // ФИО из User
    private String ownerFullName;
    private String ownerEmail;
    private String ownerPhone;
}