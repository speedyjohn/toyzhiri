package org.example.toy_zhiri.partner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса регистрации в качестве партнера.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRegistrationRequest {
    @NotBlank(message = "БИН обязателен для заполнения")
    @Size(min = 12, max = 12, message = "БИН должен содержать 12 цифр")
    @Pattern(regexp = "\\d{12}", message = "БИН должен содержать только цифры")
    private String bin;
}