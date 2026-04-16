package org.example.toy_zhiri.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.chat.dto.ChatMessageResponse;
import org.example.toy_zhiri.chat.dto.ChatReadEvent;
import org.example.toy_zhiri.chat.dto.ChatResponse;
import org.example.toy_zhiri.chat.entity.Chat;
import org.example.toy_zhiri.chat.entity.ChatMessage;
import org.example.toy_zhiri.chat.repository.ChatMessageRepository;
import org.example.toy_zhiri.chat.repository.ChatRepository;
import org.example.toy_zhiri.exception.AccessDeniedException;
import org.example.toy_zhiri.exception.BadRequestException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.enums.RelatedEntityType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с чатами.
 * Один диалог на пару (клиент, партнёр).
 * <p>
 * Является единой точкой записи в БД — REST-контроллер и WebSocket-контроллер
 * используют одни и те же методы. Публикация в STOMP-топики и отправка
 * уведомлений тоже происходят здесь, чтобы любой канал входа давал одинаковый
 * побочный эффект.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Возвращает существующий диалог между клиентом и партнёром
     * или создаёт новый, если его ещё нет.
     * <p>
     * Используется когда клиент хочет написать партнёру (в т.ч. ДО бронирования).
     *
     * @param clientUserId ID пользователя-клиента
     * @param partnerId    ID партнёра
     * @return DTO диалога
     */
    @Transactional
    public ChatResponse getOrCreateChat(UUID clientUserId, UUID partnerId) {
        User client = userRepository.findById(clientUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        // Запрещаем партнёру создавать чат "с самим собой"
        if (partner.getUser().getId().equals(clientUserId)) {
            throw new AccessDeniedException("Нельзя создать чат с самим собой");
        }

        Chat chat = chatRepository.findByUserIdAndPartnerId(clientUserId, partnerId)
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .user(client)
                            .partner(partner)
                            .build();
                    Chat saved = chatRepository.save(newChat);
                    log.info("Создан новый чат {} между клиентом {} и партнёром {}",
                            saved.getId(), clientUserId, partnerId);
                    return saved;
                });

        return mapToChatResponse(chat, clientUserId);
    }

    /**
     * Возвращает чат по ID, проверяя что текущий пользователь имеет к нему доступ.
     */
    @Transactional(readOnly = true)
    public ChatResponse getChatById(UUID chatId, UUID currentUserId) {
        Chat chat = findChatAndCheckAccess(chatId, currentUserId);
        return mapToChatResponse(chat, currentUserId);
    }

    /**
     * Возвращает все диалоги клиента.
     */
    @Transactional(readOnly = true)
    public Page<ChatResponse> getMyChatsAsClient(UUID userId, Pageable pageable) {
        return chatRepository.findAllByUserId(userId, pageable)
                .map(chat -> mapToChatResponse(chat, userId));
    }

    /**
     * Возвращает все диалоги партнёра (текущий пользователь должен быть владельцем партнёра).
     */
    @Transactional(readOnly = true)
    public Page<ChatResponse> getMyChatsAsPartner(UUID currentUserId, Pageable pageable) {
        Partner partner = partnerRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new NotFoundException("Профиль партнёра не найден"));

        return chatRepository.findAllByPartnerId(partner.getId(), pageable)
                .map(chat -> mapToChatResponse(chat, currentUserId));
    }

    /**
     * Возвращает общее количество непрочитанных сообщений пользователя
     * по всем его чатам (для бейджика в шапке).
     */
    @Transactional(readOnly = true)
    public long getTotalUnreadCount(UUID userId) {
        return chatMessageRepository.countAllUnreadForUser(userId);
    }

    /**
     * Возвращает историю сообщений чата с пагинацией (новые сверху).
     */
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(UUID chatId, UUID currentUserId, Pageable pageable) {
        findChatAndCheckAccess(chatId, currentUserId);

        return chatMessageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable)
                .map(this::mapToMessageResponse);
    }

    /**
     * Отправляет сообщение в чат.
     * Отправителем может быть любая из сторон диалога (клиент или партнёр).
     * Сообщение должно содержать либо текст, либо вложения, либо и то, и другое.
     */
    @Transactional
    public ChatMessageResponse sendMessage(UUID chatId,
                                           UUID senderUserId,
                                           String content,
                                           List<String> attachmentUrls) {
        Chat chat = findChatAndCheckAccess(chatId, senderUserId);

        boolean hasContent = content != null && !content.isBlank();
        boolean hasAttachments = attachmentUrls != null && !attachmentUrls.isEmpty();

        if (!hasContent && !hasAttachments) {
            throw new BadRequestException("Сообщение должно содержать текст или вложения");
        }

        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(hasContent ? content : null)
                .attachmentUrls(hasAttachments ? attachmentUrls : null)
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Обновляем время последнего сообщения в чате
        chat.setLastMessageAt(saved.getCreatedAt());
        chatRepository.save(chat);

        log.info("Сообщение {} отправлено в чат {} пользователем {}",
                saved.getId(), chatId, senderUserId);

        ChatMessageResponse response = mapToMessageResponse(saved);

        // Публикуем в STOMP-топик — все подписчики получат сообщение в реалтайме
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, response);

        // Уведомляем получателя (push в notifications + email/sms)
        notifyRecipient(chat, sender);

        return response;
    }

    /**
     * Помечает все непрочитанные сообщения чата как прочитанные
     * для текущего пользователя (только те, что были отправлены не им).
     * <p>
     * После обновления публикует read-receipt в персональную очередь
     * другой стороны диалога — отправитель увидит, что его сообщения прочитали.
     *
     * @return количество помеченных сообщений
     */
    @Transactional
    public int markAsRead(UUID chatId, UUID currentUserId) {
        Chat chat = findChatAndCheckAccess(chatId, currentUserId);

        LocalDateTime readAt = LocalDateTime.now();
        int updated = chatMessageRepository.markAllAsReadForRecipient(
                chatId, currentUserId, readAt);

        log.debug("Помечено как прочитанные: {} сообщений в чате {} для пользователя {}",
                updated, chatId, currentUserId);

        // Если ничего не изменилось — не дёргаем других участников
        if (updated > 0) {
            broadcastReadReceipt(chat, currentUserId, readAt, updated);
        }

        return updated;
    }

    /**
     * Находит чат и проверяет, что текущий пользователь является
     * либо клиентом, либо владельцем партнёра в этом чате.
     */
    private Chat findChatAndCheckAccess(UUID chatId, UUID currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Чат не найден"));

        boolean isClient = chat.getUser().getId().equals(currentUserId);
        boolean isPartnerOwner = chat.getPartner().getUser().getId().equals(currentUserId);

        if (!isClient && !isPartnerOwner) {
            throw new AccessDeniedException("Нет доступа к этому чату");
        }

        return chat;
    }

    /**
     * Отправляет уведомление получателю о новом сообщении.
     * Получатель — это та сторона диалога, которая НЕ является отправителем.
     */
    private void notifyRecipient(Chat chat, User sender) {
        UUID recipientId;
        String senderName;

        if (chat.getUser().getId().equals(sender.getId())) {
            // Отправитель — клиент, получатель — партнёр (его user)
            recipientId = chat.getPartner().getUser().getId();
            senderName = sender.getFullName();
        } else {
            // Отправитель — партнёр, получатель — клиент
            recipientId = chat.getUser().getId();
            senderName = chat.getPartner().getCompanyName();
        }

        notificationService.send(
                recipientId,
                NotificationType.NEW_MESSAGE,
                "Новое сообщение",
                "У вас новое сообщение от " + senderName,
                RelatedEntityType.CHAT,
                chat.getId()
        );
    }

    /**
     * Публикует read-receipt в персональную очередь другой стороны диалога.
     * Получатель события — это тот, чьи сообщения только что были прочитаны.
     */
    private void broadcastReadReceipt(Chat chat, UUID readerUserId, LocalDateTime readAt, int count) {
        // Определяем «другую сторону» — это автор прочитанных сообщений
        User otherSide;
        if (chat.getUser().getId().equals(readerUserId)) {
            // Читал клиент → уведомляем владельца партнёра
            otherSide = chat.getPartner().getUser();
        } else {
            // Читал партнёр → уведомляем клиента
            otherSide = chat.getUser();
        }

        ChatReadEvent event = ChatReadEvent.builder()
                .chatId(chat.getId())
                .readBy(readerUserId)
                .readAt(readAt)
                .markedCount(count)
                .build();

        // Имя пользователя в STOMP — это email (так настроено в JwtChannelInterceptor → StompPrincipal)
        messagingTemplate.convertAndSendToUser(
                otherSide.getEmail(),
                "/queue/chat-read",
                event
        );

        log.debug("Read-receipt отправлен пользователю {} по чату {}", otherSide.getEmail(), chat.getId());
    }

    /**
     * Преобразует Chat в DTO с превью последнего сообщения и счётчиком непрочитанных.
     */
    private ChatResponse mapToChatResponse(Chat chat, UUID currentUserId) {
        Optional<ChatMessage> lastMessage =
                chatMessageRepository.findFirstByChatIdOrderByCreatedAtDesc(chat.getId());

        long unreadCount = chatMessageRepository.countUnreadForRecipient(
                chat.getId(), currentUserId);

        return ChatResponse.builder()
                .id(chat.getId())
                .userId(chat.getUser().getId())
                .userFullName(chat.getUser().getFullName())
                .partnerId(chat.getPartner().getId())
                .partnerCompanyName(chat.getPartner().getCompanyName())
                .partnerLogoUrl(chat.getPartner().getLogoUrl())
                .lastMessageContent(lastMessage.map(this::buildPreview).orElse(null))
                .lastMessageSenderId(lastMessage.map(m -> m.getSender().getId()).orElse(null))
                .lastMessageAt(chat.getLastMessageAt())
                .unreadCount(unreadCount)
                .createdAt(chat.getCreatedAt())
                .build();
    }

    /**
     * Преобразует ChatMessage в DTO.
     */
    private ChatMessageResponse mapToMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .senderFullName(message.getSender().getFullName())
                .content(message.getContent())
                .attachmentUrls(message.getAttachmentUrls())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Формирует превью сообщения для списка диалогов.
     * Если есть текст — возвращает его, иначе помечает как вложение.
     */
    private String buildPreview(ChatMessage message) {
        if (message.getContent() != null && !message.getContent().isBlank()) {
            return message.getContent();
        }
        if (message.getAttachmentUrls() != null && !message.getAttachmentUrls().isEmpty()) {
            int count = message.getAttachmentUrls().size();
            return "📎 Вложение" + (count > 1 ? " (" + count + ")" : "");
        }
        return null;
    }
}