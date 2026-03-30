package org.dreamdev.services;


import lombok.RequiredArgsConstructor;
import org.dreamdev.dto.requests.VoteRequest;
import org.dreamdev.dto.requests.VoterRequest;
import org.dreamdev.dto.responses.VoterResponse;
import org.dreamdev.exceptions.*;
import org.dreamdev.models.*;
import org.dreamdev.repositories.CandidateRepository;
import org.dreamdev.repositories.ElectionRepository;
import org.dreamdev.repositories.VoteRepository;
import org.dreamdev.repositories.VoterRepository;
import org.dreamdev.utils.HelperClass;
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
    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;

    public Voter registerVoter(VoterRequest request) {
        Voter voter = Voter.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .citizenship(request.getCitizenship())
                .status(VoterStatus.PENDING)
                .voterId(generateVoterId(request.getFirstName(), request.getLastName()))
                .build();

        return voterRepository.save(voter);
    }


    public String initiateVote(VoteRequest request) {
        String electionId = getValidElectionId(request.getCandidateId());
        validateVoter(request);
        boolean hasVoted = hasAlreadyVoted(request.getVoterId(), electionId, request.getCategoryId());
        if(hasVoted) throw new CanNotVoteAgainException("You have already voted in this category");
        String hashedVoterId = hashVoterId(request.getVoterId());
        Vote vote = mapToVote(request, hashedVoterId, electionId);
        Vote savedVote = voteRepository.save(vote);
        String jwtToken = jwtService.generateVoteToken(savedVote.getId());
        savedVote.setJwtToken(jwtToken);
        voteRepository.save(savedVote);
        return buildVoteCompletionLink(jwtToken);
    }

    private String getValidElectionId(String candidateId) {
        String electionId = extractElectionId(candidateId);
        if(!isElectionValid(electionId)) throw new InvalidElection("Invalid Election");
        return null;
    }

    private String extractElectionId(String candidateId) {
        if (candidateId == null || !candidateId.contains("-")) {
            throw new InvalidIdFormat("Invalid candidateId format");
        }
        String[] parts = candidateId.split("-");
        if (parts.length < 3) {
            throw new InvalidIdFormat("Invalid candidateId format");
        }

        return parts[0];
    }

    public String confirmVote(String jwtToken){
        if(!jwtService.isTokenValid(jwtToken)) throw new ExpiredTokenException("Token is invalid expired");
        String voteId = jwtService.extractVoteId(jwtToken);
        Optional<Vote> vote = voteRepository.findById(voteId);
        if(vote.isEmpty()) throw new NotFoundException("Vote with this Id not found");
        increaseCandidateVote(vote.get());
        vote.get().setVoteStatus(VoteStatus.VOTED);
        voteRepository.save(vote.get());
        return "Vote confirmed";
    }

    private void increaseCandidateVote(Vote vote) {
        Optional<Candidate> candidate = candidateRepository.findByCandidateId(vote.getCandidateId());
        if(candidate.isEmpty()) throw new NotFoundException("Candidate with this id cannot be found");
        candidate.get().setNumberOfVote(candidate.get().getNumberOfVote() + 1);
        candidateRepository.save(candidate.get());
    }

    private void validateVoter(VoteRequest request) {
        Optional<Voter> voter = voterRepository.findByVoterId(request.getVoterId());

        if(voter.isEmpty()) throw new NotFoundException("This voter id does not exist");

        if(!HelperClass.hasPermission(voter.get().getPermissions(), Permission.CAN_VOTE)) {
            throw new PermissionNotFoundException("Voter does not have permission to vote" );
        }
    }

    private boolean isElectionValid(String electionId){
        Optional<Election> election = electionRepository.findByElectionId(electionId);
        if(election.isEmpty()) throw new NotFoundException("Election with id not found");
        return HelperClass.isElectionValid(election.get());
    }


    private Vote mapToVote(VoteRequest request, String hashedVoterId, String electionId){
        return Vote.builder()
                .electionId(electionId)
                .candidateId(request.getCandidateId())
                .categoryId(request.getCategoryId())
                .hashedVoterId(hashedVoterId)
                .voteStatus(VoteStatus.DEFAULTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public List<VoterResponse> getAllVoters() {
        return voterRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private VoterResponse mapToResponse(Voter voter) {
        return VoterResponse.builder()
                .lastName(voter.getLastName())
                .firstName(voter.getFirstName())
                .dateOfBirth(voter.getDateOfBirth())
                .citizenship(voter.getCitizenship())
                .voterId(voter.getVoterId())
                .status(voter.getStatus())
                .build();
    }


    private String buildVoteCompletionLink(String jwtToken) {
        return "https://localhost:8080/api/v1/voters/complete_vote?jwtToken=" + jwtToken;
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
