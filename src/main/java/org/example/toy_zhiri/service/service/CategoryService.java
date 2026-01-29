package org.example.toy_zhiri.service.service;

import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.service.dto.CategoryResponse;
import org.example.toy_zhiri.service.entity.ServiceCategory;
import org.example.toy_zhiri.service.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ServiceCategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(ServiceCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .nameRu(category.getNameRu())
                .nameKz(category.getNameKz())
                .slug(category.getSlug())
                .description(category.getDescription())
                .icon(category.getIcon())
                .build();
    }
}