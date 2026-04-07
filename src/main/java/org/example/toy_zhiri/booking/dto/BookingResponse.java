package org.example.toy_zhiri.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private UUID id;

    // Клиент
    private UUID userId;
    private String userFullName;
    private String userPhone;
    private String userEmail;

    // Услуга
    private UUID serviceId;
    private String serviceName;
    private String serviceCategory;
    private String serviceThumbnail;

    // Партнёр
    private UUID partnerId;
    private String partnerCompanyName;
    private String partnerPhone;

    // Детали бронирования
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String status;
    private String notes;
    private Integer guestsCount;
    private BigDecimal totalPrice;
    private String rejectionReason;
    private Map<String, Object> extraParams;

    // Статус двойного подтверждения завершения сделки
    private Boolean clientConfirmed;
    private Boolean partnerConfirmed;

    /**
     * Ссылка на страницу услуги. Формат: /services/{slug}
     */
    private String serviceUrl;

    private String chatUrl;

    // Временные метки статусов
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime clientConfirmedAt;
    private LocalDateTime partnerConfirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}