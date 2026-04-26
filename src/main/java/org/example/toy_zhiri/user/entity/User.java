package org.example.toy_zhiri.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.user.enums.AuthProvider;
import org.example.toy_zhiri.user.enums.UserRole;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность пользователя.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "email", length = 50, unique = true, nullable = false)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(name = "phone", length = 11, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role = UserRole.USER;

    @Column(name = "city", length = 30)
    private String city;

    @Column(name = "emailverified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "profile_completed", nullable = false)
    private Boolean profileCompleted = true;

    @Column(name = "lastlogin")
    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Вспомогательный метод для получения полного имени.
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}