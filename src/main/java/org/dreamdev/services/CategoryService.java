package org.dreamdev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.exceptions.EmptyFileException;
import org.dreamdev.exceptions.NotFoundException;
import org.dreamdev.exceptions.PermissionNotFoundException;
import org.dreamdev.models.Category;
import org.dreamdev.models.Electorate;
import org.dreamdev.models.Permission;
import org.dreamdev.repositories.CategoryRepository;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.utils.HelperClass;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ElectorateRepository electorateRepository;

    public String uploadCategories(MultipartFile file, String electorateId) {

        validateElectorate(electorateId);

        if (file.isEmpty()) throw new EmptyFileException("File is empty");

        List<String[]> rows = HelperClass.readCSVFiles(file);
        List<Category> categories = getCategoryList(rows);
        categories.forEach(this::saveCategory);

        log.info("{} categories uploaded successfully", categories.size());
        return categories.size() + " categories uploaded successfully";
    }

    // Validate that electorate exists and has permission
    private void validateElectorate(String electorateId) {
        Optional<Electorate> electorate = electorateRepository.findByElectorateId(electorateId);

        if (electorate.isEmpty()) throw new NotFoundException("Electorate with this Id not found");

        if (!HelperClass.hasPermission(electorate.get().getPermissions(), Permission.CAN_UPLOAD_FILE)) {
            throw new PermissionNotFoundException("Electorate does not have permission to approve categories");
        }
    }

    // Convert CSV rows into Category objects
    private List<Category> getCategoryList(List<String[]> rows) {
        List<Category> categories = new ArrayList<>();
        for (String[] row : rows) {
            Category category = Category.builder()
                    .categoryId(row[0])
                    .type(row[1])
                    .createdAt(LocalDateTime.now())
                    .build();
            categories.add(category);
        }
        return categories;
    }

    private void saveCategory(Category category) {
        categoryRepository.save(category);
        log.info("Saved category: {}", category.getCategoryId());
    }
}