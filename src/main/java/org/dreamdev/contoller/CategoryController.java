package org.dreamdev.contoller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.services.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCategories(
            @RequestParam("file") MultipartFile file,
            @RequestParam("electorateId") String electorateId) {

        try {
            String result = categoryService.uploadCategories(file, electorateId);
            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            log.error("Error uploading categories: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to upload categories: " + ex.getMessage());
        }
    }
}