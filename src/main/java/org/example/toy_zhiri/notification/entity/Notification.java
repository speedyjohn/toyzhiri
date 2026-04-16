package org.example.toy_zhiri.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.enums.RelatedEntityType;
import org.example.toy_zhiri.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * Тип связанной сущности (BOOKING, SERVICE, REVIEW и т.д.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "related_entity_type", length = 50)
    private RelatedEntityType relatedEntityType;

    /**
     * ID связанной сущности для формирования ссылок на фронтенде
     */
    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}