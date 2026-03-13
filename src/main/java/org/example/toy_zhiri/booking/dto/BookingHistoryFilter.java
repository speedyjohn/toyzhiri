package org.example.toy_zhiri.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.booking.enums.BookingStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryFilter {

    /**
     * Фильтр по статусу бронирования.
     */
    private BookingStatus status;

    /**
     * Фильтр по категории услуги (ID категории).
     */
    private UUID categoryId;

    /**
     * Фильтр по дате создания заказа — от.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdFrom;

    /**
     * Фильтр по дате создания заказа — до.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdTo;

    /**
     * Фильтр по дате мероприятия — от.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventFrom;

    /**
     * Фильтр по дате мероприятия — до.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventTo;
}