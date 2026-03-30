package org.dreamdev.services;


import lombok.RequiredArgsConstructor;
import org.dreamdev.dto.responses.VoteResponse;
import org.dreamdev.models.Vote;
import org.dreamdev.repositories.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;


    // get votes by categories.
    // returns candidateName, candidateId and total votes. in descending order by
    // number of votes per candidate


    public List<VoteResponse> getAllVotes() {
        return voteRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private VoteResponse mapToResponse(Vote vote) {
        return VoteResponse.builder()
                .electionId(vote.getElectionId())
                .candidateId(vote.getCandidateId())
                .categoryId(vote.getCategoryId())
                .voteStatus(vote.getVoteStatus())
                .createdAt(vote.getCreatedAt())
                .updatedAt(vote.getUpdatedAt())
                .build();
    }
}
