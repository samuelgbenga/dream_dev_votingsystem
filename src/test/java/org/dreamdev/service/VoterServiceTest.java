package org.dreamdev.service;

import org.dreamdev.dto.requests.VoteRequest;
import org.dreamdev.dto.requests.VoterRequest;
import org.dreamdev.exceptions.InvalidElection;
import org.dreamdev.exceptions.PermissionNotFoundException;
import org.dreamdev.models.*;
import org.dreamdev.repositories.CandidateRepository;
import org.dreamdev.repositories.ElectionRepository;
import org.dreamdev.repositories.ElectorateRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VoterServiceTest {

    @Autowired private VoterService voterService;
    @Autowired private VoterRepository voterRepository;
    @Autowired private VoteRepository voteRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private ElectionRepository electionRepository;
    @Autowired private ElectorateRepository electorateRepository;
    @Autowired private ElectionService electionService;
    @Autowired private JwtService jwtService;

    private VoterRequest voterRequest;
    private VoteRequest voteRequest;

    private final String ELECTORATE_ID = "ELECTORATE-001";

    // ELECTGUB2026 is today (2026-04-06) so it passes isElectionValid()
    private final String ELECTION_CSV = """
            electionId,category,state,date,startTime,stopTime
            ELECTPRES2026,PRESIDENTIAL,NATIONAL,2026-04-15,08:00,18:00
            ELECTSEN2026,SENATORIAL,LAGOS,2026-04-20,09:00,17:00
            ELECTGUB2026,GUBERNATORIAL,KOGI,2026-04-06,00:00,23:55
            """;

    @BeforeEach
    public void setUp() {
        voterRepository.deleteAll();
        voteRepository.deleteAll();
        electionRepository.deleteAll();
        candidateRepository.deleteAll();
        electorateRepository.deleteAll();

        Electorate electorate = Electorate.builder()
                .electorateId(ELECTORATE_ID)
                .firstName("Samuel")
                .lastName("Joseph")
                .dateOfBirth("1990-05-15")
                .citizenship(CitizenshipType.NATURALIZATION)
                .permissions(new ArrayList<>(List.of(Permission.CAN_UPLOAD_FILE)))
                .build();
        electorateRepository.save(electorate);

        MockMultipartFile electionFile = new MockMultipartFile(
                "file", "elections.csv", "text/csv", ELECTION_CSV.getBytes()
        );
        electionService.uploadElections(electionFile, ELECTORATE_ID);

        // VoterRequest — stateOfResidence is KOGI to match ELECTGUB2026's state
        voterRequest = new VoterRequest();
        voterRequest.setFirstName("John");
        voterRequest.setLastName("Doe");
        voterRequest.setDateOfBirth("1990-01-01");
        voterRequest.setCitizenship(CitizenshipType.DUAL_CITIZENSHIP);
        voterRequest.setStateOfResidence(State.KOGI);

        voteRequest = new VoteRequest();
        voteRequest.setCandidateId("ELECTGUB2026-CAND-001");
    }

    @Test
    public void create_a_new_voter() {
        assertEquals(0L, voterRepository.count());
        voterService.registerVoter(voterRequest);
        assertEquals(1L, voterRepository.count());
    }

    @Test
    public void register_voter_saves_state_of_residence() {
        var response = voterService.registerVoter(voterRequest);
        assertNotNull(response.getVoterId());

        Voter saved = voterRepository.findByVoterId(response.getVoterId()).orElseThrow();
        assertEquals(State.KOGI, saved.getStateOfResidence());
        assertEquals(VoterStatus.PENDING, saved.getStatus());
    }

    @Test
    public void test_initiate_vote_successfully() {
        var response = voterService.registerVoter(voterRequest);
        Voter savedVoter = voterRepository.findByVoterId(response.getVoterId()).orElseThrow();

        savedVoter.getPermissions().add(Permission.CAN_VOTE);
        savedVoter.getStatePermissions().add(Permission.forState(State.KOGI));
        savedVoter.getStatePermissions().add(Permission.forState(State.NATIONAL));
        voterRepository.save(savedVoter);

        voteRequest.setVoterId(savedVoter.getVoterId());

        String link = voterService.initiateVote(voteRequest);

        assertNotNull(link);
        assertTrue(link.contains("/api/v1/voters/complete_vote"));
        assertEquals(1L, voteRepository.count());
    }

    @Test
    public void test_voter_without_can_vote_permission_throws_exception() {
        var response = voterService.registerVoter(voterRequest);
        Voter savedVoter = voterRepository.findByVoterId(response.getVoterId()).orElseThrow();

        // State permission only — no CAN_VOTE
        savedVoter.getStatePermissions().add(Permission.forState(State.KOGI));
        voterRepository.save(savedVoter);

        voteRequest.setVoterId(savedVoter.getVoterId());

        assertThrows(PermissionNotFoundException.class, () -> voterService.initiateVote(voteRequest));
    }

    @Test
    public void test_voter_without_state_permission_throws_exception() {
        var response = voterService.registerVoter(voterRequest);
        Voter savedVoter = voterRepository.findByVoterId(response.getVoterId()).orElseThrow();

        // CAN_VOTE only — no state permission
        savedVoter.getPermissions().add(Permission.CAN_VOTE);
        voterRepository.save(savedVoter);

        voteRequest.setVoterId(savedVoter.getVoterId());

        assertThrows(PermissionNotFoundException.class, () -> voterService.initiateVote(voteRequest));
    }

    @Test
    public void test_initiate_vote_on_invalid_election_throws_exception() {
        var response = voterService.registerVoter(voterRequest);
        Voter savedVoter = voterRepository.findByVoterId(response.getVoterId()).orElseThrow();

        savedVoter.getPermissions().add(Permission.CAN_VOTE);
        savedVoter.getStatePermissions().add(Permission.forState(State.LAGOS));
        voterRepository.save(savedVoter);

        // ELECTSEN2026 is in the future — not valid today
        VoteRequest futureRequest = new VoteRequest();
        futureRequest.setVoterId(savedVoter.getVoterId());
        futureRequest.setCandidateId("ELECTSEN2026-CAND-001");

        assertThrows(InvalidElection.class, () -> voterService.initiateVote(futureRequest));
    }

    @Test
    public void confirm_vote_successfully() {
        Candidate candidate = Candidate.builder()
                .candidateId("ELECTGUB2026-CAND-001")
                .numberOfVote(0)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth("1990-01-01")
                .citizenship(CitizenshipType.REGISTRATION)
                .build();
        candidateRepository.save(candidate);

        Vote vote = Vote.builder()
                .electionId("ELECTGUB2026")
                .candidateId("ELECTGUB2026-CAND-001")
                .hashedVoterId("hashed-id")
                .voteStatus(VoteStatus.DEFAULTED)
                .build();

        Vote savedVote = voteRepository.save(vote);
        String token = jwtService.generateVoteToken(savedVote.getId());

        String response = voterService.confirmVote(token);

        assertEquals("Vote confirmed", response);

        Vote updatedVote = voteRepository.findById(savedVote.getId()).orElseThrow();
        assertEquals(VoteStatus.VOTED, updatedVote.getVoteStatus());
    }

    @Test
    public void confirm_vote_should_increase_candidate_vote_count() {
        Candidate candidate = Candidate.builder()
                .candidateId("ELECTGUB2026-CAND-001")
                .numberOfVote(10)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth("1990-01-01")
                .citizenship(CitizenshipType.REGISTRATION)
                .build();
        candidateRepository.save(candidate);

        Vote vote = Vote.builder()
                .electionId("ELECTGUB2026")
                .candidateId("ELECTGUB2026-CAND-001")
                .hashedVoterId("hashed-id")
                .voteStatus(VoteStatus.DEFAULTED)
                .build();

        Vote savedVote = voteRepository.save(vote);
        String token = jwtService.generateVoteToken(savedVote.getId());

        voterService.confirmVote(token);

        Candidate updatedCandidate = candidateRepository
                .findByCandidateId("ELECTGUB2026-CAND-001").orElseThrow();

        assertEquals(11, updatedCandidate.getNumberOfVote());
    }

    @Test
    public void confirm_vote_with_expired_token_throws_exception() {
        assertThrows(RuntimeException.class, () -> voterService.confirmVote("invalid.token.here"));
    }
}