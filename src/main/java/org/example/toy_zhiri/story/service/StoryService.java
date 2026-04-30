package org.example.toy_zhiri.story.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.exception.AccessDeniedException;
import org.example.toy_zhiri.exception.InvalidStateException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.enums.RelatedEntityType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.payment.enums.PaymentMethod;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.story.dto.CreateStoryRequest;
import org.example.toy_zhiri.story.dto.StoryAnalyticsResponse;
import org.example.toy_zhiri.story.dto.StoryResponse;
import org.example.toy_zhiri.story.entity.Story;
import org.example.toy_zhiri.story.entity.StoryView;
import org.example.toy_zhiri.story.enums.StoryStatus;
import org.example.toy_zhiri.story.repository.StoryRepository;
import org.example.toy_zhiri.story.repository.StoryViewRepository;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы со сторис.
 * Содержит логику создания (с моковой оплатой), просмотра ленты,
 * учёта уникальных просмотров и удаления.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {

    /**
     * Срок жизни сторис — 24 часа.
     */
    private static final int STORY_LIFETIME_HOURS = 24;

    /**
     * Моковая фиксированная цена за публикацию одной сторис.
     */
    private static final BigDecimal STORY_PRICE = new BigDecimal("1500.00");

    /**
     * Лимит сторис в публичной ленте.
     */
    private static final int FEED_LIMIT = 20;

    private final StoryRepository storyRepository;
    private final StoryViewRepository storyViewRepository;
    private final ServiceRepository serviceRepository;
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Создаёт сторис с моковой оплатой.
     * После успешной оплаты сторис сразу активируется на 24 часа.
     */
    @Transactional
    public StoryResponse createStory(UUID userId, CreateStoryRequest request) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new AccessDeniedException("Услуга не принадлежит вам");
        }

        // Моковая обработка оплаты
        String paymentId = generatePaymentId(request.getPaymentMethod());

        LocalDateTime now = LocalDateTime.now();

        Story story = Story.builder()
                .service(service)
                .partner(partner)
                .category(service.getCategory())
                .mediaUrl(request.getMediaUrl())
                .mediaType(request.getMediaType())
                .caption(request.getCaption())
                .status(StoryStatus.ACTIVE)
                .paidAmount(STORY_PRICE)
                .paymentMethod(request.getPaymentMethod())
                .paymentId(paymentId)
                .viewsCount(0)
                .expiresAt(now.plusHours(STORY_LIFETIME_HOURS))
                .build();

        Story saved = storyRepository.save(story);

        log.info("Сторис {} создана партнёром {} (услуга: {}, оплачено: {} {})",
                saved.getId(), partner.getCompanyName(), service.getName(),
                STORY_PRICE, request.getPaymentMethod());

        // Уведомление об успешной оплате
        notificationService.send(
                userId,
                NotificationType.PAYMENT_SUCCESS,
                "Сторис опубликована",
                "Оплата прошла успешно. Сторис для услуги «" + service.getName() +
                        "» активна до " + saved.getExpiresAt().toLocalDate() + " " +
                        saved.getExpiresAt().toLocalTime().withSecond(0).withNano(0),
                RelatedEntityType.SERVICE,
                service.getId()
        );

        return mapToResponse(saved);
    }

    /**
     * Публичная лента сторис — рандомная выборка активных.
     * Если передана категория — выборка только в её рамках.
     */
    public List<StoryResponse> getFeed(UUID categoryId) {
        List<Story> stories = categoryId != null
                ? storyRepository.findRandomActiveByCategory(categoryId, FEED_LIMIT)
                : storyRepository.findRandomActive(FEED_LIMIT);

        return stories.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Возвращает детали одной сторис.
     * Доступна любая сторис (даже истёкшая) — для просмотра модалки или ссылки.
     */
    public StoryResponse getStoryById(UUID storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Сторис не найдена"));

        return mapToResponse(story);
    }

    /**
     * Регистрирует уникальный просмотр сторис.
     * Если пользователь уже смотрел эту сторис — повторный вызов игнорируется.
     */
    @Transactional
    public void registerView(UUID userId, UUID storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Сторис не найдена"));

        if (story.getStatus() != StoryStatus.ACTIVE) {
            throw new InvalidStateException("Сторис недоступна для просмотра");
        }

        if (storyViewRepository.existsByStoryIdAndUserId(storyId, userId)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        try {
            StoryView view = StoryView.builder()
                    .story(story)
                    .user(user)
                    .build();
            storyViewRepository.save(view);
            storyRepository.incrementViewsCount(storyId);
        } catch (DataIntegrityViolationException e) {
            // Гонка: запись уже существует — это нормально, считается зарегистрированной
            log.debug("Просмотр сторис {} пользователем {} уже зарегистрирован", storyId, userId);
        }
    }

    /**
     * Список сторис партнёра — для личного кабинета.
     */
    public Page<StoryResponse> getMyStories(UUID userId, Pageable pageable) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        return storyRepository
                .findByPartnerIdOrderByCreatedAtDesc(partner.getId(), pageable)
                .map(this::mapToResponse);
    }

    /**
     * Аналитика по конкретной сторис партнёра.
     */
    public StoryAnalyticsResponse getStoryAnalytics(UUID userId, UUID storyId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Сторис не найдена"));

        if (!story.getPartner().getId().equals(partner.getId())) {
            throw new AccessDeniedException("Сторис не принадлежит вам");
        }

        return StoryAnalyticsResponse.builder()
                .storyId(story.getId())
                .viewsCount(story.getViewsCount())
                .status(story.getStatus().name())
                .createdAt(story.getCreatedAt())
                .expiresAt(story.getExpiresAt())
                .build();
    }

    /**
     * Партнёр досрочно удаляет свою сторис.
     */
    @Transactional
    public MessageResponse deleteMyStory(UUID userId, UUID storyId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Сторис не найдена"));

        if (!story.getPartner().getId().equals(partner.getId())) {
            throw new AccessDeniedException("Сторис не принадлежит вам");
        }

        if (story.getStatus() != StoryStatus.ACTIVE) {
            throw new InvalidStateException("Можно удалить только активную сторис");
        }

        story.setStatus(StoryStatus.REMOVED_BY_PARTNER);
        storyRepository.save(story);

        log.info("Сторис {} удалена партнёром {}", storyId, partner.getCompanyName());

        return MessageResponse.builder()
                .message("Сторис удалена")
                .build();
    }

    /**
     * Список всех сторис для админ-панели.
     */
    public Page<StoryResponse> getAllStories(StoryStatus status, Pageable pageable) {
        Page<Story> stories = status != null
                ? storyRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : storyRepository.findAllByOrderByCreatedAtDesc(pageable);

        return stories.map(this::mapToResponse);
    }

    /**
     * Администратор снимает сторис с публикации.
     */
    @Transactional
    public MessageResponse removeStoryByAdmin(UUID storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Сторис не найдена"));

        if (story.getStatus() == StoryStatus.REMOVED_BY_ADMIN) {
            throw new InvalidStateException("Сторис уже снята администратором");
        }

        story.setStatus(StoryStatus.REMOVED_BY_ADMIN);
        storyRepository.save(story);

        log.info("Сторис {} снята администратором (партнёр: {}, услуга: {})",
                storyId, story.getPartner().getCompanyName(), story.getService().getName());

        // Уведомление партнёру
        notificationService.send(
                story.getPartner().getUser().getId(),
                NotificationType.SYSTEM,
                "Сторис снята",
                "Ваша сторис для услуги «" + story.getService().getName() +
                        "» была снята администратором",
                RelatedEntityType.SERVICE,
                story.getService().getId()
        );

        return MessageResponse.builder()
                .message("Сторис снята с публикации")
                .build();
    }

    /**
     * Генерирует моковый ID платежа в зависимости от способа оплаты.
     */
    private String generatePaymentId(PaymentMethod method) {
        String prefix = switch (method) {
            case KASPI -> "KASPI-";
            case BANK_CARD -> "CARD-";
            case GOOGLE_PAY -> "GPAY-";
            case APPLE_PAY -> "APAY-";
        };
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private StoryResponse mapToResponse(Story story) {
        return StoryResponse.builder()
                .id(story.getId())
                .serviceId(story.getService().getId())
                .serviceName(story.getService().getName())
                .partnerId(story.getPartner().getId())
                .partnerCompanyName(story.getPartner().getCompanyName())
                .categoryId(story.getCategory().getId())
                .categoryName(story.getCategory().getNameRu())
                .mediaUrl(story.getMediaUrl())
                .mediaType(story.getMediaType().name())
                .caption(story.getCaption())
                .status(story.getStatus().name())
                .paidAmount(story.getPaidAmount())
                .paymentMethod(story.getPaymentMethod() != null
                        ? story.getPaymentMethod().name() : null)
                .paymentId(story.getPaymentId())
                .viewsCount(story.getViewsCount())
                .expiresAt(story.getExpiresAt())
                .createdAt(story.getCreatedAt())
                .build();
    }
}