package org.example.toy_zhiri.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.user.dto.ChangePasswordRequest;
import org.example.toy_zhiri.user.dto.UpdateProfileRequest;
import org.example.toy_zhiri.user.dto.UserInfoResponse;
import org.example.toy_zhiri.user.dto.DeleteAccountRequest;
import org.example.toy_zhiri.user.entity.User;
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

    /**
     * Получает пользователя по email.
     *
     * @param email email пользователя
     * @return Optional<User> с пользователь, если найден
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Получает пользователя по email или выбрасывает исключение.
     *
     * @param email email пользователя
     * @return User пользователь
     * @throws UsernameNotFoundException если пользователь не найден
     */
    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден с email: " + email));
    }

    /**
     * Получает идентификатор пользователя по email.
     *
     * @param email email пользователя
     * @return UUID идентификатор пользователя или null, если не найден
     */
    public UUID getIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }

    /**
     * Получает информацию о пользователе по email.
     *
     * @param email email пользователя
     * @return UserInfoResponse информация о пользователе
     * @throws UsernameNotFoundException если пользователь не найден
     */
    public UserInfoResponse getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return mapToUserInfoResponse(user);
    }

    /**
     * Обновляет профиль пользователя.
     * Пользователь может изменить только: firstName, lastName, phone, city.
     *
     * @param email email пользователя
     * @param request данные для обновления
     * @return обновленная информация о пользователе
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
                    throw new RuntimeException("Телефон уже используется другим пользователем");
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
     * Изменяет пароль пользователя.
     *
     * @param email email пользователя
     * @param request данные для смены пароля
     * @return MessageResponse сообщение об успехе
     * @throws RuntimeException если текущий пароль неверный
     */
    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmailOrThrow(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Текущий пароль неверен");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Новый пароль должен отличаться от текущего");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // TODO: Отправить уведомление на email о смене пароля когда будет email сервис

        return MessageResponse.builder()
                .message("Пароль успешно изменен")
                .build();
    }

    /**
     * Удаляет аккаунт пользователя (soft delete).
     *
     * @param email email пользователя
     * @param request данные для подтверждения удаления
     * @return MessageResponse сообщение об успехе
     * @throws RuntimeException если пароль неверный
     */
    @Transactional
    public MessageResponse deleteAccount(String email, DeleteAccountRequest request) {
        User user = getUserByEmailOrThrow(email);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверный пароль. Удаление отменено.");
        }

        // TODO: Добавить в login history?
        // TODO: Отправить email о удалении аккаунта

        // Удаляем пользователя (soft delete)
        user.setIsActive(false);
        userRepository.save(user);

        return MessageResponse.builder()
                .message("Ваш аккаунт успешно удален")
                .build();
    }

    /**
     * Преобразует сущность User в DTO UserInfoResponse.
     *
     * @param user сущность пользователя
     * @return UserInfoResponse DTO с информацией о пользователе
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
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
