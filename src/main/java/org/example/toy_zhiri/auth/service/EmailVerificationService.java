package org.example.toy_zhiri.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.auth.entity.EmailVerificationToken;
import org.example.toy_zhiri.auth.repository.EmailVerificationTokenRepository;
import org.example.toy_zhiri.exception.BadRequestException;
import org.example.toy_zhiri.exception.ConflictException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.notification.service.EmailNotificationService;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для управления подтверждением email.
 * Отвечает за генерацию токенов верификации, отправку писем,
 * валидацию токенов при переходе по ссылке и повторную отправку писем.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    @Value("${app.email-verification.token-ttl-hours:24}")
    private long tokenTtlHours;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Создаёт новый токен подтверждения для пользователя и отправляет письмо со ссылкой.
     * Перед созданием удаляет все предыдущие токены пользователя.
     *
     * @param user пользователь, которому нужно подтвердить email
     */
    @Transactional
    public void sendVerificationEmail(User user) {
        // Удаляем старые токены пользователя, чтобы активной была только одна ссылка
        tokenRepository.deleteByUserId(user.getId());

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusHours(tokenTtlHours))
                .build();

        tokenRepository.save(token);

        String verificationLink = frontendUrl + "/verify-email?token=" + token.getToken();
        String message = "Здравствуйте, " + user.getFirstName() + "!<br><br>" +
                "Благодарим за регистрацию на платформе Toy Zhiri. " +
                "Чтобы завершить регистрацию, пожалуйста, подтвердите ваш email, перейдя по ссылке:<br><br>" +
                "<a href=\"" + verificationLink + "\">Подтвердить email</a><br><br>" +
                "Ссылка действительна в течение " + tokenTtlHours + " часов.<br>" +
                "Если вы не регистрировались на Toy Zhiri, просто проигнорируйте это письмо.";

        emailNotificationService.send(user.getEmail(), "Подтверждение email", message);

        log.info("Токен подтверждения email создан для пользователя {}", user.getEmail());
    }

    /**
     * Подтверждает email пользователя по токену.
     * При успешной верификации помечает пользователя как emailVerified=true
     * и удаляет использованный токен.
     *
     * @param token значение токена из ссылки
     * @throws NotFoundException   если токен не найден
     * @throws BadRequestException если токен истёк
     * @throws ConflictException   если email уже подтверждён
     */
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Токен подтверждения не найден"));

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new BadRequestException(
                    "Токен подтверждения истёк. Запросите повторную отправку письма");
        }

        User user = verificationToken.getUser();

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            tokenRepository.delete(verificationToken);
            throw new ConflictException("Email уже подтверждён");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        log.info("Email успешно подтверждён для пользователя {}", user.getEmail());
    }

    /**
     * Повторно отправляет письмо с подтверждением email.
     *
     * @param email email пользователя
     * @throws NotFoundException если пользователь не найден
     * @throws ConflictException если email уже подтверждён
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с email " + email + " не найден"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ConflictException("Email уже подтверждён");
        }

        sendVerificationEmail(user);
    }

    /**
     * Периодическая очистка истекших токенов подтверждения.
     * Выполняется каждый час.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Удалено {} истекших токенов подтверждения email", deleted);
        }
    }
}