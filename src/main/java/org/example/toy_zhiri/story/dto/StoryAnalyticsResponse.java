package org.example.toy_zhiri.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Аналитика по сторис партнёра.
 * Содержит количество уникальных просмотров и базовую информацию о сторис.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryAnalyticsResponse {
    private UUID storyId;
    private Integer viewsCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}