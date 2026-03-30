package org.dreamdev.service;

import org.dreamdev.models.Electorate;
import org.dreamdev.models.Permission;
import org.dreamdev.models.Voter;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.repositories.VoterRepository;
import org.dreamdev.services.ElectorateService;
import org.dreamdev.utils.HelperClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        // Clear previous data
        voterRepository.deleteAll();
        electorateRepository.deleteAll();

        // Seed a voter
        voter = Voter.builder()
                .firstName("Alice")
                .lastName("Smith")
                .dateOfBirth("1995-04-20")
                .voterId("NIG-JD-0001")
                .citizenship(CitizenshipType.NATURALIZATION)
                .permissions(List.of()) // initially no permissions
                .build();
        voterRepository.save(voter);

        // Seed an electorate with CAN_APPROVE_VOTER permission
        electorate = Electorate.builder()
                .electorateId("ELEC-001")
                .firstName("Samuel")
                .lastName("Joseph")
                .citizenship(CitizenshipType.NATURALIZATION)
                .permissions(List.of(Permission.CAN_APPROVE_VOTER))
                .build();
        electorateRepository.save(electorate);
    }

    @Test
    public void test_approveVoter_success() {
            String response = electorateService.approveVoter(voter.getVoterId(), electorate.getElectorateId());

        assertEquals("Your voting id has been approved", response);

        // Verify voter now has CAN_VOTE permission
        Voter updatedVoter = voterRepository.findById(voter.getId()).orElseThrow();
        assertTrue(updatedVoter.getPermissions().contains(Permission.CAN_VOTE));
    }

    @Test
    public void test_approveVoter_voterNotFound() {
        Exception exception = assertThrows(
                org.dreamdev.exceptions.NotFoundException.class,
                () -> electorateService.approveVoter("nonexistent-id", electorate.getElectorateId())
        );

        assertEquals("No voter with this id", exception.getMessage());
    }

    @Test
    public void test_approveVoter_electorateNotFound() {
        Exception exception = assertThrows(
                org.dreamdev.exceptions.NotFoundException.class,
                () -> electorateService.approveVoter(voter.getId(), "invalid-electorate")
        );

        assertEquals("Electorate with this Id not found", exception.getMessage());
    }

    @Test
    public void test_approveVoter_electorateWithoutPermission() {
        // Remove CAN_APPROVE_VOTER from electorate
        electorate.setPermissions(List.of());
        electorateRepository.save(electorate);

        Exception exception = assertThrows(
                org.dreamdev.exceptions.PermissionNotFoundException.class,
                () -> electorateService.approveVoter(voter.getId(), electorate.getElectorateId())
        );

        assertEquals("Electorate does not have permission to vote", exception.getMessage());
    }
}