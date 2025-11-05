package com.zeremonos.wastecollection.controller;

import com.zeremonos.wastecollection.dto.ServiceRequestDTO;
import com.zeremonos.wastecollection.dto.ServiceRequestResponse;
import com.zeremonos.wastecollection.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for citizen operations
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CitizenController {

    private final ServiceRequestService serviceRequestService;

    /**
     * Create a new waste collection request
     * POST /api/requests
     */
    @PostMapping
    public ResponseEntity<ServiceRequestResponse> createRequest(
            @Valid @RequestBody ServiceRequestDTO requestDTO) {
        log.info("POST /api/requests - Creating new service request for municipality: {}", 
            requestDTO.getMunicipalityName());
        
        ServiceRequestResponse response = serviceRequestService.createServiceRequest(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get service request details by token
     * GET /api/requests/{token}
     */
    @GetMapping("/{token}")
    public ResponseEntity<ServiceRequestResponse> getRequestByToken(@PathVariable String token) {
        log.info("GET /api/requests/{} - Fetching service request", token);
        
        ServiceRequestResponse response = serviceRequestService.getByToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel service request by token
     * DELETE /api/requests/{token}
     */
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> cancelRequest(@PathVariable String token) {
        log.info("DELETE /api/requests/{} - Cancelling service request", token);
        
        serviceRequestService.cancelByToken(token);
        return ResponseEntity.noContent().build();
    }
}

