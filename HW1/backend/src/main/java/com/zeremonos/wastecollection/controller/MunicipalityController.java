package com.zeremonos.wastecollection.controller;

import com.zeremonos.wastecollection.dto.MunicipalityDTO;
import com.zeremonos.wastecollection.service.MunicipalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/municipalities")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MunicipalityController {

    private final MunicipalityService municipalityService;

    @GetMapping
    public ResponseEntity<List<MunicipalityDTO>> getAllMunicipalities() {
        log.info("GET /api/municipalities - Fetching all municipalities");
        
        try {
            List<MunicipalityDTO> municipalities = municipalityService.getAllMunicipalities();
            return ResponseEntity.ok(municipalities);
        } catch (Exception e) {
            log.error("Error retrieving municipalities: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}