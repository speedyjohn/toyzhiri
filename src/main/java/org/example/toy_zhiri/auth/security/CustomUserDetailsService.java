package org.example.toy_zhiri.auth.security;

import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Сервис для загрузки данных пользователя при аутентификации.
 */
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Загружает данные пользователя по email.
     *
     * @param email email пользователя
     * @return UserDetails с данными пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return buildUserDetails(user);
    }

    /**
     * Загружает данные пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return UserDetails с данными пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    public UserDetails loadUserById(UUID id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));

        return buildUserDetails(user);
    }

    /**
     * Создает объект UserDetails из сущности User.
     *
     * @param user сущность пользователя
     * @return UserDetails
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}