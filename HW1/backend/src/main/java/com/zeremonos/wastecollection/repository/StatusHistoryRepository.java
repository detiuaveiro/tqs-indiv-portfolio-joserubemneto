package com.zeremonos.wastecollection.repository;

import com.zeremonos.wastecollection.model.ServiceRequest;
import com.zeremonos.wastecollection.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    /**
     * Find all status history entries for a specific service request
     * @param serviceRequest the service request
     * @return List of status history entries ordered by timestamp descending
     */
    List<StatusHistory> findByServiceRequestOrderByTimestampDesc(ServiceRequest serviceRequest);

    /**
     * Find all status history entries for a service request by its ID
     * @param serviceRequestId the ID of the service request
     * @return List of status history entries ordered by timestamp descending
     */
    List<StatusHistory> findByServiceRequestIdOrderByTimestampDesc(Long serviceRequestId);

    /**
     * Find status history entries within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return List of status history entries
     */
    List<StatusHistory> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count total status changes for a service request
     * @param serviceRequest the service request
     * @return count of status changes
     */
    Long countByServiceRequest(ServiceRequest serviceRequest);
}

