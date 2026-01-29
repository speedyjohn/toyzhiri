package org.example.toy_zhiri.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.file.dto.FileUploadResponse;
import org.example.toy_zhiri.file.service.FileUploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "API для загрузки изображений")
public class FileUploadController {
    private final FileUploadService fileUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Загрузить одно изображение",
            description = "Загружает одно изображение и возвращает URL. Максимальный размер: 5MB. Форматы: JPG, PNG, WEBP, GIF",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<FileUploadResponse> uploadSingleImage(
            @RequestParam("file")
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            MultipartFile file) {

        String url = fileUploadService.uploadImage(file);

        return ResponseEntity.ok(FileUploadResponse.builder()
                .url(url)
                .message("Файл успешно загружен")
                .build());
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Загрузить несколько изображений",
            description = "Загружает несколько изображений и возвращает массив URL",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<FileUploadResponse> uploadMultipleImages(
            @RequestParam("files")
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            List<MultipartFile> files) {

        List<String> urls = fileUploadService.uploadImages(files);

        return ResponseEntity.ok(FileUploadResponse.builder()
                .urls(urls)
                .message("Файлы успешно загружены")
                .build());
    }
}