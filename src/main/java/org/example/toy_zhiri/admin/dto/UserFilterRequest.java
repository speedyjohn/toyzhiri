package org.example.toy_zhiri.admin.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Параметры фильтрации и пагинации для списка пользователей.
 */
@Data
public class UserFilterRequest {

    @Parameter(description = "Номер страницы (начиная с 0)")
    private int page = 0;

    @Parameter(description = "Размер страницы")
    private int size = 20;

    @Parameter(description = "Поле для сортировки")
    private String sortBy = "createdAt";

    @Parameter(description = "Направление сортировки (ASC/DESC)")
    private Sort.Direction sortDirection = Sort.Direction.DESC;

    @Parameter(description = "Фильтр по роли")
    private String role;

    @Parameter(description = "Поиск по email, имени")
    private String search;

    @Parameter(description = "Фильтр по верификации email")
    private Boolean emailVerified;

    public Pageable toPageable() {
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }
}
