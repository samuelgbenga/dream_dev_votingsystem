package org.dreamdev.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.dto.requests.ElectoratePermissionRequestDto;
import org.dreamdev.dto.requests.ElectorateRequestDto;
import org.dreamdev.dto.responses.ElectorateResponse;
import org.dreamdev.services.ElectorateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/electorates")
@RequiredArgsConstructor
@Slf4j
public class ElectorateController {

    private final ElectorateService electorateService;

    // Approve a voter
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

    @PostMapping
    public ResponseEntity<ElectorateResponse> addNewElectorate(
            @RequestBody ElectorateRequestDto request,
            @RequestParam("assignerElectorateId") String assignerElectorateId) {

        try {
            ElectorateResponse response = electorateService.addNewElectorate(request, assignerElectorateId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.error("Error adding new electorate: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/assign-permission")
    public ResponseEntity<?> assignPermission(
            @RequestBody ElectoratePermissionRequestDto request,
            @RequestParam("assignerElectorateId") String assignerElectorateId) {

        try {
            String response = electorateService.assignPermissions(request, assignerElectorateId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error assigning permission: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/remove-permission")
    public ResponseEntity<?> removePermission(
            @RequestBody ElectoratePermissionRequestDto request,
            @RequestParam("assignerElectorateId") String assignerElectorateId) {

        try {
            String response = electorateService.removePermissions(request, assignerElectorateId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error removing permission: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ElectorateResponse>> getAllElectorates(
            @RequestParam("electorateId") String electorateId) {

        try {
            List<ElectorateResponse> responses = electorateService.getAllElectorates(electorateId);
            return ResponseEntity.ok(responses);
        } catch (Exception ex) {
            log.error("Error fetching electorates: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}