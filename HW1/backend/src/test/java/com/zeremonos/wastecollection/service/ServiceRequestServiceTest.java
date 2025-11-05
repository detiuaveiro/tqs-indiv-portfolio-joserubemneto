package com.zeremonos.wastecollection.service;

import com.zeremonos.wastecollection.dto.ServiceRequestDTO;
import com.zeremonos.wastecollection.dto.ServiceRequestResponse;
import com.zeremonos.wastecollection.dto.UpdateStatusRequest;
import com.zeremonos.wastecollection.exception.BusinessException;
import com.zeremonos.wastecollection.exception.ResourceNotFoundException;
import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.ServiceRequest;
import com.zeremonos.wastecollection.model.TimeSlot;
import com.zeremonos.wastecollection.repository.ServiceRequestRepository;
import com.zeremonos.wastecollection.repository.StatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceRequestService - Business Rules Tests")
class ServiceRequestServiceTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @InjectMocks
    private ServiceRequestService serviceRequestService;

    private ServiceRequestDTO validDTO;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(serviceRequestService, "maxRequestsPerMunicipalityPerDay", 10);

        validDTO = new ServiceRequestDTO();
        validDTO.setMunicipalityCode("1106");
        validDTO.setMunicipalityName("Lisboa");
        validDTO.setCitizenName("João Silva");
        validDTO.setCitizenEmail("joao@example.com");
        validDTO.setCitizenPhone("912345678");
        validDTO.setPickupAddress("Rua Example, 123");
        validDTO.setItemDescription("Old refrigerator and washing machine");
        validDTO.setPreferredDate(LocalDate.now().plusDays(5));
        validDTO.setPreferredTimeSlot(TimeSlot.MORNING);
    }

    @Test
    @DisplayName("Should create service request successfully with valid data")
    void testCreateServiceRequest_Success() {
        when(serviceRequestRepository.countActiveRequestsByMunicipalityAndDate(anyString(), any()))
            .thenReturn(5L);
        
        ServiceRequest savedRequest = createMockServiceRequest();
        when(serviceRequestRepository.save(any(ServiceRequest.class)))
            .thenReturn(savedRequest);

        ServiceRequestResponse response = serviceRequestService.createServiceRequest(validDTO);

        assertThat(response).isNotNull();
        assertThat(response.getMunicipalityName()).isEqualTo("Lisboa");
        assertThat(response.getStatus()).isEqualTo(RequestStatus.RECEIVED);
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getCitizenName()).isEqualTo("João Silva");

        verify(serviceRequestRepository).countActiveRequestsByMunicipalityAndDate("Lisboa", validDTO.getPreferredDate());
        verify(serviceRequestRepository).save(any(ServiceRequest.class));
        verify(statusHistoryRepository).save(any());
    }

    @Test
    @DisplayName("Should reject request with past date")
    void testCreateServiceRequest_PastDate_ThrowsException() {
        validDTO.setPreferredDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> serviceRequestService.createServiceRequest(validDTO))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("past");

        verify(serviceRequestRepository, never()).save(any());
        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject request when daily limit is reached")
    void testCreateServiceRequest_ExceedsDailyLimit_ThrowsException() {
        when(serviceRequestRepository.countActiveRequestsByMunicipalityAndDate(anyString(), any()))
            .thenReturn(10L); // At limit

        assertThatThrownBy(() -> serviceRequestService.createServiceRequest(validDTO))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Daily limit reached")
            .hasMessageContaining("Lisboa");

        verify(serviceRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject request when daily limit is exceeded")
    void testCreateServiceRequest_ExceedsDailyLimitByOne_ThrowsException() {
        when(serviceRequestRepository.countActiveRequestsByMunicipalityAndDate(anyString(), any()))
            .thenReturn(11L); // Over limit

        assertThatThrownBy(() -> serviceRequestService.createServiceRequest(validDTO))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Daily limit reached");

        verify(serviceRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow request when just under daily limit")
    void testCreateServiceRequest_JustUnderDailyLimit_Success() {
        when(serviceRequestRepository.countActiveRequestsByMunicipalityAndDate(anyString(), any()))
            .thenReturn(9L); // One below limit
        
        ServiceRequest savedRequest = createMockServiceRequest();
        when(serviceRequestRepository.save(any(ServiceRequest.class)))
            .thenReturn(savedRequest);

        ServiceRequestResponse response = serviceRequestService.createServiceRequest(validDTO);

        assertThat(response).isNotNull();
        verify(serviceRequestRepository).save(any(ServiceRequest.class));
    }

    @Test
    @DisplayName("Should retrieve request by valid token")
    void testGetByToken_Success() {
        ServiceRequest mockRequest = createMockServiceRequest();
        when(serviceRequestRepository.findByToken("valid-token"))
            .thenReturn(Optional.of(mockRequest));

        ServiceRequestResponse response = serviceRequestService.getByToken("valid-token");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("valid-token");
        assertThat(response.getMunicipalityName()).isEqualTo("Lisboa");
        
        verify(serviceRequestRepository).findByToken("valid-token");
    }

    @Test
    @DisplayName("Should throw exception when token not found")
    void testGetByToken_NotFound_ThrowsException() {
        when(serviceRequestRepository.findByToken("invalid-token"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceRequestService.getByToken("invalid-token"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("token")
            .hasMessageContaining("invalid-token");
        
        verify(serviceRequestRepository).findByToken("invalid-token");
    }

    @Test
    @DisplayName("Should cancel request successfully")
    void testCancelByToken_Success() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.RECEIVED);
        when(serviceRequestRepository.findByToken("test-token"))
            .thenReturn(Optional.of(mockRequest));

        serviceRequestService.cancelByToken("test-token");

        assertThat(mockRequest.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        verify(serviceRequestRepository).save(mockRequest);
        verify(statusHistoryRepository).save(any());
    }

    @Test
    @DisplayName("Should not cancel already completed request")
    void testCancelByToken_AlreadyCompleted_ThrowsException() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.COMPLETED);
        when(serviceRequestRepository.findByToken("test-token"))
            .thenReturn(Optional.of(mockRequest));

        assertThatThrownBy(() -> serviceRequestService.cancelByToken("test-token"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot cancel a completed request");
        
        verify(serviceRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not cancel already cancelled request")
    void testCancelByToken_AlreadyCancelled_ThrowsException() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.CANCELLED);
        when(serviceRequestRepository.findByToken("test-token"))
            .thenReturn(Optional.of(mockRequest));

        assertThatThrownBy(() -> serviceRequestService.cancelByToken("test-token"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already cancelled");
        
        verify(serviceRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel request in ASSIGNED state")
    void testCancelByToken_AssignedState_Success() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.ASSIGNED);
        when(serviceRequestRepository.findByToken("test-token"))
            .thenReturn(Optional.of(mockRequest));

        serviceRequestService.cancelByToken("test-token");

        assertThat(mockRequest.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        verify(serviceRequestRepository).save(mockRequest);
    }

    @Test
    @DisplayName("Should cancel request in IN_PROGRESS state")
    void testCancelByToken_InProgressState_Success() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.IN_PROGRESS);
        when(serviceRequestRepository.findByToken("test-token"))
            .thenReturn(Optional.of(mockRequest));

        serviceRequestService.cancelByToken("test-token");

        assertThat(mockRequest.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        verify(serviceRequestRepository).save(mockRequest);
    }

    @Test
    @DisplayName("Should update status from RECEIVED to ASSIGNED")
    void testUpdateStatus_ReceivedToAssigned_Success() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.RECEIVED);
        when(serviceRequestRepository.findById(1L))
            .thenReturn(Optional.of(mockRequest));
        when(serviceRequestRepository.save(any()))
            .thenReturn(mockRequest);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest(RequestStatus.ASSIGNED, "Assigned to team A");
        ServiceRequestResponse response = serviceRequestService.updateStatus(1L, updateRequest);

        assertThat(response.getStatus()).isEqualTo(RequestStatus.ASSIGNED);
        verify(statusHistoryRepository).save(any());
        verify(serviceRequestRepository).save(mockRequest);
    }

    @Test
    @DisplayName("Should update status from ASSIGNED to IN_PROGRESS")
    void testUpdateStatus_AssignedToInProgress_Success() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.ASSIGNED);
        when(serviceRequestRepository.findById(1L))
            .thenReturn(Optional.of(mockRequest));
        when(serviceRequestRepository.save(any()))
            .thenReturn(mockRequest);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest(RequestStatus.IN_PROGRESS, "Collection started");
        ServiceRequestResponse response = serviceRequestService.updateStatus(1L, updateRequest);

        assertThat(response.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        verify(statusHistoryRepository).save(any());
    }

    @Test
    @DisplayName("Should update status from IN_PROGRESS to COMPLETED")
    void testUpdateStatus_InProgressToCompleted_Success() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.IN_PROGRESS);
        when(serviceRequestRepository.findById(1L))
            .thenReturn(Optional.of(mockRequest));
        when(serviceRequestRepository.save(any()))
            .thenReturn(mockRequest);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest(RequestStatus.COMPLETED, "Collection done");
        ServiceRequestResponse response = serviceRequestService.updateStatus(1L, updateRequest);

        assertThat(response.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        verify(statusHistoryRepository).save(any());
    }

    @Test
    @DisplayName("Should reject invalid transition from RECEIVED to COMPLETED")
    void testUpdateStatus_InvalidTransitionReceivedToCompleted_ThrowsException() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.RECEIVED);
        when(serviceRequestRepository.findById(1L))
            .thenReturn(Optional.of(mockRequest));

        UpdateStatusRequest updateRequest = new UpdateStatusRequest(RequestStatus.COMPLETED, null);

        assertThatThrownBy(() -> serviceRequestService.updateStatus(1L, updateRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Invalid status transition")
            .hasMessageContaining("RECEIVED")
            .hasMessageContaining("COMPLETED");
        
        verify(serviceRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject invalid transition from RECEIVED to IN_PROGRESS")
    void testUpdateStatus_InvalidTransitionReceivedToInProgress_ThrowsException() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.RECEIVED);
        when(serviceRequestRepository.findById(1L))
            .thenReturn(Optional.of(mockRequest));

        UpdateStatusRequest updateRequest = new UpdateStatusRequest(RequestStatus.IN_PROGRESS, null);

        assertThatThrownBy(() -> serviceRequestService.updateStatus(1L, updateRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Invalid status transition");
        
        verify(serviceRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject update of completed request")
    void testUpdateStatus_CompletedRequest_ThrowsException() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.COMPLETED);
        when(serviceRequestRepository.findById(1L))
            .thenReturn(Optional.of(mockRequest));

        UpdateStatusRequest updateRequest = new UpdateStatusRequest(RequestStatus.ASSIGNED, null);

        assertThatThrownBy(() -> serviceRequestService.updateStatus(1L, updateRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot change status of completed request");
        
        verify(serviceRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow reopening cancelled request")
    void testUpdateStatus_ReopenCancelled_Success() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.CANCELLED);
        when(serviceRequestRepository.findById(1L))
            .thenReturn(Optional.of(mockRequest));
        when(serviceRequestRepository.save(any()))
            .thenReturn(mockRequest);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest(RequestStatus.RECEIVED, "Reopened");
        ServiceRequestResponse response = serviceRequestService.updateStatus(1L, updateRequest);

        assertThat(response.getStatus()).isEqualTo(RequestStatus.RECEIVED);
        verify(statusHistoryRepository).save(any());
    }

    @Test
    @DisplayName("Should reject invalid cancelled transition to ASSIGNED")
    void testUpdateStatus_CancelledToAssigned_ThrowsException() {
        ServiceRequest mockRequest = createMockServiceRequest();
        mockRequest.setStatus(RequestStatus.CANCELLED);
        when(serviceRequestRepository.findById(1L))
            .thenReturn(Optional.of(mockRequest));

        UpdateStatusRequest updateRequest = new UpdateStatusRequest(RequestStatus.ASSIGNED, null);

        assertThatThrownBy(() -> serviceRequestService.updateStatus(1L, updateRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Can only reopen cancelled requests to RECEIVED status");
        
        verify(serviceRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent request")
    void testUpdateStatus_RequestNotFound_ThrowsException() {
        when(serviceRequestRepository.findById(999L))
            .thenReturn(Optional.empty());

        UpdateStatusRequest updateRequest = new UpdateStatusRequest(RequestStatus.ASSIGNED, null);

        assertThatThrownBy(() -> serviceRequestService.updateStatus(999L, updateRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");
        
        verify(serviceRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve all requests without filter")
    void testGetAllRequests_NoFilter_Success() {
        List<ServiceRequest> mockRequests = Arrays.asList(
            createMockServiceRequest(),
            createMockServiceRequest(),
            createMockServiceRequest()
        );
        when(serviceRequestRepository.findAllByOrderByCreatedAtDesc())
            .thenReturn(mockRequests);

        List<ServiceRequestResponse> responses = serviceRequestService.getAllRequests(null);

        assertThat(responses).hasSize(3);
        verify(serviceRequestRepository).findAllByOrderByCreatedAtDesc();
        verify(serviceRequestRepository, never()).findByMunicipalityName(anyString());
    }

    @Test
    @DisplayName("Should retrieve requests filtered by municipality")
    void testGetAllRequests_WithMunicipalityFilter_Success() {
        List<ServiceRequest> mockRequests = Arrays.asList(
            createMockServiceRequest(),
            createMockServiceRequest()
        );
        when(serviceRequestRepository.findByMunicipalityName("Lisboa"))
            .thenReturn(mockRequests);

        List<ServiceRequestResponse> responses = serviceRequestService.getAllRequests("Lisboa");

        assertThat(responses).hasSize(2);
        verify(serviceRequestRepository).findByMunicipalityName("Lisboa");
        verify(serviceRequestRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Should handle empty municipality filter as no filter")
    void testGetAllRequests_EmptyMunicipalityFilter_ReturnsAll() {
        List<ServiceRequest> mockRequests = Arrays.asList(createMockServiceRequest());
        when(serviceRequestRepository.findAllByOrderByCreatedAtDesc())
            .thenReturn(mockRequests);

        List<ServiceRequestResponse> responses = serviceRequestService.getAllRequests("");

        assertThat(responses).hasSize(1);
        verify(serviceRequestRepository).findAllByOrderByCreatedAtDesc();
    }

    private ServiceRequest createMockServiceRequest() {
        ServiceRequest request = new ServiceRequest();
        request.setId(1L);
        request.setToken("valid-token");
        request.setMunicipalityCode("1106");
        request.setMunicipalityName("Lisboa");
        request.setCitizenName("João Silva");
        request.setCitizenEmail("joao@example.com");
        request.setCitizenPhone("912345678");
        request.setPickupAddress("Rua Example, 123");
        request.setItemDescription("Old refrigerator");
        request.setPreferredDate(LocalDate.now().plusDays(5));
        request.setPreferredTimeSlot(TimeSlot.MORNING);
        request.setStatus(RequestStatus.RECEIVED);
        return request;
    }
}

