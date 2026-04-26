package org.example.toy_zhiri.story.enums;

/**
 * Статусы сторис.
 * <p>
 * Жизненный цикл:
 * ACTIVE → EXPIRED (через 24 часа после публикации, scheduled job)
 * ACTIVE → REMOVED_BY_PARTNER (партнёр удалил досрочно)
 * ACTIVE → REMOVED_BY_ADMIN (администратор снял сторис)
 */
public enum StoryStatus {
    ACTIVE,             // Сторис активна и видна пользователям
    EXPIRED,            // 24 часа истекли — скрыта от показа
    REMOVED_BY_PARTNER, // Партнёр удалил досрочно
    REMOVED_BY_ADMIN    // Снята администратором
}