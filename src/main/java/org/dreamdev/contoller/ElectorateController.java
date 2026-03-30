package org.dreamdev.contoller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.services.ElectorateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/electorates")
@RequiredArgsConstructor
@Slf4j
public class ElectorateController {

    private final ElectorateService electorateService;


    @PostMapping("/approve-voter")
    public ResponseEntity<String> approveVoter(
            @RequestParam("voterId") String voterId,
            @RequestParam("electorateId") String electorateId) {

        try {
            String result = electorateService.approveVoter(voterId, electorateId);
            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            log.error("Error approving voter: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to approve voter: " + ex.getMessage());
        }
    }
}