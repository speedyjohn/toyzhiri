package org.example.toy_zhiri.user.service;

import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.user.dto.UserInfoResponse;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
