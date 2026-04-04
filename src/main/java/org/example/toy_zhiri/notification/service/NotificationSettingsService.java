package org.example.toy_zhiri.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.notification.dto.NotificationSettingsResponse;
import org.example.toy_zhiri.notification.dto.UpdateNotificationSettingsRequest;
import org.example.toy_zhiri.notification.entity.NotificationSettings;
import org.example.toy_zhiri.notification.repository.NotificationSettingsRepository;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationSettingsService {
    private final NotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    /**
     * Возвращает настройки уведомлений пользователя.
     * Если настройки ещё не созданы — создаёт с дефолтными значениями.
     */
    public NotificationSettingsResponse getSettings(UUID userId) {
        NotificationSettings settings = getOrCreateSettings(userId);
        return mapToResponse(settings);
    }

    /**
     * Обновляет настройки уведомлений.
     * Обновляются только те поля, которые переданы (не null).
     */
    @Transactional
    public NotificationSettingsResponse updateSettings(
            UUID userId,
            UpdateNotificationSettingsRequest request) {

        NotificationSettings settings = getOrCreateSettings(userId);

        if (request.getPushEnabled() != null) {
            settings.setPushEnabled(request.getPushEnabled());
        }
        if (request.getEmailEnabled() != null) {
            settings.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getSmsEnabled() != null) {
            settings.setSmsEnabled(request.getSmsEnabled());
        }
        if (request.getBookingUpdates() != null) {
            settings.setBookingUpdates(request.getBookingUpdates());
        }
        if (request.getChatMessages() != null) {
            settings.setChatMessages(request.getChatMessages());
        }
        if (request.getPromotions() != null) {
            settings.setPromotions(request.getPromotions());
        }
        if (request.getEventReminders() != null) {
            settings.setEventReminders(request.getEventReminders());
        }
        if (request.getNewBookings() != null) {
            settings.setNewBookings(request.getNewBookings());
        }

        return mapToResponse(settingsRepository.save(settings));
    }

    /**
     * Возвращает настройки пользователя или создаёт их с дефолтными значениями.
     */
    private NotificationSettings getOrCreateSettings(UUID userId) {
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

                    return settingsRepository.save(
                            NotificationSettings.builder()
                                    .user(user)
                                    .pushEnabled(true)
                                    .emailEnabled(true)
                                    .smsEnabled(false)
                                    .bookingUpdates(true)
                                    .chatMessages(true)
                                    .promotions(false)
                                    .eventReminders(true)
                                    .newBookings(true)
                                    .build()
                    );
                });
    }

    private NotificationSettingsResponse mapToResponse(NotificationSettings settings) {
        return NotificationSettingsResponse.builder()
                .pushEnabled(settings.getPushEnabled())
                .emailEnabled(settings.getEmailEnabled())
                .smsEnabled(settings.getSmsEnabled())
                .bookingUpdates(settings.getBookingUpdates())
                .chatMessages(settings.getChatMessages())
                .promotions(settings.getPromotions())
                .eventReminders(settings.getEventReminders())
                .newBookings(settings.getNewBookings())
                .build();
    }
}