package org.example.toy_zhiri.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetAvailabilityRequest {

    @NotNull(message = "Список дат обязателен")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> dates;

    @NotNull(message = "Статус обязателен")
    private String status; // AVAILABLE или BLOCKED

    private String note;
}