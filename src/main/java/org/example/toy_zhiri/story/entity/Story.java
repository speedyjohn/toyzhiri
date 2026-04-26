package org.example.toy_zhiri.story.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.payment.enums.PaymentMethod;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.entity.ServiceCategory;
import org.example.toy_zhiri.story.enums.StoryMediaType;
import org.example.toy_zhiri.story.enums.StoryStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность сторис партнёра.
 * Платная публикация, привязанная к конкретной услуге.
 * Категория денормализована для эффективной фильтрации без JOIN.
 */
@Entity
@Table(name = "stories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private StoryMediaType mediaType;

    @Column(length = 200)
    private String caption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StoryStatus status = StoryStatus.ACTIVE;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_id", length = 255)
    private String paymentId;

    @Column(name = "views_count", nullable = false)
    private Integer viewsCount = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}