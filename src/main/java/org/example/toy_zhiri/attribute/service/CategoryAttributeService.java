package org.example.toy_zhiri.attribute.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.attribute.dto.AttributeDefinitionResponse;
import org.example.toy_zhiri.attribute.dto.CategoryAttributeResponse;
import org.example.toy_zhiri.attribute.dto.CreateCategoryAttributeRequest;
import org.example.toy_zhiri.attribute.dto.UpdateCategoryAttributeRequest;
import org.example.toy_zhiri.attribute.entity.AttributeDefinition;
import org.example.toy_zhiri.attribute.entity.CategoryAttribute;
import org.example.toy_zhiri.attribute.repository.CategoryAttributeRepository;
import org.example.toy_zhiri.exception.ConflictException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.service.entity.ServiceCategory;
import org.example.toy_zhiri.service.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Сервис административного управления привязками атрибутов к категориям.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAttributeService {

    private final CategoryAttributeRepository categoryAttributeRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final AttributeDefinitionService attributeDefinitionService;

    /**
     * Возвращает все привязки атрибутов к категории.
     */
    public List<CategoryAttributeResponse> listByCategory(UUID categoryId) {
        ensureCategoryExists(categoryId);

        return categoryAttributeRepository
                .findByCategoryIdOrderBySortOrderAsc(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Создаёт привязку атрибута к категории.
     */
    @Transactional
    public CategoryAttributeResponse create(UUID categoryId, CreateCategoryAttributeRequest request) {
        ServiceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        "Категория с ID " + categoryId + " не найдена"));

        AttributeDefinition attribute = attributeDefinitionService.findByIdOrThrow(request.getAttributeId());

        if (categoryAttributeRepository.existsByCategoryIdAndAttributeId(categoryId, attribute.getId())) {
            throw new ConflictException(
                    "Атрибут '" + attribute.getKey() + "' уже привязан к этой категории");
        }

        CategoryAttribute binding = CategoryAttribute.builder()
                .category(category)
                .attribute(attribute)
                .isRequired(request.getIsRequired())
                .isFilterable(request.getIsFilterable())
                .sortOrder(request.getSortOrder())
                .build();

        CategoryAttribute saved = categoryAttributeRepository.save(binding);
        log.info("Создана привязка атрибута '{}' к категории '{}'",
                attribute.getKey(), category.getSlug());
        return mapToResponse(saved);
    }

    /**
     * Обновляет параметры существующей привязки (флаги, сортировку).
     */
    @Transactional
    public CategoryAttributeResponse update(UUID categoryId,
                                            UUID attributeId,
                                            UpdateCategoryAttributeRequest request) {
        CategoryAttribute binding = categoryAttributeRepository
                .findByCategoryIdAndAttributeId(categoryId, attributeId)
                .orElseThrow(() -> new NotFoundException(
                        "Привязка атрибута к категории не найдена"));

        binding.setIsRequired(request.getIsRequired());
        binding.setIsFilterable(request.getIsFilterable());
        binding.setSortOrder(request.getSortOrder());

        CategoryAttribute saved = categoryAttributeRepository.save(binding);
        log.info("Обновлена привязка атрибута '{}' к категории id={}",
                binding.getAttribute().getKey(), categoryId);
        return mapToResponse(saved);
    }

    /**
     * Удаляет привязку атрибута от категории.
     */
    @Transactional
    public void delete(UUID categoryId, UUID attributeId) {
        CategoryAttribute binding = categoryAttributeRepository
                .findByCategoryIdAndAttributeId(categoryId, attributeId)
                .orElseThrow(() -> new NotFoundException(
                        "Привязка атрибута к категории не найдена"));

        categoryAttributeRepository.delete(binding);
        log.info("Удалена привязка атрибута '{}' от категории id={}",
                binding.getAttribute().getKey(), categoryId);
    }

    private void ensureCategoryExists(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Категория с ID " + categoryId + " не найдена");
        }
    }

    private CategoryAttributeResponse mapToResponse(CategoryAttribute binding) {
        AttributeDefinition def = binding.getAttribute();
        AttributeDefinitionResponse attrResponse = AttributeDefinitionResponse.builder()
                .id(def.getId())
                .key(def.getKey())
                .type(def.getType().name())
                .matchStrategy(def.getMatchStrategy().name())
                .storageKeys(def.getStorageKeys())
                .labelRu(def.getLabelRu())
                .labelKk(def.getLabelKk())
                .unit(def.getUnit())
                .validationRules(def.getValidationRules())
                .createdAt(def.getCreatedAt())
                .updatedAt(def.getUpdatedAt())
                .build();

        return CategoryAttributeResponse.builder()
                .id(binding.getId())
                .categoryId(binding.getCategory().getId())
                .attribute(attrResponse)
                .isRequired(binding.getIsRequired())
                .isFilterable(binding.getIsFilterable())
                .sortOrder(binding.getSortOrder())
                .createdAt(binding.getCreatedAt())
                .build();
    }
}