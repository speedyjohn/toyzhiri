package org.example.toy_zhiri.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.toy_zhiri.service.enums.SortType;

import java.util.List;

/**
 * Обёртка для страницы с услугами.
 * Содержит данные пагинации и применённый тип сортировки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePageResponse {
    private List<ServiceResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private SortType appliedSortType;
}