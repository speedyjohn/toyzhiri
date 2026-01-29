package org.example.toy_zhiri.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    @NotBlank(message = "Название на русском обязательно")
    @Size(max = 100)
    private String nameRu;

    @NotBlank(message = "Название на казахском обязательно")
    @Size(max = 100)
    private String nameKz;

    @NotBlank(message = "Slug обязателен")
    @Size(max = 100)
    private String slug;

    private String description;
    private String icon;
    private Integer displayOrder;
}