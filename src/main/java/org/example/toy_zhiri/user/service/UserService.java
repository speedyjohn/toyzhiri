package org.example.toy_zhiri.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.exception.BadRequestException;
import org.example.toy_zhiri.exception.ConflictException;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.user.dto.ChangePasswordRequest;
import org.example.toy_zhiri.user.dto.CompleteProfileRequest;
import org.example.toy_zhiri.user.dto.DeleteAccountRequest;
import org.example.toy_zhiri.user.dto.UpdateProfileRequest;
import org.example.toy_zhiri.user.dto.UserInfoResponse;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.enums.AuthProvider;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления пользователями.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    /**
     * Получает пользователя по email.
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Получает пользователя по email или выбрасывает исключение.
     */
    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден с email: " + email));
    }

    /**
     * Получает идентификатор пользователя по email.
     */
    public UUID getIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }

    /**
     * Получает информацию о пользователе по email.
     */
    public UserInfoResponse getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return mapToUserInfoResponse(user);
    }

    /**
     * Обновляет профиль пользователя.
     * Пользователь может изменить только: firstName, lastName, phone, city.
     */
    @Transactional
    public UserInfoResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmailOrThrow(email);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            userRepository.findByEmail(email).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new ConflictException("Телефон уже используется другим пользователем");
                }
            });
            user.setPhone(request.getPhone());
        }

        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserInfoResponse(updatedUser);
    }

    /**
     * Дозаполняет профиль пользователя после входа через OAuth-провайдера.
     * Доступно только если профиль ещё не завершён.
     *
     * @param email   email текущего пользователя
     * @param request данные для дозаполнения (телефон, город)
     * @return обновлённая информация о пользователе
     * @throws BadRequestException если профиль уже завершён
     * @throws ConflictException   если телефон занят другим пользователем
     */
    @Transactional
    public UserInfoResponse completeProfile(String email, CompleteProfileRequest request) {
        User user = getUserByEmailOrThrow(email);

        if (Boolean.TRUE.equals(user.getProfileCompleted())) {
            throw new BadRequestException(
                    "Профиль уже заполнен. Используйте обновление профиля");
        }

        userRepository.findByPhone(request.getPhone()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(user.getId())) {
                throw new ConflictException("Пользователь с таким телефоном уже существует");
            }
        });

        user.setPhone(request.getPhone());
        user.setCity(request.getCity());
        user.setProfileCompleted(true);

        User saved = userRepository.save(user);

        return mapToUserInfoResponse(saved);
    }

    /**
     * Изменяет пароль пользователя.
     */
    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmailOrThrow(email);

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new BadRequestException(
                    "Смена пароля недоступна для аккаунтов, созданных через Google");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Текущий пароль неверен");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Новый пароль должен отличаться от текущего");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Уведомление о смене пароля
        notificationService.send(
                user.getId(),
                NotificationType.PASSWORD_CHANGED,
                "Пароль изменён",
                "Ваш пароль был изменён. Если это были не вы — обратитесь в поддержку"
        );

        return MessageResponse.builder()
                .message("Пароль успешно изменен")
                .build();
    }

    /**
     * Удаляет аккаунт пользователя (soft delete).
     */
    @Transactional
    public MessageResponse deleteAccount(String email, DeleteAccountRequest request) {
        User user = getUserByEmailOrThrow(email);

        if (user.getAuthProvider() == AuthProvider.LOCAL) {
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadRequestException("Неверный пароль. Удаление отменено.");
            }
        }

        // Удаляем пользователя (soft delete)
        user.setIsActive(false);
        userRepository.save(user);

        // Уведомление о деактивации аккаунта
        notificationService.send(
                user.getId(),
                NotificationType.ACCOUNT_DEACTIVATED,
                "Аккаунт удалён",
                "Ваш аккаунт был деактивирован по вашему запросу"
        );

        return MessageResponse.builder()
                .message("Ваш аккаунт успешно удален")
                .build();
    }

    /**
     * Преобразует сущность User в DTO UserInfoResponse.
     */
    private UserInfoResponse mapToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .city(user.getCity())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .authProvider(user.getAuthProvider().name())
                .profileCompleted(user.getProfileCompleted())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}