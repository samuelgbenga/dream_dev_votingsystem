package org.dreamdev.service;

import org.dreamdev.models.Candidate;
import org.dreamdev.repositories.CandidateRepository;
import org.dreamdev.services.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CandidateServiceTest {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private CandidateRepository candidateRepository;

    private MockMultipartFile validCsvFile;
    private MockMultipartFile emptyCsvFile;

    @BeforeEach
    public void setUp() {
        candidateRepository.deleteAll();

        String csvContent = """
                candidateId,numberOfVote,categoryId,lastName,firstName,dateOfBirth,citizenship
                ELECT2024-CAND-001,120,CAT-A,Okafor,Chinedu,1995-04-12,JUS_SANGUINIS
                ELECT2024-CAND-002,85,CAT-B,Adebayo,Tolu,1998-09-23,JUS_SOLI
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

        String result = candidateService.uploadCandidate(validCsvFile);

        assertEquals(5L, candidateRepository.count());
        assertEquals("5 candidates uploaded successfully", result);
    }

    @Test
    public void upload_candidates_saves_correct_data() {
        candidateService.uploadCandidate(validCsvFile);

        Candidate candidate = candidateRepository.findByCandidateId("ELECT2024-CAND-001");

        assertNotNull(candidate);
        assertEquals("Chinedu", candidate.getFirstName());
        assertEquals("Okafor", candidate.getLastName());
        assertEquals("CAT-A", candidate.getCategoryId());
        assertEquals(120, candidate.getNumberOfVote());
    }

    @Test
    public void upload_empty_file_throws_exception() {
        assertThrows(RuntimeException.class, () -> candidateService.uploadCandidate(emptyCsvFile));
        assertEquals(0L, candidateRepository.count());
    }
}