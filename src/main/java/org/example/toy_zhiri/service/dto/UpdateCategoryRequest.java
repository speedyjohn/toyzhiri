package org.example.toy_zhiri.service.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    @Size(max = 100)
    private String nameRu;

    @Size(max = 100)
    private String nameKz;

    @Size(max = 100)
    private String slug;

    private String description;
    private String icon;
    private Integer displayOrder;
    private Boolean isActive;
}