package org.example.toy_zhiri.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность для хранения заблокированных JWT токенов.
 * Используется для реализации функции logout.
 */
@Entity
@Table(name = "token_blacklist")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "token", length = 500, nullable = false, unique = true)
    private String token;

    @Column(name = "user_email", length = 50, nullable = false)
    private String userEmail;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @CreationTimestamp
    @Column(name = "blacklisted_at", nullable = false, updatable = false)
    private LocalDateTime blacklistedAt;
}