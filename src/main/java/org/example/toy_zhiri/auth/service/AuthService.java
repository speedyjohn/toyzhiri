package org.example.toy_zhiri.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.auth.dto.*;
import org.example.toy_zhiri.auth.entity.RefreshToken;
import org.example.toy_zhiri.auth.security.JwtTokenProvider;
import org.example.toy_zhiri.exception.AuthException;
import org.example.toy_zhiri.exception.BadRequestException;
import org.example.toy_zhiri.exception.ConflictException;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.enums.UserRole;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final LoginHistoryService loginHistoryService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

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
     * @throws ConflictException если пользователь с таким email уже существует
     */
    private RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
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

        emailVerificationService.sendVerificationEmail(savedUser);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .phone(savedUser.getPhone())
                .city(savedUser.getCity())
                .role(savedUser.getRole().name())
                .createdAt(savedUser.getCreatedAt())
                .message("Регистрация успешна. На ваш email отправлено письмо с подтверждением. " +
                        "Подтвердите email, чтобы войти в систему")
                .build();
    }

    /**
     * Выполняет авторизацию пользователя.
     * Возвращает access токен и refresh токен.
     *
     * @param request     данные для авторизации
     * @param httpRequest HTTP запрос для логирования
     * @return AuthResponse access и refresh токены
     * @throws AuthException если email или пароль неверны, или аккаунт заблокирован
     */
    public AuthResponse login(AuthRequest request, HttpServletRequest httpRequest) {
        User user = null;

        try {
            user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);

            if (user == null) {
                User tempUser = User.builder()
                        .email(request.getEmail())
                        .firstName("Unknown")
                        .lastName("User")
                        .build();
                loginHistoryService.logLogin(tempUser, false, "Пользователь не найден", httpRequest);
                throw new AuthException("Неверный email или пароль");
            }

            if (!user.getIsActive()) {
                loginHistoryService.logLogin(user, false, "Аккаунт заблокирован", httpRequest);
                throw new AuthException("Аккаунт заблокирован. Обратитесь к администратору.");
            }

            if (!user.getEmailVerified()) {
                loginHistoryService.logLogin(user, false, "Email не подтверждён", httpRequest);
                throw new AuthException(
                        "Email не подтверждён. Проверьте почту или запросите повторную отправку письма");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            loginHistoryService.logLogin(user, true, null, httpRequest);

            String accessToken = jwtTokenProvider.generateToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name()
            );

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .build();

        } catch (AuthenticationException e) {
            if (user != null) {
                loginHistoryService.logLogin(user, false, "Неверный пароль", httpRequest);
            }
            throw new AuthException("Неверный email или пароль");
        }
    }

    /**
     * Обновляет access токен по refresh токену.
     * Реализует ротацию: старый refresh токен заменяется новым.
     *
     * @param request запрос с refresh токеном
     * @return AuthResponse новая пара access + refresh токенов
     * @throws AuthException если refresh токен невалиден или истёк
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthException("Refresh токен не найден"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    /**
     * Выход из системы (отзыв токена).
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param httpRequest HTTP запрос для извлечения токена
     * @return LogoutResponse сообщение об успешном выходе
     */
    public LogoutResponse logout(UserDetails userDetails, HttpServletRequest httpRequest) {
        String header = httpRequest.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            tokenBlacklistService.blacklistToken(token);

            User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
            refreshTokenService.deleteByUserId(user.getId());

            loginHistoryService.logLogout(user, httpRequest);

            return LogoutResponse.builder()
                    .token(token)
                    .build();
        }

        throw new BadRequestException("Токен не предоставлен");
    }
}