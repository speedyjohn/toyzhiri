package org.example.toy_zhiri.chat.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сущность сообщения в чате.
 * Отправитель — всегда User (либо клиент, либо пользователь, привязанный к партнёру).
 * <p>
 * Сообщение может содержать только текст, только вложения, либо и то, и другое.
 * Гарантия «не пустое» обеспечивается чек-констрейнтом chk_chat_messages_content_or_attachments.
 */
@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * URL-ы прикреплённых файлов (фото, документы).
     * Файлы предварительно загружаются через POST /api/v1/files/upload.
     */
    @Type(ListArrayType.class)
    @Column(name = "attachment_urls", columnDefinition = "text[]")
    private List<String> attachmentUrls;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}