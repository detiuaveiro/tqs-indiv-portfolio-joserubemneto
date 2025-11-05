package com.zeremonos.wastecollection.controller;

import com.zeremonos.wastecollection.dto.ServiceRequestResponse;
import com.zeremonos.wastecollection.dto.UpdateStatusRequest;
import com.zeremonos.wastecollection.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StaffController {

    private final ServiceRequestService serviceRequestService;

    @GetMapping
    public ResponseEntity<List<ServiceRequestResponse>> getAllRequests(
            @RequestParam(required = false) String municipality) {
        log.info("GET /api/staff/requests - Fetching all requests (municipality filter: {})", 
            municipality);
        
        List<ServiceRequestResponse> requests = serviceRequestService.getAllRequests(municipality);
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceRequestResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest updateRequest) {
        log.info("PUT /api/staff/requests/{}/status - Updating status to {}", 
            id, updateRequest.getNewStatus());
        
        ServiceRequestResponse response = serviceRequestService.updateStatus(id, updateRequest);
        return ResponseEntity.ok(response);
    }
}

