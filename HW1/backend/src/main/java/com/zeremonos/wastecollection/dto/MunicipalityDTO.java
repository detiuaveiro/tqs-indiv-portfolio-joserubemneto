package com.zeremonos.wastecollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityDTO {

    private String name;
    private String code;

    public MunicipalityDTO(String name) {
        this.name = name;
        this.code = generateCode(name);
    }

    private String generateCode(String name) {
        if (name == null || name.isEmpty()) {
            return "0000";
        }
        String cleanName = name.replaceAll("[^A-Za-zÀ-ÿ]", "").toUpperCase();
        
        if (cleanName.isEmpty()) {
            return String.format("X%03d", Math.abs(name.hashCode() % 1000));
        }
        
        String prefix = cleanName.substring(0, Math.min(4, cleanName.length()));
        return String.format("%s%02d", prefix, Math.abs(name.hashCode() % 100));
    }
}

