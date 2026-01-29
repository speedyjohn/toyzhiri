package org.example.toy_zhiri.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {
    @NotNull(message = "Категория обязательна")
    private UUID categoryId;

    @NotBlank(message = "Название обязательно")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Краткое описание обязательно")
    @Size(max = 500)
    private String shortDescription;

    @Size(max = 5000)
    private String fullDescription;

    private BigDecimal priceFrom;
    private BigDecimal priceTo;

    @NotBlank(message = "Тип цены обязателен")
    private String priceType;

    @NotBlank(message = "Город обязателен")
    private String city;

    private String address;

    private String thumbnail;
    private List<String> imageUrls;
}