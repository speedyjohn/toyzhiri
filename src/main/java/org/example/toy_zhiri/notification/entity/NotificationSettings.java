package org.example.toy_zhiri.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // --- Каналы ---

    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;

    // --- Типы событий (общие) ---

    /** Изменения статуса бронирования */
    @Column(name = "booking_updates", nullable = false)
    private Boolean bookingUpdates = true;

    /** Новые сообщения в чате */
    @Column(name = "chat_messages", nullable = false)
    private Boolean chatMessages = true;

    /** Акции и спецпредложения */
    @Column(name = "promotions", nullable = false)
    private Boolean promotions = false;

    // --- Типы событий только для клиентов ---

    /** Напоминания о предстоящем мероприятии */
    @Column(name = "event_reminders", nullable = false)
    private Boolean eventReminders = true;

    // --- Типы событий только для партнёров ---

    /** Новые входящие бронирования */
    @Column(name = "new_bookings", nullable = false)
    private Boolean newBookings = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}