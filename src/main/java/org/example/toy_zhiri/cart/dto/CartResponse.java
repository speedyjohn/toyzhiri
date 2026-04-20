package org.example.toy_zhiri.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.service.dto.ServiceResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO для отображения содержимого корзины пользователя.
 * Содержит список элементов, общее количество и итоговую сумму.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal totalPrice;

    /**
     * DTO для отображения отдельного элемента корзины.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {
        private String cartItemId;
        private ServiceResponse service;
        private Integer quantity;
        private LocalDate eventDate;
        private String notes;
        private BigDecimal itemTotal;
    }
}