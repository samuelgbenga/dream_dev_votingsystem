package org.dreamdev.service;

import org.dreamdev.exceptions.EmptyFileException;
import org.dreamdev.exceptions.InvalidFileException;
import org.dreamdev.models.*;
import org.dreamdev.repositories.ElectionRepository;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.services.ElectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ElectionServiceTest {

    @Autowired
    private ElectionService electionService;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private ElectorateRepository electorateRepository;

    private MockMultipartFile validCsvFile;
    private MockMultipartFile emptyCsvFile;
    private MockMultipartFile wrongHeadersCsvFile;

    private final String ELECTORATE_ID = "ELECTORATE-001";

    // Updated CSV format: electionId,category,state,date,startTime,stopTime
    // ELECTGUB2026 is today (2026-04-05) so it's within valid voting range
    private final String VALID_CSV = """
            electionId,category,state,date,startTime,stopTime
            ELECTPRES2026,PRESIDENTIAL,NATIONAL,2026-04-15,08:00,18:00
            ELECTSEN2026,SENATORIAL,LAGOS,2026-04-20,09:00,17:00
            ELECTGUB2026,GUBERNATORIAL,KOGI,2026-04-06,00:00,23:55
            """;

    @BeforeEach
    public void setUp() {
        electionRepository.deleteAll();
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
                "file", "elections.csv", "text/csv", VALID_CSV.getBytes()
        );

        emptyCsvFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]
        );

        // Wrong headers — simulates uploading candidate CSV to election endpoint
        String wrongCsv = """
                candidateId,lastName,firstName,dateOfBirth,citizenship
                ELECTGUB2026-CAND-001,Okafor,Chinedu,1995-04-12,NATURALIZATION
                """;
        wrongHeadersCsvFile = new MockMultipartFile(
                "file", "wrong.csv", "text/csv", wrongCsv.getBytes()
        );
    }

    @Test
    public void upload_elections_from_csv_successfully() {
        assertEquals(0L, electionRepository.count());

        String result = electionService.uploadElections(validCsvFile, ELECTORATE_ID);

        assertEquals(3L, electionRepository.count());
        assertEquals("3 elections uploaded successfully", result);
    }

    @Test
    public void upload_elections_saves_correct_data() {
        electionService.uploadElections(validCsvFile, ELECTORATE_ID);

        Election election = electionRepository.findByElectionId("ELECTGUB2026").orElseThrow();

        assertNotNull(election);
        assertEquals(Category.GUBERNATORIAL, election.getCategory());
        assertEquals(State.KOGI, election.getState());
        assertEquals("00:00", election.getStartTime().toString());
        assertEquals("23:55", election.getStopTime().toString());
    }

    @Test
    public void upload_empty_file_throws_exception() {
        assertThrows(EmptyFileException.class,
                () -> electionService.uploadElections(emptyCsvFile, ELECTORATE_ID));
        assertEquals(0L, electionRepository.count());
    }

    @Test
    public void upload_wrong_csv_headers_throws_invalid_file_exception() {
        assertThrows(InvalidFileException.class,
                () -> electionService.uploadElections(wrongHeadersCsvFile, ELECTORATE_ID));
        assertEquals(0L, electionRepository.count());
    }

    @Test
    public void upload_duplicate_elections_are_skipped() {
        electionService.uploadElections(validCsvFile, ELECTORATE_ID);
        assertEquals(3L, electionRepository.count());

        // Upload same file again — all duplicates, count stays at 3
        electionService.uploadElections(validCsvFile, ELECTORATE_ID);
        assertEquals(3L, electionRepository.count());
    }

    @Test
    public void upload_elections_with_invalid_electorate_throws_exception() {
        assertThrows(RuntimeException.class,
                () -> electionService.uploadElections(validCsvFile, "INVALID-ID"));
    }

    @Test
    public void upload_elections_without_permission_throws_exception() {
        Electorate noPermElectorate = Electorate.builder()
                .electorateId("NO-PERM-001")
                .firstName("Test")
                .lastName("User")
                .dateOfBirth("1990-01-01")
                .citizenship(CitizenshipType.NATURALIZATION)
                .permissions(List.of()) // no CAN_UPLOAD_FILE
                .build();
        electorateRepository.save(noPermElectorate);

        assertThrows(RuntimeException.class,
                () -> electionService.uploadElections(validCsvFile, "NO-PERM-001"));
    }
}