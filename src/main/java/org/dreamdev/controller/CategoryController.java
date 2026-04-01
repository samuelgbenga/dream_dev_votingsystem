package org.dreamdev.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.dto.responses.CategoryResponse;
import org.dreamdev.services.CategoryService;
import org.dreamdev.utils.HelperClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCategories(
            @RequestParam("file") MultipartFile file,
            @RequestParam("electorateId") String electorateId) {

        Optional<ResponseEntity<String>> UNSUPPORTED_MEDIA_TYPE = HelperClass.getStringResponseEntity(file);
        if (UNSUPPORTED_MEDIA_TYPE.isPresent()) return UNSUPPORTED_MEDIA_TYPE.get();
        try {
            String result = categoryService.uploadCategories(file, electorateId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error uploading categories: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to upload categories: " + ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        try {
            List<CategoryResponse> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception ex) {
            log.error("Error fetching categories: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}