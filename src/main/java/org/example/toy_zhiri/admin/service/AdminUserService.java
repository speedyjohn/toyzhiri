package org.example.toy_zhiri.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.*;
import org.example.toy_zhiri.exception.BadRequestException;
import org.example.toy_zhiri.exception.ConflictException;
import org.example.toy_zhiri.exception.NotFoundException;
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
     * Получить список всех пользователей с пагинацией и фильтрацией.
     *
     * @param pageable      параметры пагинации и сортировки
     * @param role          фильтр по роли (опционально)
     * @param search        поиск по email, имени (опционально)
     * @param emailVerified фильтр по верификации email (опционально)
     * @return Page<AdminUserResponse> страница с пользователями
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
                throw new BadRequestException("Некорректная роль: " + role);
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
     * Получить детальную информацию о пользователе по ID.
     *
     * @param userId идентификатор пользователя
     * @return AdminUserDetailResponse детальная информация о пользователе
     */
    public AdminUserDetailResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        return mapToAdminUserDetailResponse(user);
    }

    /**
     * Получить детальную информацию о пользователе по email.
     *
     * @param email email пользователя
     * @return AdminUserDetailResponse детальная информация о пользователе
     */
    public AdminUserDetailResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь с email " + email + " не найден"));

        return mapToAdminUserDetailResponse(user);
    }

    /**
     * Создать нового пользователя администратором.
     *
     * @param request данные нового пользователя
     * @return AdminUserResponse созданный пользователь
     */
    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с email " + request.getEmail() + " уже существует");
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
     * Обновить информацию о пользователе.
     *
     * @param userId  идентификатор пользователя
     * @param request данные для обновления
     * @return AdminUserResponse обновлённый пользователь
     */
    @Transactional
    public AdminUserResponse updateUser(UUID userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email " + request.getEmail() + " уже занят");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getCity() != null) user.setCity(request.getCity());

        User updatedUser = userRepository.save(user);
        return mapToAdminUserResponse(updatedUser);
    }

    /**
     * Изменить роль пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param request новая роль пользователя
     * @return AdminUserResponse обновлённый пользователь
     */
    @Transactional
    public AdminUserResponse changeUserRole(UUID userId, AdminChangeRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        try {
            UserRole newRole = UserRole.valueOf(request.getRole().toUpperCase());
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Некорректная роль: " + request.getRole());
        }

        User updatedUser = userRepository.save(user);
        return mapToAdminUserResponse(updatedUser);
    }

    /**
     * Изменить статус верификации email пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param request новый статус верификации
     * @return AdminUserResponse обновлённый пользователь
     */
    @Transactional
    public AdminUserResponse changeEmailVerification(UUID userId,
                                                     AdminChangeEmailVerificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        user.setEmailVerified(request.getEmailVerified());
        User updatedUser = userRepository.save(user);
        return mapToAdminUserResponse(updatedUser);
    }

    /**
     * Сбросить пароль пользователя администратором.
     * После сброса пользователю отправляется уведомление.
     *
     * @param userId  идентификатор пользователя
     * @param request новый пароль
     * @return MessageResponse сообщение об успехе
     */
    @Transactional
    public MessageResponse resetUserPassword(UUID userId, AdminResetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

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
     * Изменить статус активности пользователя (блокировка / разблокировка).
     * При блокировке пользователю отправляется уведомление.
     *
     * @param userId  идентификатор пользователя
     * @param request новый статус активности
     * @return AdminUserResponse обновлённый пользователь
     */
    @Transactional
    public AdminUserResponse changeActiveStatus(UUID userId,
                                                AdminChangeActiveStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        user.setIsActive(request.getIsActive());
        User updatedUser = userRepository.save(user);

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
     * Удалить пользователя из системы (hard delete).
     *
     * @param userId идентификатор пользователя
     * @return MessageResponse сообщение об успехе
     */
    @Transactional
    public MessageResponse deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        String userEmail = user.getEmail();
        userRepository.delete(user);

        return MessageResponse.builder()
                .message("Пользователь " + userEmail + " успешно удален")
                .build();
    }

    /**
     * Получить статистику по пользователям системы.
     *
     * @return UserStatisticsResponse агрегированная статистика пользователей
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