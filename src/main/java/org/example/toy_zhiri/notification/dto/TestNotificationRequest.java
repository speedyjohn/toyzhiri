package org.example.toy_zhiri.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestNotificationRequest {

    @NotBlank(message = "Заголовок обязателен")
    private String title;

    @NotBlank(message = "Текст сообщения обязателен")
    private String message;

    /**
     * Email получателя. Если не указан — используется email текущего пользователя
     */
    @Email(message = "Некорректный формат email")
    private String recipientEmail;

    /**
     * Отправить in-app уведомление
     */
    @NotNull
    private Boolean push = true;

    /**
     * Отправить email
     */
    @NotNull
    private Boolean email = true;

    /**
     * Отправить SMS (лог)
     */
    @NotNull
    private Boolean sms = true;
}