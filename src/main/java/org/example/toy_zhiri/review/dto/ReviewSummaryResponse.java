package org.example.toy_zhiri.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryResponse {
    /**
     * Средний рейтинг, например 4.6
     */
    private BigDecimal averageRating;
    /**
     * Общее количество видимых отзывов
     */
    private Long totalReviews;
}