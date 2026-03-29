package org.dreamdev.services;


import lombok.RequiredArgsConstructor;
import org.dreamdev.dto.requests.VoterRequest;
import org.dreamdev.models.Voter;
import org.dreamdev.models.VoterStatus;
import org.dreamdev.repositories.VoterRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoterService {

    private final VoterRepository voterRepository;

    public Voter registerVoter(VoterRequest request) {
        Voter voter = Voter.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .citizenship(request.getCitizenship())
                .status(VoterStatus.PENDING)
                .build();

        return voterRepository.save(voter);
    }
}
