package org.example.toy_zhiri.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.*;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.enums.UserRole;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Сервис для управления пользователями со стороны администратора.
 * Предоставляет полный CRUD функционал и дополнительные операции.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    /**
     * Получает список всех пользователей с пагинацией и фильтрацией.
     */
    public Page<AdminUserResponse> getAllUsers(Pageable pageable, String role,
                                               String search, Boolean emailVerified) {
        Specification<User> spec = Specification.where(null);

        if (role != null && !role.isBlank()) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("role"), userRole));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Некорректная роль: " + role);
            }
        }

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("firstName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("lastName")), "%" + search.toLowerCase() + "%")
                    ));
        }

        if (emailVerified != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("emailVerified"), emailVerified));
        }

        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::mapToAdminUserResponse);
    }

    /**
     * Получает детальную информацию о пользователе по ID.
     */
    public AdminUserDetailResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        return mapToAdminUserDetailResponse(user);
    }

    /**
     * Получает детальную информацию о пользователе по email.
     */
    public AdminUserDetailResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с email " + email + " не найден"));

        return mapToAdminUserDetailResponse(user);
    }

    /**
     * Создает нового пользователя (администратором).
     */
    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Пользователь с email " + request.getEmail() + " уже существует");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .city(request.getCity())
                .role(UserRole.valueOf(request.getRole().toUpperCase()))
                .emailVerified(request.getEmailVerified() != null ? request.getEmailVerified() : false)
                .build();

        User savedUser = userRepository.save(user);
        return mapToAdminUserResponse(savedUser);
    }

    /**
     * Обновляет информацию о пользователе.
     */
    @Transactional
    public AdminUserResponse updateUser(UUID userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email " + request.getEmail() + " уже занят");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
        }

        if(request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if(request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }

        User updatedUser = userRepository.save(user);
        return mapToAdminUserResponse(updatedUser);
    }

    /**
     * Изменяет роль пользователя.
     */
    @Transactional
    public AdminUserResponse changeUserRole(UUID userId, AdminChangeRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        try {
            UserRole newRole = UserRole.valueOf(request.getRole().toUpperCase());
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Некорректная роль: " + request.getRole());
        }

        User updatedUser = userRepository.save(user);
        return mapToAdminUserResponse(updatedUser);
    }

    /**
     * Изменяет статус верификации email пользователя.
     */
    @Transactional
    public AdminUserResponse changeEmailVerification(UUID userId,
                                                     AdminChangeEmailVerificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        user.setEmailVerified(request.getEmailVerified());
        User updatedUser = userRepository.save(user);
        return mapToAdminUserResponse(updatedUser);
    }

    /**
     * Сбрасывает пароль пользователя.
     */
    @Transactional
    public MessageResponse resetUserPassword(UUID userId, AdminResetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Уведомление пользователю о сбросе пароля администратором
        notificationService.send(
                userId,
                NotificationType.PASSWORD_CHANGED,
                "Пароль сброшен",
                "Ваш пароль был сброшен администратором. " +
                        "Если вы не запрашивали сброс — обратитесь в поддержку"
        );

        return MessageResponse.builder()
                .message("Пароль пользователя " + user.getEmail() + " успешно сброшен")
                .build();
    }

    /**
     * Изменяет статус активности пользователя (soft delete/restore).
     */
    @Transactional
    public AdminUserResponse changeActiveStatus(UUID userId,
                                                AdminChangeActiveStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        user.setIsActive(request.getIsActive());

        User updatedUser = userRepository.save(user);

        // Уведомление при блокировке аккаунта
        if (!request.getIsActive()) {
            notificationService.send(
                    userId,
                    NotificationType.ACCOUNT_DEACTIVATED,
                    "Аккаунт заблокирован",
                    "Ваш аккаунт был заблокирован администратором. " +
                            "Обратитесь в поддержку для уточнения причины"
            );
        }

        return mapToAdminUserResponse(updatedUser);
    }

    /**
     * Удаляет пользователя из системы (hard delete).
     */
    @Transactional
    public MessageResponse deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        String userEmail = user.getEmail();
        userRepository.delete(user);

        return MessageResponse.builder()
                .message("Пользователь " + userEmail + " успешно удален")
                .build();
    }

    /**
     * Получает статистику по пользователям.
     */
    public UserStatisticsResponse getUserStatistics() {
        long totalUsers = userRepository.count();
        long userCount = userRepository.countByRole(UserRole.USER);
        long partnerCount = userRepository.countByRole(UserRole.PARTNER);
        long adminCount = userRepository.countByRole(UserRole.ADMIN);
        long verifiedCount = userRepository.countByEmailVerified(true);
        long unverifiedCount = userRepository.countByEmailVerified(false);

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .userRoleCount(userCount)
                .partnerRoleCount(partnerCount)
                .adminRoleCount(adminCount)
                .verifiedEmailCount(verifiedCount)
                .unverifiedEmailCount(unverifiedCount)
                .build();
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .city(user.getCity())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private AdminUserDetailResponse mapToAdminUserDetailResponse(User user) {
        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .city(user.getCity())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}