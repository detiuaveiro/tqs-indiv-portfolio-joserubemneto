package com.zeremonos.wastecollection.dto;

import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.ServiceRequest;
import com.zeremonos.wastecollection.model.StatusHistory;
import com.zeremonos.wastecollection.model.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestResponse {

    private Long id;
    private String token;
    private String municipalityCode;
    private String municipalityName;
    private String citizenName;
    private String citizenEmail;
    private String citizenPhone;
    private String pickupAddress;
    private String itemDescription;
    private LocalDate preferredDate;
    private TimeSlot preferredTimeSlot;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StatusHistoryDTO> statusHistory;

    public static ServiceRequestResponse fromEntity(ServiceRequest entity) {
        ServiceRequestResponse response = new ServiceRequestResponse();
        response.setId(entity.getId());
        response.setToken(entity.getToken());
        response.setMunicipalityCode(entity.getMunicipalityCode());
        response.setMunicipalityName(entity.getMunicipalityName());
        response.setCitizenName(entity.getCitizenName());
        response.setCitizenEmail(entity.getCitizenEmail());
        response.setCitizenPhone(entity.getCitizenPhone());
        response.setPickupAddress(entity.getPickupAddress());
        response.setItemDescription(entity.getItemDescription());
        response.setPreferredDate(entity.getPreferredDate());
        response.setPreferredTimeSlot(entity.getPreferredTimeSlot());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        
        if (entity.getStatusHistory() != null) {
            response.setStatusHistory(
                entity.getStatusHistory().stream()
                    .map(StatusHistoryDTO::fromEntity)
                    .collect(Collectors.toList())
            );
        }
        
        return response;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusHistoryDTO {
        private Long id;
        private RequestStatus previousStatus;
        private RequestStatus newStatus;
        private LocalDateTime timestamp;
        private String notes;

        public static StatusHistoryDTO fromEntity(StatusHistory entity) {
            return new StatusHistoryDTO(
                entity.getId(),
                entity.getPreviousStatus(),
                entity.getNewStatus(),
                entity.getTimestamp(),
                entity.getNotes()
            );
        }
    }
}

