package org.example.toy_zhiri.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.chat.dto.ChatMessageResponse;
import org.example.toy_zhiri.chat.dto.ChatResponse;
import org.example.toy_zhiri.chat.dto.CreateChatRequest;
import org.example.toy_zhiri.chat.dto.SendMessageRequest;
import org.example.toy_zhiri.chat.service.ChatService;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для работы с чатами между клиентами и партнёрами.
 * Один диалог на пару (клиент, партнёр).
 */
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Tag(name = "Chats", description = "API для работы с чатами между клиентами и партнёрами")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    /**
     * Создаёт новый чат с партнёром или возвращает существующий.
     * Клиент может написать партнёру в любой момент, в том числе до бронирования.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param request запрос с ID партнёра
     * @return ResponseEntity<ChatResponse> DTO диалога
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'PARTNER', 'ADMIN')")
    @Operation(
            summary = "Создать или получить чат с партнёром",
            description = "Возвращает существующий диалог между текущим пользователем и партнёром, " +
                    "либо создаёт новый, если его ещё нет",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ChatResponse> createOrGetChat(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateChatRequest request)
    {
        User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
        ChatResponse response = chatService.getOrCreateChat(user.getId(), request.getPartnerId());

        return ResponseEntity
                .created(URI.create("/api/v1/chats/" + response.getId()))
                .body(response);
    }

    /**
     * Возвращает чат по ID.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param chatId идентификатор чата
     * @return ResponseEntity<ChatResponse> DTO диалога
     */
    @GetMapping("/{chatId}")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER', 'ADMIN')")
    @Operation(
            summary = "Получить чат по ID",
            description = "Доступен только участникам диалога",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ChatResponse> getChat(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID chatId)
    {
        User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
        ChatResponse response = chatService.getChatById(chatId, user.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Возвращает все диалоги текущего пользователя как клиента.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param pageable параметры пагинации
     * @return ResponseEntity<Page<ChatResponse>> страница с диалогами
     */
    @GetMapping("/my/client")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER', 'ADMIN')")
    @Operation(
            summary = "Мои чаты как клиента",
            description = "Список всех диалогов, в которых текущий пользователь выступает в роли клиента",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<ChatResponse>> getMyChatsAsClient(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable)
    {
        User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
        Page<ChatResponse> response = chatService.getMyChatsAsClient(user.getId(), pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Возвращает все диалоги текущего пользователя как партнёра.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param pageable параметры пагинации
     * @return ResponseEntity<Page<ChatResponse>> страница с диалогами
     */
    @GetMapping("/my/partner")
    @PreAuthorize("hasRole('PARTNER')")
    @Operation(
            summary = "Мои чаты как партнёра",
            description = "Список всех диалогов с клиентами для текущего партнёра",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<ChatResponse>> getMyChatsAsPartner(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable)
    {
        User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
        Page<ChatResponse> response = chatService.getMyChatsAsPartner(user.getId(), pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Возвращает общее количество непрочитанных сообщений пользователя.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<Map<String, Long>> объект с полем unreadCount
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER', 'ADMIN')")
    @Operation(
            summary = "Количество непрочитанных сообщений",
            description = "Возвращает общее число непрочитанных сообщений по всем чатам — для бейджика в шапке",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails)
    {
        User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
        long count = chatService.getTotalUnreadCount(user.getId());

        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Возвращает историю сообщений чата с пагинацией (новые сверху).
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param chatId идентификатор чата
     * @param pageable параметры пагинации
     * @return ResponseEntity<Page<ChatMessageResponse>> страница с сообщениями
     */
    @GetMapping("/{chatId}/messages")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER', 'ADMIN')")
    @Operation(
            summary = "История сообщений чата",
            description = "Возвращает сообщения с пагинацией, отсортированные от новых к старым",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID chatId,
            @PageableDefault(size = 50) Pageable pageable)
    {
        User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
        Page<ChatMessageResponse> response = chatService.getMessages(chatId, user.getId(), pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Отправляет сообщение в чат.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param chatId идентификатор чата
     * @param request запрос с текстом и/или вложениями
     * @return ResponseEntity<ChatMessageResponse> DTO сохранённого сообщения
     */
    @PostMapping("/{chatId}/messages")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER', 'ADMIN')")
    @Operation(
            summary = "Отправить сообщение",
            description = "Отправляет сообщение в чат от имени текущего пользователя. " +
                    "Сообщение может содержать текст, вложения (URL-ы из /api/v1/files/upload), " +
                    "либо и то, и другое",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID chatId,
            @Valid @RequestBody SendMessageRequest request)
    {
        User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
        ChatMessageResponse response = chatService.sendMessage(
                chatId, user.getId(), request.getContent(), request.getAttachmentUrls());

        return ResponseEntity
                .created(URI.create("/api/v1/chats/" + chatId + "/messages/" + response.getId()))
                .body(response);
    }

    /**
     * Помечает все непрочитанные сообщения чата как прочитанные.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param chatId идентификатор чата
     * @return ResponseEntity<Map<String, Integer>> объект с полем markedAsRead
     */
    @PostMapping("/{chatId}/read")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER', 'ADMIN')")
    @Operation(
            summary = "Отметить чат прочитанным",
            description = "Помечает все непрочитанные сообщения чата как прочитанные для текущего пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, Integer>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID chatId)
    {
        User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
        int updated = chatService.markAsRead(chatId, user.getId());

        return ResponseEntity.ok(Map.of("markedAsRead", updated));
    }
}