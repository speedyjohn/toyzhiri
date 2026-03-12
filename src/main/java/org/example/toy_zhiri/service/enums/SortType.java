package org.example.toy_zhiri.service.enums;

/**
 * Типы сортировки каталога услуг.
 */
public enum SortType {
    POPULARITY,  // По популярности: bookings_count DESC, views_count DESC (по умолчанию)
    PRICE_ASC,   // По цене: от дешёвых к дорогим
    PRICE_DESC,  // По цене: от дорогих к дешёвым
    RATING       // По рейтингу: rating DESC, reviews_count DESC
}