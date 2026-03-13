package org.example.toy_zhiri.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsResponse {

    // Каналы
    private Boolean pushEnabled;
    private Boolean emailEnabled;
    private Boolean smsEnabled;

    // Типы событий (общие)
    private Boolean bookingUpdates;
    private Boolean chatMessages;
    private Boolean promotions;

    // Только для клиентов
    private Boolean eventReminders;

    // Только для партнёров
    private Boolean newBookings;
}