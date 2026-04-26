package org.example.toy_zhiri.story.repository;

import org.example.toy_zhiri.story.entity.Story;
import org.example.toy_zhiri.story.enums.StoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StoryRepository extends JpaRepository<Story, UUID> {

    /**
     * Случайные активные сторис без фильтра по категории — для главной страницы.
     */
    @Query(value = "SELECT * FROM stories " +
            "WHERE status = 'ACTIVE' AND expires_at > NOW() " +
            "ORDER BY random() " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Story> findRandomActive(@Param("limit") int limit);

    /**
     * Случайные активные сторис в рамках конкретной категории — для поиска по категории.
     */
    @Query(value = "SELECT * FROM stories " +
            "WHERE status = 'ACTIVE' AND expires_at > NOW() " +
            "AND category_id = :categoryId " +
            "ORDER BY random() " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Story> findRandomActiveByCategory(
            @Param("categoryId") UUID categoryId,
            @Param("limit") int limit
    );

    /**
     * Сторис партнёра — для личного кабинета, любого статуса, с пагинацией.
     */
    Page<Story> findByPartnerIdOrderByCreatedAtDesc(UUID partnerId, Pageable pageable);

    /**
     * Истекшие активные сторис — для scheduled job.
     */
    List<Story> findByStatusAndExpiresAtBefore(StoryStatus status, LocalDateTime now);

    /**
     * Атомарный инкремент счётчика просмотров.
     * Используется только после успешной вставки уникального StoryView.
     */
    @Modifying
    @Query("UPDATE Story s SET s.viewsCount = s.viewsCount + 1 WHERE s.id = :storyId")
    void incrementViewsCount(@Param("storyId") UUID storyId);

    /**
     * Все сторис с фильтром по статусу — для админ-панели.
     */
    Page<Story> findByStatusOrderByCreatedAtDesc(StoryStatus status, Pageable pageable);

    /**
     * Все сторис без фильтра — для админ-панели.
     */
    Page<Story> findAllByOrderByCreatedAtDesc(Pageable pageable);
}