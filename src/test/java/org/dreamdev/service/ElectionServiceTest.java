package org.dreamdev.service;

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

    private final String ELECTORATE_ID = "ELECTORATE-001";

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

        // Prepare CSV file with 3 elections
        String csvContent = """
                electionId,electionName,date,startTime,stopTime
                ELECTPRES2026,Presidential Election 2026,2026-04-15,08:00,18:00
                ELECTSEN2026,Senatorial Election 2026,2026-04-20,09:00,17:00
                ELECTGUB2026,Gubernatorial Election 2026,2026-05-01,07:00,19:00
                """;

        validCsvFile = new MockMultipartFile(
                "file",
                "elections.csv",
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
    public void upload_elections_from_csv_successfully() {
        assertEquals(0L, electionRepository.count());

        String result = electionService.uploadElections(validCsvFile, ELECTORATE_ID);

        assertEquals(3L, electionRepository.count());
        assertEquals("3 elections uploaded successfully", result);
    }

    @Test
    public void upload_elections_saves_correct_data() {
        electionService.uploadElections(validCsvFile, ELECTORATE_ID);

        Election election = electionRepository.findByElectionId("ELECTION2026").get();

        assertNotNull(election);
        assertEquals(Category.PRESIDENTIAL, election.getCategory());
        assertEquals("08:00", election.getStartTime().toString());
        assertEquals("18:00", election.getStopTime().toString());
    }

    @Test
    public void upload_empty_file_throws_exception() {
        assertThrows(RuntimeException.class, () -> electionService.uploadElections(emptyCsvFile, ELECTORATE_ID));
        assertEquals(0L, electionRepository.count());
    }
}