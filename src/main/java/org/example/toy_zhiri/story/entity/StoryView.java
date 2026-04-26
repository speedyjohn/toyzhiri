package org.example.toy_zhiri.story.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Запись о просмотре сторис конкретным пользователем.
 * Уникальность пары (story_id, user_id) обеспечивается constraint в БД,
 * что гарантирует учёт уникальных просмотров.
 */
@Entity
@Table(name = "story_views")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryView {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt;
}