package org.example.toy_zhiri.story.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.payment.enums.PaymentMethod;
import org.example.toy_zhiri.story.enums.StoryMediaType;

import java.util.UUID;

/**
 * Запрос на создание сторис.
 * Создаёт сторис вместе с моковой оплатой — после успешной оплаты
 * сторис сразу становится активной на 24 часа.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoryRequest {

    @NotNull(message = "ID услуги обязателен")
    private UUID serviceId;

    @NotBlank(message = "URL медиафайла обязателен")
    @Size(max = 500, message = "URL не должен превышать 500 символов")
    private String mediaUrl;

    @NotNull(message = "Тип медиа обязателен")
    private StoryMediaType mediaType;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String caption;

    @NotNull(message = "Метод оплаты обязателен")
    private PaymentMethod paymentMethod;
}