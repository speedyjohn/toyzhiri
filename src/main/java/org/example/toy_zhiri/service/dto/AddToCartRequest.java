package org.example.toy_zhiri.service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    @NotNull(message = "ID услуги обязателен")
    private UUID serviceId;

    @Min(value = 1, message = "Количество должно быть минимум 1")
    private Integer quantity = 1;

    @Future(message = "Дата мероприятия должна быть в будущем")
    private LocalDate eventDate;

    private String notes;
}