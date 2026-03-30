package org.dreamdev.service;

import org.dreamdev.dto.requests.VoteRequest;
import org.dreamdev.dto.requests.VoterRequest;
import org.dreamdev.models.*;
import org.dreamdev.repositories.CandidateRepository;
import org.dreamdev.repositories.ElectionRepository;
import org.dreamdev.repositories.VoteRepository;
import org.dreamdev.repositories.VoterRepository;
import org.dreamdev.services.ElectionService;
import org.dreamdev.services.JwtService;
import org.dreamdev.services.VoterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VoterServiceTest {

    @Autowired
    private VoterService voterService;

    @Autowired
    private VoterRepository voterRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private ElectionService electionService;

    @Autowired
    private JwtService jwtService;

    private VoterRequest voterRequest;

    private VoteRequest voteRequest;

    private final String ELECTORATE_ID = "ELECTORATE-001";

    @BeforeEach
    public void setUp() {
        voterRepository.deleteAll();
        voteRepository.deleteAll();
        electionRepository.deleteAll();

        voterRequest = new VoterRequest();
        voterRequest.setFirstName("John");
        voterRequest.setLastName("Doe");
        voterRequest.setDateOfBirth("1990-01-01");
        voterRequest.setCitizenship(CitizenshipType.DUAL_CITIZENSHIP);

        voteRequest = new VoteRequest();
        voteRequest.setVoterId("NIG-JD-0001");

        // will update the id later
        voteRequest.setCategoryId("CAT-001");
        voteRequest.setCandidateId("ELECTGUB2026-CAND-001");

        // Prepare CSV file with 3 elections
        String csvContent = """
                electionId,electionName,date,startTime,stopTime
                ELECTPRES2026,Presidential Election 2026,2026-04-15,08:00,18:00
                ELECTSEN2026,Senatorial Election 2026,2026-04-20,09:00,17:00
                ELECTGUB2026,Gubernatorial Election 2026,2026-03-30,07:00,23:00""";

        MockMultipartFile validCsvFile = new MockMultipartFile(
                "file",
                "elections.csv",
                "text/csv",
                csvContent.getBytes()
        );

        electionService.uploadElections(validCsvFile, ELECTORATE_ID);

    }

    @Test
    public void create_a_new_voter() {
        assertEquals(0L, voterRepository.count());
        voterService.registerVoter(voterRequest);
        assertEquals(1L, voterRepository.count());
    }


    @Test
    public void test_initiate_vote_successfully() {
        Voter savedVoter = voterService.registerVoter(voterRequest);
        savedVoter.setPermissions(List.of(Permission.CAN_VOTE));
        voterRepository.save(savedVoter);
        voteRequest.setVoterId(savedVoter.getVoterId());

        String link = voterService.initiateVote(voteRequest);

        assertNotNull(link);
        assertTrue(link.contains("/api/v1/voters/complete_vote"));
        assertEquals(1L, voteRepository.count());
    }


    @Test
    public void test_voter_without_voting_permission() {
        assertEquals(0L, voterRepository.count());
        voterService.registerVoter(voterRequest);
        assertEquals(1L, voterRepository.count());
    }

    @Test
    public void confirm_vote_successfully() {
       Vote vote = Vote.builder()
                .electionId("ELECTGUB2026")
                .candidateId("ELECTGUB2026-CAND-001")
                .categoryId("CAT-003")
                .hashedVoterId("hashed-id")
                .voteStatus(VoteStatus.DEFAULTED)
                .build();

        Vote savedVote = voteRepository.save(vote);

        String token = jwtService.generateVoteToken(savedVote.getId());

        String response = voterService.confirmVote(token);

        assertEquals("Vote confirmed", response);

        Vote updatedVote =
                voteRepository.findById(savedVote.getId()).orElse(null);

        assertNotNull(updatedVote);
        assertEquals(VoteStatus.VOTED, updatedVote.getVoteStatus());
    }


    @Test
    public void confirm_vote_should_increase_candidate_vote_count() {

        Candidate candidate = Candidate.builder()
                .candidateId("ELECTGUB2026-CAND-001")
                .numberOfVote(10)
                .categoryId("CAT-003")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth("1990-01-01")
                .citizenship(CitizenshipType.REGISTRATION)
                .build();

        candidateRepository.save(candidate);

        Vote vote = Vote.builder()
                .electionId("ELECTGUB2026")
                .candidateId("ELECTGUB2026-CAND-001")
                .categoryId("CAT-003")
                .hashedVoterId("hashed-id")
                .voteStatus(VoteStatus.DEFAULTED)
                .build();

        Vote savedVote = voteRepository.save(vote);

        String token = jwtService.generateVoteToken(savedVote.getId());

        voterService.confirmVote(token);

        Candidate updatedCandidate =
                candidateRepository.findByCandidateId("ELECTGUB2026-CAND-001").get();

        // Step 6: Assertions
        assertNotNull(updatedCandidate);
        assertEquals(11, updatedCandidate.getNumberOfVote()); // incremented by 1
    }
}