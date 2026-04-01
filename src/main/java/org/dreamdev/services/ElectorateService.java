package org.dreamdev.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.dto.requests.ElectoratePermissionRequestDto;
import org.dreamdev.dto.requests.ElectorateRequestDto;
import org.dreamdev.dto.responses.ElectorateResponse;
import org.dreamdev.exceptions.AlreadyExistException;
import org.dreamdev.exceptions.NotFoundException;
import org.dreamdev.exceptions.PermissionNotFoundException;
import org.dreamdev.models.Electorate;
import org.dreamdev.models.Permission;
import org.dreamdev.models.Voter;
import org.dreamdev.models.VoterStatus;
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

        voter.get().setStatus(VoterStatus.APPROVED);
        voter.get().getPermissions().add(Permission.CAN_VOTE);

        voterRepository.save(voter.get());

        return "Your voting id has been approved";

    }


    public ElectorateResponse addNewElectorate(ElectorateRequestDto request, String assignerElectorateId) {

        validateElectorateForElectorate(assignerElectorateId);
        Electorate electorate = Electorate.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .citizenship(request.getCitizenship())
                .permissions(List.of(Permission.CAN_UPLOAD_FILE))
                .electorateId(request.getElectorateId())
                .build();

        Electorate saved = electorateRepository.save(electorate);

        return mapToResponse(saved);
    }

    public ElectorateResponse assignPermissions(ElectoratePermissionRequestDto request, String assignerElectorateId) {

        validateElectorateForElectorate(assignerElectorateId);
        Electorate electorate = getElectorateById(request.getElectorateId());
        if(HelperClass.hasPermission(electorate.getPermissions(), request.getPermission())){
            throw  new AlreadyExistException("Electorate Already has this permission");
        }

        electorate.getPermissions().add(request.getPermission());
        Electorate saved = electorateRepository.save(electorate);
        return mapToResponse(saved);
    }

    public ElectorateResponse removePermissions(ElectoratePermissionRequestDto request, String assignerElectorateId) {
        validateElectorateForElectorate(assignerElectorateId);
        Electorate electorate = getElectorateById(request.getElectorateId());
        electorate.getPermissions().remove(request.getPermission());
        Electorate saved = electorateRepository.save(electorate);
        return mapToResponse(saved);
    }

    private Electorate getElectorateById(String electorateId) {
        Optional<Electorate> optional = electorateRepository.findByElectorateId(electorateId);
        if (optional.isEmpty()) throw new RuntimeException("Electorate not found");
        return optional.get();
    }

    private void validateElectorate(String electorateId) {

        Optional<Electorate> electorate = electorateRepository.findByElectorateId(electorateId);

        if(electorate.isEmpty()) throw new NotFoundException("Electorate with this Id not found");

        if(!HelperClass.hasPermission(electorate.get().getPermissions(), Permission.CAN_APPROVE_VOTER)) {
            throw new PermissionNotFoundException("Electorate does not have permission to vote" );
        }
    }

    private void validateElectorateForElectorate(String electorateId) {

        Optional<Electorate> electorate = electorateRepository.findByElectorateId(electorateId);

        if(electorate.isEmpty()) throw new NotFoundException("Electorate with this Id not found");

        if(!HelperClass.hasPermission(electorate.get().getPermissions(), Permission.CAN_UPDATE_ELECTORATE)) {
            throw new PermissionNotFoundException("Electorate does not have permission to vote" );
        }
    }

    public List<ElectorateResponse> getAllElectorates(String electorateId) {
        extracted(electorateId);
        return electorateRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void extracted(String electorateId) {
        Optional<Electorate> electorate = electorateRepository.findByElectorateId(electorateId);
        if (electorate.isEmpty()) throw new NotFoundException("Electorate with this id not found");
        if (!HelperClass.hasPermission(electorate.get().getPermissions(), Permission.CAN_VIEW_ELECTORATE)) {
            throw new PermissionNotFoundException("You do not have required permission to view");
        }
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

}
