package org.example.toy_zhiri.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.payment.enums.PaymentMethod;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "ID подписки обязателен")
    private UUID subscriptionId;

    @NotNull(message = "Метод оплаты обязателен")
    private PaymentMethod paymentMethod;
}