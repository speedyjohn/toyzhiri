package org.example.toy_zhiri.admin.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.example.toy_zhiri.partner.enums.PartnerStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


/**
 * DTO для фильтрации и пагинации для списка партнеров.
 */
@Data
public class PartnerFilterRequest {

    @Parameter(description = "Номер страницы")
    private int page = 0;

    @Parameter(description = "Размер страницы")
    private int size = 20;

    @Parameter(description = "Поле сортировки")
    private String sortBy = "createdAt";

    @Parameter(description = "Направление сортировки")
    private Sort.Direction sortDirection = Sort.Direction.DESC;

    @Parameter(description = "Фильтр по городу")
    private String city;

    @Parameter(description = "Фильтр по статусу")
    private PartnerStatus status;

    @Parameter(description = "Поиск по названию компании")
    private String search;

    public Pageable toPageable() {
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }
}