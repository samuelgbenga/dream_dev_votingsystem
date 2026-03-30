package org.dreamdev.contoller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.services.ElectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/elections")
@RequiredArgsConstructor
@Slf4j
public class ElectionController {

    private final ElectionService electionService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadElections(
            @RequestParam("file") MultipartFile file,
            @RequestParam("electorateId") String electorateId) {

        try {
            String result = electionService.uploadElections(file, electorateId);
            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            log.error("Error uploading elections: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to upload elections: " + ex.getMessage());
        }
    }
}