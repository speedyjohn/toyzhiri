package org.example.toy_zhiri.story.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.story.dto.StoryResponse;
import org.example.toy_zhiri.story.enums.StoryStatus;
import org.example.toy_zhiri.story.service.StoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для модерации сторис администратором.
 * Доступен только пользователям с ролью ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/stories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Stories", description = "Модерация сторис администратором")
public class AdminStoryController {
    private final StoryService storyService;

    /**
     * Возвращает все сторис системы с опциональным фильтром по статусу.
     *
     * @param status опциональный фильтр по статусу
     * @param page   номер страницы
     * @param size   размер страницы
     * @return ResponseEntity<Page<StoryResponse>> страница со сторис
     */
    @GetMapping
    @Operation(
            summary = "Список всех сторис",
            description = "Получить список всех сторис в системе с опциональным " +
                    "фильтром по статусу (ACTIVE, EXPIRED, REMOVED_BY_PARTNER, REMOVED_BY_ADMIN).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<StoryResponse>> getAllStories(
            @Parameter(description = "Фильтр по статусу")
            @RequestParam(required = false) StoryStatus status,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(storyService.getAllStories(status, pageable));
    }

    /**
     * Снимает сторис с публикации.
     *
     * @param storyId идентификатор сторис
     * @return ResponseEntity<MessageResponse> подтверждение
     */
    @DeleteMapping("/{storyId}")
    @Operation(
            summary = "Снять сторис",
            description = "Снимает сторис с публикации (status = REMOVED_BY_ADMIN). " +
                    "Партнёр получает уведомление.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> removeStory(
            @Parameter(description = "ID сторис")
            @PathVariable UUID storyId) {

        return ResponseEntity.ok(storyService.removeStoryByAdmin(storyId));
    }
}