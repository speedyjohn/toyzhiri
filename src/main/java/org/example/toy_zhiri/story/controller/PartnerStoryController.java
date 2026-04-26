package org.example.toy_zhiri.story.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.story.dto.CreateStoryRequest;
import org.example.toy_zhiri.story.dto.StoryAnalyticsResponse;
import org.example.toy_zhiri.story.dto.StoryResponse;
import org.example.toy_zhiri.story.service.StoryService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для управления сторис партнёром.
 * Доступен только пользователям с ролью PARTNER.
 */
@RestController
@RequestMapping("/api/v1/partner/stories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
@Tag(name = "Partner Stories", description = "Управление сторис партнёра")
public class PartnerStoryController {
    private final StoryService storyService;
    private final UserService userService;

    /**
     * Создаёт новую сторис с моковой оплатой.
     * После успешной оплаты сторис активируется на 24 часа.
     *
     * @param request     данные для создания сторис
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<StoryResponse> созданная сторис
     */
    @PostMapping
    @Operation(
            summary = "Опубликовать сторис",
            description = "Создаёт сторис с моковой оплатой. После успешной оплаты " +
                    "сторис активируется на 24 часа.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<StoryResponse> createStory(
            @Valid @RequestBody CreateStoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(201).body(storyService.createStory(userId, request));
    }

    /**
     * Возвращает все сторис текущего партнёра.
     *
     * @param page        номер страницы
     * @param size        размер страницы
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<Page<StoryResponse>> страница со сторис партнёра
     */
    @GetMapping
    @Operation(
            summary = "Мои сторис",
            description = "Список всех сторис партнёра (активные, истёкшие, удалённые) " +
                    "с пагинацией.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<StoryResponse>> getMyStories(
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(storyService.getMyStories(userId, pageable));
    }

    /**
     * Возвращает аналитику по конкретной сторис партнёра.
     *
     * @param storyId     идентификатор сторис
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<StoryAnalyticsResponse> аналитика по сторис
     */
    @GetMapping("/{storyId}/analytics")
    @Operation(
            summary = "Аналитика сторис",
            description = "Количество уникальных просмотров и базовая информация " +
                    "по сторис текущего партнёра.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<StoryAnalyticsResponse> getStoryAnalytics(
            @Parameter(description = "ID сторис")
            @PathVariable UUID storyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(storyService.getStoryAnalytics(userId, storyId));
    }

    /**
     * Досрочно удаляет активную сторис партнёра.
     *
     * @param storyId     идентификатор сторис
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<MessageResponse> подтверждение удаления
     */
    @DeleteMapping("/{storyId}")
    @Operation(
            summary = "Удалить мою сторис",
            description = "Досрочно удалить активную сторис. Возврат средств не предусмотрен.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> deleteMyStory(
            @Parameter(description = "ID сторис")
            @PathVariable UUID storyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(storyService.deleteMyStory(userId, storyId));
    }
}