package org.example.toy_zhiri.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.*;
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

    /**
     * Получает список всех пользователей с пагинацией и фильтрацией.
     *
     * Фильтрация работает следующим образом:
     * - role: фильтрует пользователей по роли (USER/PARTNER/ADMIN)
     * - search: ищет по email или fullName (регистронезависимо)
     * - emailVerified: фильтрует по статусу верификации email
     *
     * @param pageable параметры пагинации и сортировки
     * @param role фильтр по роли (опционально)
     * @param search строка поиска (опционально)
     * @param emailVerified фильтр по верификации email (опционально)
     * @return страница с пользователями
     */
    public Page<AdminUserResponse> getAllUsers(Pageable pageable, String role,
                                               String search, Boolean emailVerified) {
        Specification<User> spec = Specification.where(null);

        // Фильтрация по роли
        if (role != null && !role.isBlank()) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("role"), userRole));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Некорректная роль: " + role);
            }
        }

        // Поиск по email или имени
        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%")
                    ));
        }

        // Фильтрация по верификации email
        if (emailVerified != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("emailVerified"), emailVerified));
        }

        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::mapToAdminUserResponse);
    }

    /**
     * Получает детальную информацию о пользователе по ID.
     *
     * @param userId идентификатор пользователя
     * @return детальная информация о пользователе
     * @throws RuntimeException если пользователь не найден
     */
    public AdminUserDetailResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        return mapToAdminUserDetailResponse(user);
    }

    /**
     * Получает детальную информацию о пользователе по email.
     *
     * @param email email пользователя
     * @return детальная информация о пользователе
     * @throws RuntimeException если пользователь не найден
     */
    public AdminUserDetailResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с email " + email + " не найден"));

        return mapToAdminUserDetailResponse(user);
    }

    /**
     * Создает нового пользователя (администратором).
     *
     * Администратор может:
     * - Установить любую роль сразу при создании
     * - Установить статус верификации email
     * - Создать пользователя с уже готовым паролем
     *
     * @param request данные для создания пользователя
     * @return информация о созданном пользователе
     * @throws RuntimeException если email уже занят
     */
    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Пользователь с email " + request.getEmail() + " уже существует");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
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
     *
     * Можно обновить:
     * - Email (с проверкой уникальности)
     * - Имя (fullName)
     * - Телефон
     * - Город
     *
     * @param userId идентификатор пользователя
     * @param request данные для обновления
     * @return обновленная информация о пользователе
     * @throws RuntimeException если пользователь не найден или email занят
     */
    @Transactional
    public AdminUserResponse updateUser(UUID userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        // Проверка email на уникальность (если он изменяется)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email " + request.getEmail() + " уже занят");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false); // При смене email сбрасываем верификацию
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
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
     *
     * Возможные роли: USER, PARTNER, ADMIN
     *
     * Важно: При понижении роли PARTNER до USER,
     * связанные партнерские заявки останутся в системе,
     * но пользователь потеряет доступ к партнерскому функционалу.
     *
     * @param userId идентификатор пользователя
     * @param request новая роль
     * @return обновленная информация о пользователе
     * @throws RuntimeException если пользователь не найден или роль некорректна
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
     *
     * Администратор может вручную верифицировать или снять верификацию email.
     *
     * @param userId идентификатор пользователя
     * @param request статус верификации
     * @return обновленная информация о пользователе
     * @throws RuntimeException если пользователь не найден
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
     *
     * Администратор устанавливает новый пароль для пользователя.
     * В production окружении должно:
     * - Отправить уведомление на email пользователя
     * - Логировать эту операцию для безопасности
     *
     * @param userId идентификатор пользователя
     * @param request новый пароль
     * @return сообщение об успешной операции
     * @throws RuntimeException если пользователь не найден
     */
    @Transactional
    public MessageResponse resetUserPassword(UUID userId, AdminResetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // TODO: Отправить email пользователю о смене пароля

        return MessageResponse.builder()
                .message("Пароль пользователя " + user.getEmail() + " успешно сброшен")
                .build();
    }

    /**
     * Изменяет статус активности пользователя (soft delete/restore).
     *
     * Блокировка пользователя:
     * - Пользователь не сможет войти в систему
     * - Данные остаются в БД
     * - Можно восстановить позже
     *
     * Для реализации блокировки нужно:
     * 1. Добавить поле isActive в таблицу users (миграция БД)
     * 2. Добавить поле isActive в сущность User
     * 3. Проверять этот флаг при аутентификации
     *
     * @param userId идентификатор пользователя
     * @param request статус активности
     * @return обновленная информация о пользователе
     * @throws RuntimeException если пользователь не найден
     */
    @Transactional
    public AdminUserResponse changeActiveStatus(UUID userId,
                                                AdminChangeActiveStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        // TODO: Добавить поле isActive в сущность User и миграцию БД
        // user.setActive(request.getIsActive());

        User updatedUser = userRepository.save(user);
        return mapToAdminUserResponse(updatedUser);
    }

    /**
     * Удаляет пользователя из системы (hard delete).
     *
     * Полное удаление пользователя:
     * - Удаляются все связанные данные (cascade delete)
     * - Партнерские заявки удалятся автоматически (ON DELETE CASCADE в БД)
     * - Операция необратима!
     *
     * Безопасность:
     * - Нельзя удалить самого себя (опционально)
     * - Логируется для аудита
     *
     * @param userId идентификатор пользователя
     * @return сообщение об успешном удалении
     * @throws RuntimeException если пользователь не найден
     */
    @Transactional
    public MessageResponse deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        String userEmail = user.getEmail();
        userRepository.delete(user);

        // TODO: Логировать удаление для аудита
        // TODO: Отправить уведомление пользователю (если применимо)

        return MessageResponse.builder()
                .message("Пользователь " + userEmail + " успешно удален")
                .build();
    }

    /**
     * Получает статистику по пользователям.
     *
     * Подсчитывает:
     * - Общее количество пользователей
     * - Количество по каждой роли
     * - Количество верифицированных/неверифицированных
     * - Количество активных/заблокированных (если реализовано)
     *
     * @return статистика пользователей
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

    /**
     * Преобразует сущность User в краткий DTO для списков.
     */
    private AdminUserResponse mapToAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .city(user.getCity())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Преобразует сущность User в детальный DTO с дополнительной информацией.
     */
    private AdminUserDetailResponse mapToAdminUserDetailResponse(User user) {
        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .city(user.getCity())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                // Дополнительная информация для детального просмотра
                .totalLogins(0L) // TODO: Подключить после реализации логирования входов
                .build();
    }
}