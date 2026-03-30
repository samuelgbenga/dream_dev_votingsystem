package org.dreamdev.service;

import org.dreamdev.models.Category;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.models.Electorate;
import org.dreamdev.models.Permission;
import org.dreamdev.repositories.CategoryRepository;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ElectorateRepository electorateRepository;

    private MockMultipartFile validCsvFile;
    private MockMultipartFile emptyCsvFile;

    private final String electorateId = "ELECTORATE-001";

    @BeforeEach
    public void setUp() {
        // Clear previous data
        categoryRepository.deleteAll();
        electorateRepository.deleteAll();

        // Seed an electorate with permissions
        Electorate electorate = Electorate.builder()
                .electorateId(electorateId)
                .firstName("Samuel")
                .lastName("Joseph")
                .dateOfBirth("1990-05-15")
                .citizenship(CitizenshipType.NATURALIZATION)
                .permissions(List.of(
                        Permission.CAN_UPLOAD_FILE,
                        Permission.CAN_APPROVE_VOTER
                ))
                .build();
        electorateRepository.save(electorate);

        // Prepare CSV files
        String csvContent = """
                categoryId, type
                CAT-001,Presidential
                CAT-002,Senatorial
                CAT-003,House of Representatives
                """;

        validCsvFile = new MockMultipartFile(
                "file",
                "categories.csv",
                "text/csv",
                csvContent.getBytes()
        );

        emptyCsvFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );
    }

    @Test
    public void upload_categories_from_csv_successfully() {
        assertEquals(0L, categoryRepository.count());

        String result = categoryService.uploadCategories(validCsvFile, electorateId);

        assertEquals(3L, categoryRepository.count());
        assertEquals("3 categories uploaded successfully", result);
    }

    @Test
    public void upload_categories_saves_correct_data() {
        categoryService.uploadCategories(validCsvFile, electorateId);

        Category category = categoryRepository.findAll().stream()
                .filter(c -> "CAT-001".equals(c.getCategoryId()))
                .findFirst()
                .orElse(null);

        assertNotNull(category);
        assertEquals("Presidential", category.getType());
    }

    @Test
    public void upload_empty_file_throws_exception() {
        assertThrows(RuntimeException.class, () -> categoryService.uploadCategories(emptyCsvFile, electorateId));
        assertEquals(0L, categoryRepository.count());
    }
}