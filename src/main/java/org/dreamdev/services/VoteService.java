package org.dreamdev.services;


import lombok.RequiredArgsConstructor;
import org.dreamdev.dto.responses.CandidateVoteSummaryResponse;
import org.dreamdev.dto.responses.VoteResponse;
import org.dreamdev.exceptions.NotFoundException;
import org.dreamdev.exceptions.PermissionNotFoundException;
import org.dreamdev.models.*;
import org.dreamdev.repositories.CandidateRepository;
import org.dreamdev.repositories.ElectionRepository;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.repositories.VoteRepository;
import org.dreamdev.utils.HelperClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;

    private final CandidateRepository candidateRepository;

    private final ElectorateRepository electorateRepository;
    private final ElectionRepository electionRepository;


    public List<VoteResponse> getAllVotes(int page, int size, String electorateId) {
        Optional<Electorate> electorate = electorateRepository.findByElectorateId(electorateId);
        if(electorate.isEmpty()) throw new NotFoundException("Electorate not found");
        if(!HelperClass.hasPermission(electorate.get().getPermissions(), Permission.CAN_VIEW_VOTE)){
            throw new PermissionNotFoundException("Does not have the required permission");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Vote> votePage = voteRepository.findAll(pageable);
        return votePage.stream()
                .map(this::mapToResponse)
                .toList();
    }

//    public List<VoteResponse> getVotesByCategoryAndElection( String electionId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
//        Page<Vote> votePage = voteRepository.findByElectionId(electionId, pageable);
//
//        return votePage.stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }


    public CandidateVoteSummaryResponse getCandidateVoteSummary(String candidateId) {


        String electionId = HelperClass.extractElectionId(candidateId);

        Optional<Candidate> candidate = candidateRepository.findByCandidateId(candidateId);

        if(candidate.isEmpty()) throw new NotFoundException("Candidate not found");

        return  CandidateVoteSummaryResponse.builder()
                        .electionId(candidate.get().getCandidateId())
                        .candidateId(candidate.get().getCandidateId())
                        .category(getElectionCategory(electionId))
                        .firstName(candidate.get().getFirstName())
                        .lastName(candidate.get().getLastName())
                        .numberOfVote(candidate.get().getNumberOfVote())
                        .build();
    }

    public List<CandidateVoteSummaryResponse> getCandidatesVoteSummaryByElectionId(String electionId) {
        List<Candidate> candidates = candidateRepository.findByCandidateIdStartingWith(electionId);

        Category category = getElectionCategory(electionId);

        return candidates.stream()
                .map(c -> CandidateVoteSummaryResponse.builder()
                        .electionId(electionId)
                        .candidateId(c.getCandidateId())
                        .category(category)
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .numberOfVote(c.getNumberOfVote())
                        .build()
                )
                .sorted(Comparator.comparingInt(CandidateVoteSummaryResponse::getNumberOfVote).reversed())
                .collect(Collectors.toList());
    }

    private VoteResponse mapToResponse(Vote vote) {
        return VoteResponse.builder()
                .electionId(vote.getElectionId())
                .candidateId(vote.getCandidateId())
                .voteStatus(vote.getVoteStatus())
                .createdAt(vote.getCreatedAt())
                .updatedAt(vote.getUpdatedAt())
                .build();
    }

    private Category getElectionCategory(String electionId) {
        Election election = electionRepository.findByElectionId(electionId).orElseThrow(
                ()-> new NotFoundException("Election with this id not found")
        );
        return election.getCategory();
    }


}

