package org.dreamdev.service;

import org.dreamdev.models.Candidate;
import org.dreamdev.models.Electorate;
import org.dreamdev.models.Permission;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.repositories.CandidateRepository;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.services.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CandidateServiceTest {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ElectorateRepository electorateRepository;

    private MockMultipartFile validCsvFile;
    private MockMultipartFile emptyCsvFile;

    private final String ELECTORATE_ID = "ELECTORATE-001";

    @BeforeEach
    public void setUp() {
        // Clear previous data
        candidateRepository.deleteAll();
        electorateRepository.deleteAll();

        // Seed the electorate for this test
        Electorate electorate = Electorate.builder()
                .electorateId(ELECTORATE_ID)
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
                candidateId,numberOfVote,categoryId,lastName,firstName,dateOfBirth,citizenship
                ELECT2024-CAND-001,120,CAT-A,Okafor,Chinedu,1995-04-12,REGISTRATION
                ELECT2024-CAND-002,85,CAT-B,Adebayo,Tolu,1998-09-23,REGISTRATION
                ELECT2024-CAND-003,230,CAT-C,Balogun,Ifeoma,1992-01-30,NATURALIZATION
                ELECT2024-CAND-004,150,CAT-D,Ogunleye,Kemi,1996-07-15,REGISTRATION
                ELECT2024-CAND-005,60,CAT-E,Musa,Abdullahi,2000-11-05,DUAL_CITIZENSHIP
                """;

        validCsvFile = new MockMultipartFile(
                "file",
                "candidates.csv",
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
    public void upload_candidates_from_csv_successfully() {
        assertEquals(0L, candidateRepository.count());

        String result = candidateService.uploadCandidate(validCsvFile, ELECTORATE_ID);

        assertEquals(5L, candidateRepository.count());
        assertEquals("5 candidates uploaded successfully", result);
    }

    @Test
    public void upload_candidates_saves_correct_data() {
        candidateService.uploadCandidate(validCsvFile, ELECTORATE_ID);

        Candidate candidate = candidateRepository.findByCandidateId("ELECT2024-CAND-001").get();

        assertNotNull(candidate);
        assertEquals("Chinedu", candidate.getFirstName());
        assertEquals("Okafor", candidate.getLastName());
        assertEquals("CAT-A", candidate.getCategoryId());
        assertEquals(120, candidate.getNumberOfVote());
    }

    @Test
    public void upload_empty_file_throws_exception() {
        assertThrows(RuntimeException.class, () -> candidateService.uploadCandidate(emptyCsvFile, ELECTORATE_ID));
        assertEquals(0L, candidateRepository.count());
    }
}