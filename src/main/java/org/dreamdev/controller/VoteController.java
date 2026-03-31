package org.dreamdev.controller;

import lombok.RequiredArgsConstructor;
import org.dreamdev.dto.responses.CandidateVoteSummaryResponse;
import org.dreamdev.dto.responses.VoteResponse;
import org.dreamdev.services.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;


    @GetMapping("/all")
    public ResponseEntity<List<VoteResponse>> getAllVotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String electorateId
    ) {
        List<VoteResponse> votes = voteService.getAllVotes(page, size, electorateId);
        return ResponseEntity.ok(votes);
    }


    @GetMapping("/category-election")
    public ResponseEntity<List<VoteResponse>> getVotesByCategoryAndElection(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String categoryId,
            @RequestParam String electionId
    ) {
        List<VoteResponse> votes = voteService.getVotesByCategoryAndElection(categoryId, electionId, page, size);
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/candidate-summary")
    public ResponseEntity<List<CandidateVoteSummaryResponse>> getCandidateVoteSummary(
            @RequestParam String categoryId,
            @RequestParam String electionId
    ) {
        List<CandidateVoteSummaryResponse> summary = voteService.getCandidateVoteSummary(categoryId, electionId);
        return ResponseEntity.ok(summary);
    }
}