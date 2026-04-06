package org.dreamdev.service;

import org.dreamdev.models.*;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.repositories.VoterRepository;
import org.dreamdev.services.ElectorateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ElectorateServiceTest {

    @Autowired
    private ElectorateService electorateService;

    @Autowired
    private VoterRepository voterRepository;

    @Autowired
    private ElectorateRepository electorateRepository;

    private Voter voter;
    private Electorate electorate;

    @BeforeEach
    public void setUp() {
        voterRepository.deleteAll();
        electorateRepository.deleteAll();

        // Voter with a state of residence so statePermissions can be assigned on approval
        voter = Voter.builder()
                .firstName("Alice")
                .lastName("Smith")
                .dateOfBirth("1995-04-20")
                .voterId("NIG-AS-060420251200")
                .citizenship(CitizenshipType.NATURALIZATION)
                .stateOfResidence(State.KOGI)
                .permissions(new ArrayList<>())
                .statePermissions(new ArrayList<>())
                .build();
        voterRepository.save(voter);

        electorate = Electorate.builder()
                .electorateId("ELEC-001")
                .firstName("Samuel")
                .lastName("Joseph")
                .citizenship(CitizenshipType.NATURALIZATION)
                .permissions(new ArrayList<>(List.of(Permission.CAN_APPROVE_VOTER)))
                .build();
        electorateRepository.save(electorate);
    }

    @Test
    public void test_approveVoter_success() {
        String response = electorateService.approveVoter(voter.getVoterId(), electorate.getElectorateId());

        assertEquals("Your voting id has been approved", response);

        Voter updatedVoter = voterRepository.findByVoterId(voter.getVoterId()).orElseThrow();

        // CAN_VOTE permission added
        assertTrue(updatedVoter.getPermissions().contains(Permission.CAN_VOTE));

        // State permission for KOGI added
        assertTrue(updatedVoter.getStatePermissions().contains(Permission.forState(State.KOGI)));

        // NATIONAL permission also added
        assertTrue(updatedVoter.getStatePermissions().contains(Permission.forState(State.NATIONAL)));

        // Status set to APPROVED
        assertEquals(VoterStatus.APPROVED, updatedVoter.getStatus());
    }

    @Test
    public void test_approveVoter_voterNotFound() {
        Exception exception = assertThrows(
                org.dreamdev.exceptions.NotFoundException.class,
                () -> electorateService.approveVoter("nonexistent-voter-id", electorate.getElectorateId())
        );

        assertEquals("No voter with this id", exception.getMessage());
    }

    @Test
    public void test_approveVoter_electorateNotFound() {
        Exception exception = assertThrows(
                org.dreamdev.exceptions.NotFoundException.class,
                () -> electorateService.approveVoter(voter.getVoterId(), "invalid-electorate")
        );

        assertEquals("Electorate with this Id not found", exception.getMessage());
    }

    @Test
    public void test_approveVoter_electorateWithoutPermission() {
        electorate.setPermissions(new ArrayList<>());
        electorateRepository.save(electorate);

        Exception exception = assertThrows(
                org.dreamdev.exceptions.PermissionNotFoundException.class,
                () -> electorateService.approveVoter(voter.getVoterId(), electorate.getElectorateId())
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    public void test_approved_voter_has_correct_state_permission_not_other_state() {
        electorateService.approveVoter(voter.getVoterId(), electorate.getElectorateId());

        Voter updatedVoter = voterRepository.findByVoterId(voter.getVoterId()).orElseThrow();

        // Voter lives in KOGI — should NOT have permission for LAGOS
        assertFalse(updatedVoter.getStatePermissions().contains(Permission.forState(State.LAGOS)));
    }
}