package org.dreamdev.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.dto.requests.VoteRequest;
import org.dreamdev.dto.requests.VoterRequest;
import org.dreamdev.dto.responses.VoteResponse;
import org.dreamdev.dto.responses.VoterResponse;
import org.dreamdev.services.VoterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/voters")
@RequiredArgsConstructor
@Slf4j
public class VoterController {

    private final VoterService voterService;

    /**
     * Register a new voter
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerVoter(@RequestBody VoterRequest request) {
        try {
            return ResponseEntity.ok(voterService.registerVoter(request));
        } catch (Exception ex) {
            log.error("Error registering voter: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to register voter: " + ex.getMessage());
        }
    }


    @PostMapping("/vote")
    public ResponseEntity<?> initiateVote(@RequestBody VoteRequest request) {
        try {
            String voteLink = voterService.initiateVote(request);
            return ResponseEntity.ok(voteLink);
        } catch (Exception ex) {
            log.error("Error initiating vote: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to initiate vote: " + ex.getMessage());
        }
    }

    @PutMapping("/complete_vote")
    public ResponseEntity<?> confirmVote(@RequestParam String jwtToken) {
        try {
            String response = voterService.confirmVote(jwtToken);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error Confirming vote: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to Confirm vote: " + ex.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<VoterResponse>> getAllVoters() {
        List<VoterResponse> voters = voterService.getAllVoters();
        return ResponseEntity.ok(voters);
    }
}