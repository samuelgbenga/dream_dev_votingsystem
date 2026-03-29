package org.dreamdev.service;

import org.dreamdev.dto.requests.VoteRequest;
import org.dreamdev.dto.requests.VoterRequest;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.models.Permission;
import org.dreamdev.models.Voter;
import org.dreamdev.repositories.VoteRepository;
import org.dreamdev.repositories.VoterRepository;
import org.dreamdev.services.VoterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    private VoterRequest voterRequest;

    private VoteRequest voteRequest;

    @BeforeEach
    public void setUp() {
        voterRepository.deleteAll();
        voteRepository.deleteAll();

        voterRequest = new VoterRequest();
        voterRequest.setFirstName("John");
        voterRequest.setLastName("Doe");
        voterRequest.setDateOfBirth("1990-01-01");
        voterRequest.setCitizenship(CitizenshipType.DUAL_CITIZENSHIP);

        voteRequest = new VoteRequest();
        voteRequest.setVoterId("NIG-JD-0001");

        // will update the id later
        voteRequest.setElectionId("election-001");
        voteRequest.setCategoryId("category-001");
        voteRequest.setCandidateId("candidate-001");

    }

    @Test
    public void create_a_new_voter() {
        assertEquals(0L, voterRepository.count());
        voterService.registerVoter(voterRequest);
        assertEquals(1L, voterRepository.count());
    }


    @Test
    public void test_initiate_vote_successfully() {
        // register voter with CAN_VOTE permission
        //voterRequest.setPermissions(List.of(Permission.CAN_VOTE));
        Voter savedVoter = voterService.registerVoter(voterRequest);
        savedVoter.setPermissions(List.of(Permission.CAN_VOTE));
        voterRepository.save(savedVoter);
        voteRequest.setVoterId(savedVoter.getVoterId());

        String link = voterService.initiateVote(voteRequest);

        assertNotNull(link);
        assertTrue(link.contains("/api/v1/votes/complete/"));
        assertEquals(1L, voteRepository.count());
    }


    @Test
    public void test_voter_without_voting_permission() {
        assertEquals(0L, voterRepository.count());
        voterService.registerVoter(voterRequest);
        assertEquals(1L, voterRepository.count());
    }
}