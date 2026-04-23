package org.example.toy_zhiri.attribute.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.service.entity.ServiceCategory;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Привязка атрибута к категории услуг.
 * Определяет, какие атрибуты доступны для вариантов услуг данной категории,
 * являются ли они обязательными при создании варианта,
 * и должны ли показываться клиенту как фильтры/чекбоксы.
 */
@Entity
@Table(name = "category_attributes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_category_attribute",
                columnNames = {"category_id", "attribute_id"}
        )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeDefinition attribute;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    /**
     * Флаг видимости для клиентов.
     * Если true — атрибут показывается как чекбокс в форме бронирования
     * и (в будущем) как фильтр в каталоге.
     */
    @Column(name = "is_filterable", nullable = false)
    private Boolean isFilterable = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}