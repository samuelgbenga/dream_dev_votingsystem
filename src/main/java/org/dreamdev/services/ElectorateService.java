package org.dreamdev.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.dto.responses.ElectorateResponse;
import org.dreamdev.exceptions.NotFoundException;
import org.dreamdev.exceptions.PermissionNotFoundException;
import org.dreamdev.models.Electorate;
import org.dreamdev.models.Permission;
import org.dreamdev.models.Voter;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.repositories.VoterRepository;
import org.dreamdev.utils.HelperClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElectorateService {

    private final ElectorateRepository electorateRepository;
    private final VoterRepository voterRepository;

    // approve a voter

    public String approveVoter(String voterId, String electorateId){

        validateElectorate(electorateId);

        Optional<Voter> voter = voterRepository.findByVoterId(voterId);
        if(voter.isEmpty()) throw new NotFoundException("No voter with this id");

        voter.get().getPermissions().add(Permission.CAN_VOTE);

        voterRepository.save(voter.get());

        return "Your voting id has been approved";

    }

    public void validateElectorate(String electorateId) {

        Optional<Electorate> electorate = electorateRepository.findByElectorateId(electorateId);

        if(electorate.isEmpty()) throw new NotFoundException("Electorate with this Id not found");

        if(!HelperClass.hasPermission(electorate.get().getPermissions(), Permission.CAN_APPROVE_VOTER)) {
            throw new PermissionNotFoundException("Electorate does not have permission to vote" );
        }

    }

    public List<ElectorateResponse> getAllElectorates() {
        return electorateRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ElectorateResponse mapToResponse(Electorate electorate) {
        return ElectorateResponse.builder()
                .lastName(electorate.getLastName())
                .firstName(electorate.getFirstName())
                .dateOfBirth(electorate.getDateOfBirth())
                .citizenship(electorate.getCitizenship())
                .electorateId(electorate.getElectorateId())
                .build();
    }


    // add a new electorate without


    // update the permission status of an electorate
}
