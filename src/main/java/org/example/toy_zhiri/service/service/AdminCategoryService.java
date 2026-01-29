package org.example.toy_zhiri.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.service.dto.CategoryResponse;
import org.example.toy_zhiri.service.dto.CreateCategoryRequest;
import org.example.toy_zhiri.service.dto.UpdateCategoryRequest;
import org.example.toy_zhiri.service.entity.ServiceCategory;
import org.example.toy_zhiri.service.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {
    private final ServiceCategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new RuntimeException("Категория с таким slug уже существует");
        }

        ServiceCategory category = ServiceCategory.builder()
                .nameRu(request.getNameRu())
                .nameKz(request.getNameKz())
                .slug(request.getSlug())
                .description(request.getDescription())
                .icon(request.getIcon())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .build();

        ServiceCategory saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request) {
        ServiceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug())) {
            if (categoryRepository.findBySlug(request.getSlug()).isPresent()) {
                throw new RuntimeException("Категория с таким slug уже существует");
            }
            category.setSlug(request.getSlug());
        }

        if (request.getNameRu() != null) category.setNameRu(request.getNameRu());
        if (request.getNameKz() != null) category.setNameKz(request.getNameKz());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());

        ServiceCategory updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Transactional
    public MessageResponse deleteCategory(UUID categoryId) {
        ServiceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        categoryRepository.delete(category);

        return MessageResponse.builder()
                .message("Категория успешно удалена")
                .build();
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(UUID categoryId) {
        ServiceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        return mapToResponse(category);
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