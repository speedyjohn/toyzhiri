package org.example.toy_zhiri.story.job;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.story.entity.Story;
import org.example.toy_zhiri.story.enums.StoryStatus;
import org.example.toy_zhiri.story.repository.StoryRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Плановое задание для обработки истёкших сторис.
 * <p>
 * Каждые 5 минут проверяет сторис со статусом ACTIVE,
 * у которых истёк срок жизни (expires_at < now).
 * Такие сторис переводятся в статус EXPIRED и перестают
 * показываться в публичной ленте.
 * <p>
 * Запускается при старте сервера и затем каждые 5 минут.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoryExpirationJob {

    private final StoryRepository storyRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void expireOverdueStories() {
        List<Story> overdueStories = storyRepository.findByStatusAndExpiresAtBefore(
                StoryStatus.ACTIVE,
                LocalDateTime.now()
        );

        if (overdueStories.isEmpty()) {
            return;
        }

        log.info("StoryExpirationJob: найдено {} истёкших сторис", overdueStories.size());

        for (Story story : overdueStories) {
            story.setStatus(StoryStatus.EXPIRED);
            storyRepository.save(story);

            log.info("Сторис {} переведена в EXPIRED (партнёр: {}, услуга: {})",
                    story.getId(),
                    story.getPartner().getCompanyName(),
                    story.getService().getName());
        }

        log.info("StoryExpirationJob: обработано {} сторис", overdueStories.size());
    }
}