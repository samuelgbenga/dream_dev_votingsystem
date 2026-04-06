package org.dreamdev.service;

import org.dreamdev.exceptions.EmptyFileException;
import org.dreamdev.exceptions.InvalidFileException;
import org.dreamdev.models.Candidate;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.models.Electorate;
import org.dreamdev.models.Permission;
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
    private MockMultipartFile wrongHeadersCsvFile;

    private final String ELECTORATE_ID = "ELECTORATE-001";

    // Updated CSV headers to match current service: candidateId,lastName,firstName,dateOfBirth,citizenship
    // (numberOfVote and categoryId were removed from upload CSV)
    private final String VALID_CSV = """
            candidateId,lastName,firstName,dateOfBirth,citizenship
            ELECTGUB2026-CAND-001,Okafor,Chinedu,1995-04-12,NATURALIZATION
            ELECTGUB2026-CAND-002,Adebayo,Tolu,1998-09-23,REGISTRATION
            ELECTGUB2026-CAND-003,Balogun,Ifeoma,1992-01-30,NATURALIZATION
            ELECTGUB2026-CAND-004,Ogunleye,Kemi,1996-07-15,REGISTRATION
            ELECTGUB2026-CAND-005,Musa,Abdullahi,2000-11-05,DUAL_CITIZENSHIP
            """;

    @BeforeEach
    public void setUp() {
        candidateRepository.deleteAll();
        electorateRepository.deleteAll();

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

        validCsvFile = new MockMultipartFile(
                "file", "candidates.csv", "text/csv", VALID_CSV.getBytes()
        );

        emptyCsvFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]
        );

        // Wrong headers — simulates uploading elections CSV to candidate endpoint
        String wrongCsv = """
                electionId,category,state,date,startTime,stopTime
                ELECTGUB2026,GUBERNATORIAL,KOGI,2026-04-05,00:00,23:55
                """;
        wrongHeadersCsvFile = new MockMultipartFile(
                "file", "wrong.csv", "text/csv", wrongCsv.getBytes()
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

        Candidate candidate = candidateRepository.findByCandidateId("ELECTGUB2026-CAND-001").orElseThrow();

        assertNotNull(candidate);
        assertEquals("Chinedu", candidate.getFirstName());
        assertEquals("Okafor", candidate.getLastName());
        assertEquals("1995-04-12", candidate.getDateOfBirth());
        assertEquals(CitizenshipType.NATURALIZATION, candidate.getCitizenship());
    }

    @Test
    public void upload_empty_file_throws_exception() {
        assertThrows(EmptyFileException.class,
                () -> candidateService.uploadCandidate(emptyCsvFile, ELECTORATE_ID));
        assertEquals(0L, candidateRepository.count());
    }

    @Test
    public void upload_wrong_csv_headers_throws_invalid_file_exception() {
        assertThrows(InvalidFileException.class,
                () -> candidateService.uploadCandidate(wrongHeadersCsvFile, ELECTORATE_ID));
        assertEquals(0L, candidateRepository.count());
    }

    @Test
    public void upload_duplicate_candidates_are_skipped() {
        // First upload — all 5 saved
        candidateService.uploadCandidate(validCsvFile, ELECTORATE_ID);
        assertEquals(5L, candidateRepository.count());

        // Second upload — all duplicates, none should be added
        String result = candidateService.uploadCandidate(validCsvFile, ELECTORATE_ID);
        assertEquals(5L, candidateRepository.count()); // still 5
        assertTrue(result.contains("0 candidates uploaded successfully"));
        assertTrue(result.contains("5 skipped"));
    }

    @Test
    public void upload_partial_duplicates_only_saves_new_ones() {
        // Save first 3 manually
        String partialCsv = """
                candidateId,lastName,firstName,dateOfBirth,citizenship
                ELECTGUB2026-CAND-001,Okafor,Chinedu,1995-04-12,NATURALIZATION
                ELECTGUB2026-CAND-002,Adebayo,Tolu,1998-09-23,REGISTRATION
                ELECTGUB2026-CAND-003,Balogun,Ifeoma,1992-01-30,NATURALIZATION
                """;
        MockMultipartFile partialFile = new MockMultipartFile(
                "file", "partial.csv", "text/csv", partialCsv.getBytes()
        );
        candidateService.uploadCandidate(partialFile, ELECTORATE_ID);
        assertEquals(3L, candidateRepository.count());

        // Upload full 5 — only 2 new should be saved
        String result = candidateService.uploadCandidate(validCsvFile, ELECTORATE_ID);
        assertEquals(5L, candidateRepository.count());
        assertTrue(result.contains("2 candidates uploaded successfully"));
        assertTrue(result.contains("3 skipped"));
    }

    @Test
    public void upload_candidates_with_invalid_electorate_throws_exception() {
        assertThrows(RuntimeException.class,
                () -> candidateService.uploadCandidate(validCsvFile, "INVALID-ID"));
    }
}