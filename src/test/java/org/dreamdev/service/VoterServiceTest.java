package org.dreamdev.service;

import org.dreamdev.dto.requests.VoterRequest;
import org.dreamdev.repositories.VoterRepository;
import org.dreamdev.services.VoterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VoterServiceTest {

    @Autowired
    private VoterService voterService;

    @Autowired
    private VoterRepository voterRepository;

    private VoterRequest voterRequest;

    @BeforeEach
    public void setUp() {
        voterRepository.deleteAll();
        voterRequest = new VoterRequest();
        voterRequest.setFirstName("John");
        voterRequest.setLastName("Doe");
        voterRequest.setDateOfBirth("1990-01-01");
        voterRequest.setCitizenship("Nigerian");
    }

    @Test
    public void create_a_new_voter() {
        assertEquals(0L, voterRepository.count());
        voterService.registerVoter(voterRequest);
        assertEquals(1L, voterRepository.count());
    }
}