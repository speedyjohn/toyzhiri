package org.example.toy_zhiri.story.repository;

import org.example.toy_zhiri.story.entity.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, UUID> {

    /**
     * Проверяет, просматривал ли пользователь уже эту сторис.
     */
    boolean existsByStoryIdAndUserId(UUID storyId, UUID userId);
}