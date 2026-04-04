package org.example.toy_zhiri.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.notification.dto.NotificationResponse;
import org.example.toy_zhiri.notification.dto.UnreadCountResponse;
import org.example.toy_zhiri.notification.entity.Notification;
import org.example.toy_zhiri.notification.entity.NotificationSettings;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.enums.RelatedEntityType;
import org.example.toy_zhiri.notification.repository.NotificationRepository;
import org.example.toy_zhiri.notification.repository.NotificationSettingsRepository;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Центральный сервис уведомлений.
 *
 * Отвечает за:
 * - Создание in-app уведомлений с учётом настроек пользователя
 * - Получение списка уведомлений
 * - Пометку уведомлений как прочитанных
 * - Подсчёт непрочитанных
 *
 * В будущем здесь же будет диспатч на email и SMS каналы.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    // =========================================================
    // СОЗДАНИЕ УВЕДОМЛЕНИЙ (вызывается из других сервисов)
    // =========================================================

    /**
     * Создаёт уведомление для пользователя.
     * Перед созданием проверяет настройки пользователя — хочет ли он получать
     * уведомления данного типа.
     *
     * @param userId              ID получателя
     * @param type                тип уведомления
     * @param title               заголовок
     * @param message             текст сообщения
     * @param relatedEntityType   тип связанной сущности (может быть null)
     * @param relatedEntityId     ID связанной сущности (может быть null)
     */
    @Transactional
    public void send(UUID userId,
                     NotificationType type,
                     String title,
                     String message,
                     RelatedEntityType relatedEntityType,
                     UUID relatedEntityId) {

        if (!isNotificationEnabled(userId, type)) {
            log.debug("Уведомление {} для пользователя {} отключено в настройках", type, userId);
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build();

        notificationRepository.save(notification);

        log.info("Уведомление [{}] отправлено пользователю {}: {}", type, userId, title);

        // TODO: В будущем — диспатч на email/SMS каналы
    }

    /**
     * Упрощённый вариант без привязки к сущности.
     */
    @Transactional
    public void send(UUID userId,
                     NotificationType type,
                     String title,
                     String message) {
        send(userId, type, title, message, null, null);
    }

    // =========================================================
    // ЧТЕНИЕ УВЕДОМЛЕНИЙ (для контроллера)
    // =========================================================

    /**
     * Возвращает все уведомления пользователя с пагинацией.
     */
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Возвращает только непрочитанные уведомления.
     */
    public Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Возвращает количество непрочитанных уведомлений.
     */
    public UnreadCountResponse getUnreadCount(UUID userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return UnreadCountResponse.builder().count(count).build();
    }

    // =========================================================
    // ПОМЕТКА КАК ПРОЧИТАННЫХ
    // =========================================================

    /**
     * Помечает одно уведомление как прочитанное.
     */
    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("У вас нет доступа к этому уведомлению");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    /**
     * Помечает все уведомления пользователя как прочитанные.
     */
    @Transactional
    public MessageResponse markAllAsRead(UUID userId) {
        int updated = notificationRepository.markAllAsRead(userId);
        return MessageResponse.builder()
                .message("Прочитано уведомлений: " + updated)
                .build();
    }

    // =========================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =========================================================

    /**
     * Проверяет, включён ли данный тип уведомлений в настройках пользователя.
     * Если настройки не созданы — считаем, что всё включено (дефолт).
     */
    private boolean isNotificationEnabled(UUID userId, NotificationType type) {
        return settingsRepository.findByUserId(userId)
                .map(settings -> isTypeEnabled(settings, type))
                .orElse(true);
    }

    /**
     * Маппит тип уведомления на соответствующий флаг в настройках.
     */
    private boolean isTypeEnabled(NotificationSettings settings, NotificationType type) {
        return switch (type) {
            case BOOKING_CREATED,
                 BOOKING_CONFIRMED,
                 BOOKING_REJECTED,
                 BOOKING_CANCELLED,
                 BOOKING_COMPLETED,
                 BOOKING_EXPIRED,
                 BOOKING_COMPLETION_CONFIRMED -> settings.getBookingUpdates();

            case REVIEW_RECEIVED -> settings.getBookingUpdates();

            case PROMOTION -> settings.getPromotions();

            case EVENT_REMINDER -> settings.getEventReminders();

            // Системные уведомления отправляются всегда
            case SERVICE_APPROVED,
                 SERVICE_REJECTED,
                 PASSWORD_CHANGED,
                 ACCOUNT_DEACTIVATED,
                 PAYMENT_SUCCESS,
                 SYSTEM -> true;
        };
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .relatedEntityType(
                        notification.getRelatedEntityType() != null
                                ? notification.getRelatedEntityType().name()
                                : null
                )
                .relatedEntityId(notification.getRelatedEntityId())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}