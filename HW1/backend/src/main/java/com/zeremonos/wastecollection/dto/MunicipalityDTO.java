package com.zeremonos.wastecollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Municipality information
 * Note: GeoAPI.pt returns a simple array of municipality names
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityDTO {

    private String name;
    private String code;

    public MunicipalityDTO(String name) {
        this.name = name;
        // Generate a simple code based on the name (first 4 chars + hash for uniqueness)
        this.code = generateCode(name);
    }

    private String generateCode(String name) {
        if (name == null || name.isEmpty()) {
            return "0000";
        }
        // Simple code generation: first 3-4 chars + position hash
        String cleanName = name.replaceAll("[^A-Za-zÀ-ÿ]", "").toUpperCase();
        
        if (cleanName.isEmpty()) {
            // If no letters remain, use hash only
            return String.format("X%03d", Math.abs(name.hashCode() % 1000));
        }
        
        String prefix = cleanName.substring(0, Math.min(4, cleanName.length()));
        return String.format("%s%02d", prefix, Math.abs(name.hashCode() % 100));
    }
}

