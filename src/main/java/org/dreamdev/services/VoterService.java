package org.dreamdev.services;


import lombok.RequiredArgsConstructor;
import org.dreamdev.dto.requests.VoteRequest;
import org.dreamdev.dto.requests.VoterRequest;
import org.dreamdev.exceptions.CanNotVoteAgainException;
import org.dreamdev.exceptions.PermissionNotFoundException;
import org.dreamdev.exceptions.VoterNotFoundException;
import org.dreamdev.models.*;
import org.dreamdev.repositories.CandidateRepository;
import org.dreamdev.repositories.VoteRepository;
import org.dreamdev.repositories.VoterRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoterService {

    private final VoterRepository voterRepository;
    private final VoteRepository voteRepository;
    private final JwtService jwtService;

    public Voter registerVoter(VoterRequest request) {
        Voter voter = Voter.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .citizenship(request.getCitizenship())
                .status(VoterStatus.PENDING)
                .voterId(generateVoterId(request.getFirstName(), request.getLastName()))
                //.permissions(List.of(Permission.CAN_UPLOAD_FILE))
                .build();

        return voterRepository.save(voter);
    }


    public Voter vote(VoterRequest request) {
        Voter voter = Voter.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .citizenship(request.getCitizenship())
                .status(VoterStatus.PENDING)
                //.permissions(List.of(Permission.CAN_UPLOAD_FILE))
                .build();

        return voterRepository.save(voter);
    }


    public String initiateVote(VoteRequest request) {
        validateVoter(request);
        boolean hasVoted = hasAlreadyVoted(request.getVoterId(), request.getElectionId(), request.getCategoryId());
        if(hasVoted) throw new CanNotVoteAgainException("You have already voted in this category");
        String hashedVoterId = hashVoterId(request.getVoterId());
        Vote vote = mapToVote(request, hashedVoterId);
        Vote savedVote = voteRepository.save(vote);
        String jwtToken = jwtService.generateVoteToken(savedVote.getId());
        savedVote.setJwtToken(jwtToken);
        voteRepository.save(savedVote);
        return buildVoteCompletionLink(jwtToken);
    }

    private void validateVoter(VoteRequest request) {
        // check if user even exist

        Optional<Voter> voter = voterRepository.findByVoterId(request.getVoterId());

        if(voter.isEmpty()) throw new VoterNotFoundException("This voter id does not exist");

        if(!hasPermission(voter.get().getPermissions(), Permission.CAN_VOTE)) {
            throw new PermissionNotFoundException("Voter does not have permission to vote" );
        }

    }

    private boolean hasPermission(List<Permission> permissions, Permission existingPermission) {
        for(Permission permission: permissions){
            if(permission.equals(existingPermission)) return true;
        }
        return false;
    }

    private Vote mapToVote(VoteRequest request, String hashedVoterId){
        return Vote.builder()
                .electionId(request.getElectionId())
                .candidateId(request.getCandidateId())
                .categoryId(request.getCategoryId())
                .hashedVoterId(hashedVoterId)
                .voteStatus(VoteStatus.DEFAULTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    private String buildVoteCompletionLink(String jwtToken) {
        return "https://localhost:8080/api/v1/votes/complete/" + jwtToken;
    }

    private boolean hasAlreadyVoted(String voterId, String electionId, String categoryId) {
        String hashedVoterId = hashVoterId(voterId);
        return voteRepository.existsByHashedVoterIdAndElectionIdAndCategoryId(hashedVoterId, electionId, categoryId);
    }

    private String hashVoterId(String voterId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(voterId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash voterId", e);
        }
    }

    private String generateVoterId(String firstName, String lastName) {
        String initials = String.valueOf(firstName.charAt(0)).toUpperCase() +
                String.valueOf(lastName.charAt(0)).toUpperCase();
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("ddMMyyyyHHmm"));
        return "NIG-" + initials + "-" + timestamp;
    }

}
