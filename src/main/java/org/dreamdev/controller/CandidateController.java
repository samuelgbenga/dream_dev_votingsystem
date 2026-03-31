package org.dreamdev.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.dto.responses.CandidateResponse;
import org.dreamdev.services.CandidateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Slf4j
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCandidates(
            @RequestParam("file") MultipartFile file,
            @RequestParam("electorateId") String electorateId) {

        try {
            String result = candidateService.uploadCandidate(file, electorateId);
            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            log.error("Error uploading candidates: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to upload candidates: " + ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getAllCandidates() {
        try {
            List<CandidateResponse> candidates = candidateService.getAllCandidates();
            return ResponseEntity.ok(candidates);
        } catch (Exception ex) {
            log.error("Error fetching candidates: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}