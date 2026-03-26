package org.example.toy_zhiri.review.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "ID бронирования обязателен")
    private UUID bookingId;

    @NotNull(message = "Оценка обязательна")
    @Min(value = 1, message = "Минимальная оценка — 1")
    @Max(value = 5, message = "Максимальная оценка — 5")
    private Short rating;

    @NotBlank(message = "Комментарий обязателен")
    @Size(min = 20, max = 1000, message = "Комментарий должен быть от 20 до 1000 символов")
    private String comment;

    // URL-ы изображений, загруженных заранее через /api/v1/files/upload
    @Size(max = 10, message = "Можно прикрепить не более 10 фотографий")
    private List<String> imageUrls;
}