package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.service.dto.CategoryResponse;
import org.example.toy_zhiri.service.dto.CreateCategoryRequest;
import org.example.toy_zhiri.service.dto.UpdateCategoryRequest;
import org.example.toy_zhiri.service.service.AdminCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Categories", description = "Управление категориями услуг (только для админов)")
public class AdminCategoryController {
    private final AdminCategoryService adminCategoryService;

    @PostMapping
    @Operation(
            summary = "Создать категорию",
            description = "Создание новой категории услуг",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(201).body(adminCategoryService.createCategory(request));
    }

    @PutMapping("/{categoryId}")
    @Operation(
            summary = "Редактировать категорию",
            description = "Обновление существующей категории",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(adminCategoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(
            summary = "Удалить категорию",
            description = "Удаление категории (будьте осторожны, удаляются и все связанные услуги)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> deleteCategory(
            @PathVariable UUID categoryId) {
        return ResponseEntity.ok(adminCategoryService.deleteCategory(categoryId));
    }

    @GetMapping
    @Operation(
            summary = "Все категории",
            description = "Получить список всех категорий (включая неактивные)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(adminCategoryService.getAllCategories());
    }

    @GetMapping("/{categoryId}")
    @Operation(
            summary = "Получить категорию по ID",
            description = "Детальная информация о категории",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable UUID categoryId) {
        return ResponseEntity.ok(adminCategoryService.getCategoryById(categoryId));
    }
}