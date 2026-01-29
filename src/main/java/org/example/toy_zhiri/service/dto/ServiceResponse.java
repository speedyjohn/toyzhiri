package org.example.toy_zhiri.service.dto;

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
public class ServiceResponse {
    private UUID id;
    private UUID partnerId;
    private String partnerName;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String slug;
    private String shortDescription;
    private String fullDescription;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private String priceType;
    private String city;
    private String address;
    private BigDecimal rating;
    private Integer reviewsCount;
    private Integer viewsCount;
    private String thumbnail;
    private List<String> images;
    private Boolean isFavorite;
    private Boolean inCart;
}