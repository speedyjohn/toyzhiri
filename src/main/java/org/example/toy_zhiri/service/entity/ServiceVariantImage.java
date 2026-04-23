package org.example.toy_zhiri.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Изображение варианта услуги.
 * Аналогично ServiceImage, но привязано к конкретному варианту,
 * а не к услуге в целом.
 */
@Entity
@Table(name = "service_variant_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceVariantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ServiceVariant variant;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}