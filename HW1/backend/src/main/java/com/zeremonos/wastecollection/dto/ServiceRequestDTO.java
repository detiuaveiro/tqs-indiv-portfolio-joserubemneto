package com.zeremonos.wastecollection.dto;

import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.TimeSlot;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for creating a new service request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestDTO {

    @NotBlank(message = "Municipality code is required")
    private String municipalityCode;

    @NotBlank(message = "Municipality name is required")
    private String municipalityName;

    @NotBlank(message = "Citizen name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String citizenName;

    @Email(message = "Invalid email format")
    private String citizenEmail;

    @Pattern(regexp = "^[0-9]{9}$", message = "Phone must have 9 digits")
    private String citizenPhone;

    @NotBlank(message = "Pickup address is required")
    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String pickupAddress;

    @NotBlank(message = "Item description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String itemDescription;

    @NotNull(message = "Preferred date is required")
    @Future(message = "Preferred date must be in the future")
    private LocalDate preferredDate;

    @NotNull(message = "Preferred time slot is required")
    private TimeSlot preferredTimeSlot;
}

