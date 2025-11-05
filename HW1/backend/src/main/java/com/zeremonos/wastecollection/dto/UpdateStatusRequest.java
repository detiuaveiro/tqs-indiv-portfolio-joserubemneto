package com.zeremonos.wastecollection.dto;

import com.zeremonos.wastecollection.model.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {

    @NotNull(message = "New status is required")
    private RequestStatus newStatus;

    private String notes;
}

