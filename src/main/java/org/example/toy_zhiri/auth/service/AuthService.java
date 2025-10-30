package org.example.toy_zhiri.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.auth.dto.AuthRequest;
import org.example.toy_zhiri.auth.dto.AuthResponse;
import org.example.toy_zhiri.auth.dto.RegisterRequest;
import org.example.toy_zhiri.auth.dto.RegisterResponse;
import org.example.toy_zhiri.auth.security.JwtTokenProvider;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.enums.UserRole;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Сервис для обработки аутентификации и регистрации пользователей.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request данные для регистрации
     * @return RegisterResponse информация о зарегистрированном пользователе
     */
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        return register(request);
    }

    /**
     * Выполняет регистрацию пользователя.
     *
     * @param request данные для регистрации
     * @return RegisterResponse информация о зарегистрированном пользователе
     * @throws DuplicateKeyException если пользователь с таким email уже существует
     */
    private RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateKeyException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .city(request.getCity())
                .role(UserRole.USER)
                .emailVerified(false)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .phone(savedUser.getPhone())
                .city(savedUser.getCity())
                .role(savedUser.getRole().name())
                .createdAt(savedUser.getCreatedAt())
                .message("Регистрация успешна")
                .build();
    }

    /**
     * Выполняет авторизацию пользователя.
     *
     * @param request данные для авторизации
     * @return AuthResponse JWT токен для доступа
     * @throws RuntimeException если email или пароль неверны
     */
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (!user.getIsActive()) {
                throw new RuntimeException("Аккаунт заблокирован. Обратитесь к администратору.");
            }

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtTokenProvider.generateToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name()
            );

            return AuthResponse.builder()
                    .token(token)
                    .build();

        } catch (AuthenticationException e) {
            throw new RuntimeException("Неверный email или пароль");
        }
    }

//    TODO
//    public void logout(String token) {}
}
