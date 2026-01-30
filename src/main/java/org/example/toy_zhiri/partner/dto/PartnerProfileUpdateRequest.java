package org.example.toy_zhiri.partner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления профиля партнера.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerProfileUpdateRequest {
    @Size(max = 255, message = "Название компании не должно превышать 255 символов")
    private String companyName;

    @Size(max = 5000, message = "Описание не должно превышать 5000 символов")
    private String description;

    private String address;

    @Size(max = 100, message = "Город не должен превышать 100 символов")
    private String city;

    @Size(max = 100, message = "Регион не должен превышать 100 символов")
    private String region;

    @Pattern(
            regexp = "^77\\d{9}$",
            message = "Неверный формат телефона. Ожидается формат: 77XXXXXXXXX"
    )
    private String phone;

    @Email(message = "Неверный формат email")
    private String email;

    @Pattern(
            regexp = "^77\\d{9}$",
            message = "Неверный формат WhatsApp. Ожидается формат: 77XXXXXXXXX"
    )
    private String whatsapp;

    @Size(max = 100, message = "Telegram не должен превышать 100 символов")
    private String telegram;

    @Size(max = 100, message = "Instagram не должен превышать 100 символов")
    private String instagram;

    @Size(max = 255, message = "Website не должен превышать 255 символов")
    private String website;

    private String logoUrl;
}