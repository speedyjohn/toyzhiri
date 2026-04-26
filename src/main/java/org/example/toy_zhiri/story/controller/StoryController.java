package org.example.toy_zhiri.story.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.story.dto.StoryResponse;
import org.example.toy_zhiri.story.service.StoryService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Публичный контроллер для просмотра сторис.
 * Доступен любым пользователям, регистрация просмотра требует авторизации.
 */
@RestController
@RequestMapping("/api/v1/stories")
@RequiredArgsConstructor
@Tag(name = "Stories", description = "Публичный API для просмотра сторис")
public class StoryController {
    private final StoryService storyService;
    private final UserService userService;

    /**
     * Возвращает ленту активных сторис.
     * Если передана категория — выборка ограничивается только этой категорией.
     * Без категории возвращается рандомная подборка из всех активных сторис.
     *
     * @param categoryId опциональный фильтр по категории услуги
     * @return ResponseEntity<List<StoryResponse>> рандомная подборка активных сторис
     */
    @GetMapping("/feed")
    @Operation(
            summary = "Лента сторис",
            description = "Рандомная подборка активных сторис. " +
                    "Если передан categoryId — выборка только в рамках этой категории."
    )
    public ResponseEntity<List<StoryResponse>> getFeed(
            @Parameter(description = "ID категории услуги (опционально)")
            @RequestParam(required = false) UUID categoryId) {

        return ResponseEntity.ok(storyService.getFeed(categoryId));
    }

    /**
     * Возвращает детали одной сторис.
     *
     * @param storyId идентификатор сторис
     * @return ResponseEntity<StoryResponse> данные сторис
     */
    @GetMapping("/{storyId}")
    @Operation(
            summary = "Детали сторис",
            description = "Получить полные данные одной сторис по её идентификатору."
    )
    public ResponseEntity<StoryResponse> getStoryById(
            @Parameter(description = "ID сторис")
            @PathVariable UUID storyId) {

        return ResponseEntity.ok(storyService.getStoryById(storyId));
    }

    /**
     * Регистрирует уникальный просмотр сторис текущим пользователем.
     * Повторные вызовы от того же пользователя игнорируются.
     *
     * @param storyId     идентификатор сторис
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<MessageResponse> подтверждение
     */
    @PostMapping("/{storyId}/view")
    @Operation(
            summary = "Зарегистрировать просмотр",
            description = "Учитывает уникальный просмотр сторис. " +
                    "Повторные вызовы от того же пользователя не увеличивают счётчик.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> registerView(
            @Parameter(description = "ID сторис")
            @PathVariable UUID storyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        storyService.registerView(userId, storyId);

        return ResponseEntity.ok(MessageResponse.builder()
                .message("Просмотр зарегистрирован")
                .build());
    }
}