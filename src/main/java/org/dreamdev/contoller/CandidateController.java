package org.dreamdev.contoller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.services.CandidateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}