package org.example.toy_zhiri.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Сервис отправки email-уведомлений через SMTP.
 * Отправка выполняется асинхронно, чтобы не блокировать основной поток.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@toyzhiri.kz}")
    private String fromAddress;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    /**
     * Отправляет email-уведомление.
     *
     * @param to      email получателя
     * @param title   тема письма
     * @param message текст сообщения
     */
    @Async
    public void send(String to, String title, String message) {
        if (!mailEnabled) {
            log.debug("Email-уведомления отключены. Пропускаем отправку на {}", to);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Toy Zhiri — " + title);
            helper.setText(buildHtmlContent(title, message), true);

            mailSender.send(mimeMessage);
            log.info("Email отправлен на {}: {}", to, title);

        } catch (MessagingException e) {
            log.error("Ошибка отправки email на {}: {}", to, e.getMessage());
        }
    }

    /**
     * Формирует простой HTML-шаблон письма.
     */
    private String buildHtmlContent(String title, String message) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; border-radius: 8px; padding: 30px;">
                        <h2 style="color: #333; margin-top: 0;">%s</h2>
                        <p style="color: #555; font-size: 16px; line-height: 1.5;">%s</p>
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">
                            Это автоматическое уведомление от платформы Toy Zhiri.
                            Вы можете управлять настройками уведомлений в личном кабинете.
                        </p>
                    </div>
                </div>
                """.formatted(title, message);
    }
}