package org.example.toy_zhiri.service.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO для фильтрации услуг.
 * Поддерживает комбинацию всех параметров фильтрации.
 *
 * Используется с @ModelAttribute в контроллере,
 * что позволяет Spring автоматически биндить query parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFilterRequest {

    @Parameter(description = "ID категории")
    private UUID categoryId;

    @Parameter(description = "Минимальная цена")
    private BigDecimal priceMin;

    @Parameter(description = "Максимальная цена")
    private BigDecimal priceMax;

    @Parameter(description = "Минимальный рейтинг (например, 4.0 = от 4 звёзд)")
    private BigDecimal ratingMin;

    @Parameter(description = "Город")
    private String city;

    @Parameter(description = "Список городов (можно указать несколько)")
    private List<String> cities;

    @Parameter(description = "Тип услуги (поиск в описании, например: 'свадебный банкет', 'корпоратив')")
    private String serviceType;

    @Parameter(description = "Доступная дата (формат: YYYY-MM-DD)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate availableDate;

    @Parameter(description = "Список доступных дат")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> availableDates;

    @Parameter(description = "Поиск по ключевым словам (в названии и описании)")
    private String searchQuery;

    @Parameter(description = "Показывать только услуги с фотографиями")
    private Boolean hasImages;

    @Parameter(description = "Минимальное количество отзывов")
    private Integer minReviews;
}