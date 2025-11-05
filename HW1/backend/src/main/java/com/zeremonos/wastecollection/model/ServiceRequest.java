package com.zeremonos.wastecollection.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String token;

    @NotBlank(message = "Municipality code is required")
    @Column(nullable = false, length = 20)
    private String municipalityCode;

    @NotBlank(message = "Municipality name is required")
    @Column(nullable = false, length = 100)
    private String municipalityName;

    @NotBlank(message = "Citizen name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String citizenName;

    @Email(message = "Email should be valid")
    @Column(length = 100)
    private String citizenEmail;

    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "Phone number should be valid")
    @Column(length = 20)
    private String citizenPhone;

    @NotBlank(message = "Pickup address is required")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    @Column(nullable = false, length = 200)
    private String pickupAddress;

    @NotBlank(message = "Item description is required")
    @Size(min = 5, max = 500, message = "Description must be between 5 and 500 characters")
    @Column(nullable = false, length = 500)
    private String itemDescription;

    @NotNull(message = "Preferred date is required")
    @Future(message = "Preferred date must be in the future")
    @Column(nullable = false)
    private LocalDate preferredDate;

    @NotNull(message = "Preferred time slot is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TimeSlot preferredTimeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.RECEIVED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "serviceRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp DESC")
    private List<StatusHistory> statusHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.token == null) {
            this.token = UUID.randomUUID().toString();
        }
        addStatusChange(null, this.status, "Initial request received");
    }

    public void addStatusChange(RequestStatus previousStatus, RequestStatus newStatus, String notes) {
        StatusHistory history = new StatusHistory();
        history.setServiceRequest(this);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setNotes(notes);
        history.setTimestamp(LocalDateTime.now());
        this.statusHistory.add(history);
    }

    public void updateStatus(RequestStatus newStatus, String notes) {
        RequestStatus previousStatus = this.status;
        this.status = newStatus;
        addStatusChange(previousStatus, newStatus, notes);
    }
}

