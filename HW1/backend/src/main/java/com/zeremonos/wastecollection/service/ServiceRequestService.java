package com.zeremonos.wastecollection.service;

import com.zeremonos.wastecollection.dto.ServiceRequestDTO;
import com.zeremonos.wastecollection.dto.ServiceRequestResponse;
import com.zeremonos.wastecollection.dto.UpdateStatusRequest;
import com.zeremonos.wastecollection.exception.BusinessException;
import com.zeremonos.wastecollection.exception.ResourceNotFoundException;
import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.ServiceRequest;
import com.zeremonos.wastecollection.model.StatusHistory;
import com.zeremonos.wastecollection.repository.ServiceRequestRepository;
import com.zeremonos.wastecollection.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    @Value("${app.max-requests-per-municipality-per-day:10}")
    private int maxRequestsPerMunicipalityPerDay;

    /**
     * Create a new service request
     */
    @Transactional
    public ServiceRequestResponse createServiceRequest(ServiceRequestDTO dto) {
        log.info("Creating service request for municipality: {}", dto.getMunicipalityName());

        // Business Rule: Validate date is not in the past
        if (dto.getPreferredDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Preferred date cannot be in the past");
        }

        // Business Rule: Check municipality daily limit
        long activeRequests = serviceRequestRepository
            .countActiveRequestsByMunicipalityAndDate(
                dto.getMunicipalityName(),
                dto.getPreferredDate()
            );

        if (activeRequests >= maxRequestsPerMunicipalityPerDay) {
            throw new BusinessException(
                String.format("Daily limit reached for municipality %s on %s. Maximum %d requests allowed per day.",
                    dto.getMunicipalityName(), dto.getPreferredDate(), maxRequestsPerMunicipalityPerDay)
            );
        }

        // Create service request
        ServiceRequest request = new ServiceRequest();
        request.setToken(UUID.randomUUID().toString());
        request.setMunicipalityCode(dto.getMunicipalityCode());
        request.setMunicipalityName(dto.getMunicipalityName());
        request.setCitizenName(dto.getCitizenName());
        request.setCitizenEmail(dto.getCitizenEmail());
        request.setCitizenPhone(dto.getCitizenPhone());
        request.setPickupAddress(dto.getPickupAddress());
        request.setItemDescription(dto.getItemDescription());
        request.setPreferredDate(dto.getPreferredDate());
        request.setPreferredTimeSlot(dto.getPreferredTimeSlot());
        request.setStatus(RequestStatus.RECEIVED);

        ServiceRequest savedRequest = serviceRequestRepository.save(request);

        // Create initial status history
        createStatusHistory(savedRequest, null, RequestStatus.RECEIVED, "Initial request created");

        log.info("Service request created with token: {}", savedRequest.getToken());
        return ServiceRequestResponse.fromEntity(savedRequest);
    }

    /**
     * Get service request by token
     */
    @Transactional(readOnly = true)
    public ServiceRequestResponse getByToken(String token) {
        log.debug("Fetching service request by token: {}", token);
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("Service request", "token", token));
        
        return ServiceRequestResponse.fromEntity(request);
    }

    /**
     * Cancel service request by token
     */
    @Transactional
    public void cancelByToken(String token) {
        log.info("Cancelling service request with token: {}", token);
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("Service request", "token", token));

        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed request");
        }

        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessException("Request is already cancelled");
        }

        RequestStatus previousStatus = request.getStatus();
        request.setStatus(RequestStatus.CANCELLED);
        serviceRequestRepository.save(request);

        createStatusHistory(request, previousStatus, RequestStatus.CANCELLED, "Cancelled by citizen");
        log.info("Service request cancelled: {}", token);
    }

    /**
     * Get all service requests (for staff)
     */
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getAllRequests(String municipalityName) {
        log.debug("Fetching all service requests for municipality: {}", municipalityName);
        
        List<ServiceRequest> requests;
        if (municipalityName != null && !municipalityName.isBlank()) {
            requests = serviceRequestRepository.findByMunicipalityName(municipalityName);
        } else {
            requests = serviceRequestRepository.findAllByOrderByCreatedAtDesc();
        }

        return requests.stream()
            .map(ServiceRequestResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Update service request status (for staff)
     */
    @Transactional
    public ServiceRequestResponse updateStatus(Long id, UpdateStatusRequest updateRequest) {
        log.info("Updating status for request ID: {} to {}", id, updateRequest.getNewStatus());
        
        ServiceRequest request = serviceRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service request", "id", id));

        // Business Rule: Validate status transition
        validateStatusTransition(request.getStatus(), updateRequest.getNewStatus());

        RequestStatus previousStatus = request.getStatus();
        request.setStatus(updateRequest.getNewStatus());
        ServiceRequest updatedRequest = serviceRequestRepository.save(request);

        createStatusHistory(updatedRequest, previousStatus, updateRequest.getNewStatus(), 
            updateRequest.getNotes());

        log.info("Status updated successfully for request ID: {}", id);
        return ServiceRequestResponse.fromEntity(updatedRequest);
    }

    /**
     * Validate status transition rules
     */
    private void validateStatusTransition(RequestStatus current, RequestStatus newStatus) {
        if (current == RequestStatus.COMPLETED) {
            throw new BusinessException("Cannot change status of completed request");
        }

        if (current == RequestStatus.CANCELLED && newStatus != RequestStatus.RECEIVED) {
            throw new BusinessException("Can only reopen cancelled requests to RECEIVED status");
        }

        // Define valid transitions
        boolean validTransition = switch (current) {
            case RECEIVED -> newStatus == RequestStatus.ASSIGNED || 
                           newStatus == RequestStatus.CANCELLED;
            case ASSIGNED -> newStatus == RequestStatus.IN_PROGRESS || 
                           newStatus == RequestStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == RequestStatus.COMPLETED || 
                              newStatus == RequestStatus.CANCELLED;
            case CANCELLED -> newStatus == RequestStatus.RECEIVED;
            case COMPLETED -> false;
        };

        if (!validTransition) {
            throw new BusinessException(
                String.format("Invalid status transition from %s to %s", current, newStatus)
            );
        }
    }

    /**
     * Create status history entry
     */
    private void createStatusHistory(ServiceRequest request, RequestStatus previousStatus, 
                                     RequestStatus newStatus, String notes) {
        StatusHistory history = new StatusHistory();
        history.setServiceRequest(request);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setTimestamp(LocalDateTime.now());
        history.setNotes(notes);
        statusHistoryRepository.save(history);
    }
}

