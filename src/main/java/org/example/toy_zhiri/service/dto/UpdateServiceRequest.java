package org.example.toy_zhiri.service.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceRequest {
    @Size(max = 255)
    private String name;

    @Size(max = 500)
    private String shortDescription;

    @Size(max = 5000)
    private String fullDescription;

    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private String priceType;
    private String city;
    private String address;
    private String thumbnail;
    private List<String> imageUrls;
    private Boolean isActive;
}