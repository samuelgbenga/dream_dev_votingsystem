package org.dreamdev.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.dto.responses.ElectionResponse;
import org.dreamdev.models.State;
import org.dreamdev.services.ElectionService;
import org.dreamdev.utils.HelperClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/elections")
@RequiredArgsConstructor
@Slf4j
public class ElectionController {

    private final ElectionService electionService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadElections(
            @RequestParam("file") MultipartFile file,
            @RequestParam("electorateId") String electorateId) {

        Optional<ResponseEntity<String>> UNSUPPORTED_MEDIA_TYPE = HelperClass.getStringResponseEntity(file);
        if (UNSUPPORTED_MEDIA_TYPE.isPresent()) return UNSUPPORTED_MEDIA_TYPE.get();

        try {
            String result = electionService.uploadElections(file, electorateId);
            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            log.error("Error uploading elections: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to upload elections: " + ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ElectionResponse>> getAllElection() {
        try {
            List<ElectionResponse> responses = electionService.getAllElections();
            return ResponseEntity.ok(responses);
        } catch (Exception ex) {
            log.error("Error fetching electionResponse: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GetMapping("/getbystate")
    public ResponseEntity<List<ElectionResponse>> getAllByState(
            @RequestParam State state
    ) {
        try {
            List<ElectionResponse> responses = electionService.getListOfElectionOfSameState(state);
            return ResponseEntity.ok(responses);
        } catch (Exception ex) {
            log.error("Error fetching electionResponse: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GetMapping("/getbystateanddate")
    public ResponseEntity<List<ElectionResponse>> getAllByStateAndDate(
            @RequestParam State state,
            @RequestParam LocalDate date
    ) {
        try {
            List<ElectionResponse> responses = electionService.getListOfElectionOfSameStateAndDate(state, date);
            return ResponseEntity.ok(responses);
        } catch (Exception ex) {
            log.error("Error fetching electionResponse: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}