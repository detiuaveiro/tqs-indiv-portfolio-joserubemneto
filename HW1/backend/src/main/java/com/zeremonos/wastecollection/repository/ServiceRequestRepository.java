package com.zeremonos.wastecollection.repository;

import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    /**
     * Find a service request by its unique token
     * @param token the access token
     * @return Optional containing the service request if found
     */
    Optional<ServiceRequest> findByToken(String token);

    /**
     * Find all service requests for a specific municipality
     * @param municipalityName the name of the municipality
     * @return List of service requests
     */
    List<ServiceRequest> findByMunicipalityName(String municipalityName);

    /**
     * Find all service requests with a specific status
     * @param status the request status
     * @return List of service requests
     */
    List<ServiceRequest> findByStatus(RequestStatus status);

    /**
     * Find all service requests for a municipality with a specific status
     * @param municipalityName the name of the municipality
     * @param status the request status
     * @return List of service requests
     */
    List<ServiceRequest> findByMunicipalityNameAndStatus(String municipalityName, RequestStatus status);

    /**
     * Find all service requests for a specific date
     * @param date the preferred date
     * @return List of service requests
     */
    List<ServiceRequest> findByPreferredDate(LocalDate date);

    /**
     * Count service requests for a specific municipality and date
     * @param municipalityName the name of the municipality
     * @param date the preferred date
     * @return count of service requests
     */
    Long countByMunicipalityNameAndPreferredDate(String municipalityName, LocalDate date);

    /**
     * Find all service requests ordered by creation date (most recent first)
     * @return List of service requests
     */
    List<ServiceRequest> findAllByOrderByCreatedAtDesc();

    /**
     * Find service requests by municipality ordered by creation date
     * @param municipalityName the name of the municipality
     * @return List of service requests
     */
    List<ServiceRequest> findByMunicipalityNameOrderByCreatedAtDesc(String municipalityName);

    /**
     * Check if a token already exists
     * @param token the access token
     * @return true if token exists, false otherwise
     */
    boolean existsByToken(String token);

    /**
     * Find service requests created after a specific date
     * @param date the date to compare
     * @return List of service requests
     */
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.createdAt >= :date ORDER BY sr.createdAt DESC")
    List<ServiceRequest> findRecentRequests(@Param("date") LocalDate date);

    /**
     * Count active (non-cancelled, non-completed) requests for a municipality on a specific date
     * @param municipalityName the name of the municipality
     * @param date the preferred date
     * @return count of active service requests
     */
    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.municipalityName = :municipality " +
           "AND sr.preferredDate = :date " +
           "AND sr.status NOT IN (com.zeremonos.wastecollection.model.RequestStatus.CANCELLED, " +
           "com.zeremonos.wastecollection.model.RequestStatus.COMPLETED)")
    Long countActiveRequestsByMunicipalityAndDate(@Param("municipality") String municipalityName, 
                                                   @Param("date") LocalDate date);
}

