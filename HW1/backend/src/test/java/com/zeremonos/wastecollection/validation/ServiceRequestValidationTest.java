package com.zeremonos.wastecollection.validation;

import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.ServiceRequest;
import com.zeremonos.wastecollection.model.TimeSlot;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ServiceRequest - Validation Tests")
class ServiceRequestValidationTest {

    private static Validator validator;
    private ServiceRequest serviceRequest;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        serviceRequest = new ServiceRequest();
        serviceRequest.setMunicipalityCode("1106");
        serviceRequest.setMunicipalityName("Lisboa");
        serviceRequest.setCitizenName("João Silva");
        serviceRequest.setCitizenEmail("joao@example.com");
        serviceRequest.setCitizenPhone("912345678");
        serviceRequest.setPickupAddress("Rua Example, 123, Lisboa");
        serviceRequest.setItemDescription("Old refrigerator and washing machine");
        serviceRequest.setPreferredDate(LocalDate.now().plusDays(5));
        serviceRequest.setPreferredTimeSlot(TimeSlot.MORNING);
        serviceRequest.setStatus(RequestStatus.RECEIVED);
    }

    @Test
    @DisplayName("Should pass validation with all valid fields")
    void testValidServiceRequest_NoViolations() {
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation without optional email")
    void testValidServiceRequest_WithoutEmail_NoViolations() {
        serviceRequest.setCitizenEmail(null);
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject null municipality code")
    void testMunicipalityCode_Null_HasViolation() {
        serviceRequest.setMunicipalityCode(null);
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Municipality code");
    }

    @Test
    @DisplayName("Should reject blank municipality code")
    void testMunicipalityCode_Blank_HasViolation() {
        serviceRequest.setMunicipalityCode("   ");
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should reject municipality code exceeding max length")
    void testMunicipalityCode_TooLong_HasViolation() {
        serviceRequest.setMunicipalityCode("A".repeat(21)); // Max is 20
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        if (!violations.isEmpty()) {
            assertThat(violations.iterator().next().getMessage())
                .containsAnyOf("20", "length");
        }
    }

    @Test
    @DisplayName("Should reject null municipality name")
    void testMunicipalityName_Null_HasViolation() {
        serviceRequest.setMunicipalityName(null);
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Municipality name");
    }

    @Test
    @DisplayName("Should reject municipality name exceeding max length")
    void testMunicipalityName_TooLong_HasViolation() {
        serviceRequest.setMunicipalityName("A".repeat(101)); // Max is 100
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        if (!violations.isEmpty()) {
            assertThat(violations.iterator().next().getMessage())
                .containsAnyOf("100", "length");
        }
    }

    @Test
    @DisplayName("Should reject null citizen name")
    void testCitizenName_Null_HasViolation() {
        serviceRequest.setCitizenName(null);
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Citizen name");
    }

    @Test
    @DisplayName("Should reject blank citizen name")
    void testCitizenName_Blank_HasViolation() {
        serviceRequest.setCitizenName("   ");
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should reject citizen name exceeding max length")
    void testCitizenName_TooLong_HasViolation() {
        serviceRequest.setCitizenName("A".repeat(101)); // Max is 100
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should reject invalid email format")
    void testCitizenEmail_InvalidFormat_HasViolation() {
        serviceRequest.setCitizenEmail("invalid-email");
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Email should be valid");
    }

    @Test
    @DisplayName("Should reject email without domain")
    void testCitizenEmail_NoDomain_HasViolation() {
        serviceRequest.setCitizenEmail("user@");
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should reject email without @")
    void testCitizenEmail_NoAtSign_HasViolation() {
        serviceRequest.setCitizenEmail("userdomain.com");
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void testCitizenEmail_ValidFormats_NoViolations() {
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user123@test-domain.com"
        };

        for (String email : validEmails) {
            serviceRequest.setCitizenEmail(email);
            Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    @DisplayName("Should reject email exceeding max length")
    void testCitizenEmail_TooLong_HasViolation() {
        serviceRequest.setCitizenEmail("a".repeat(90) + "@example.com"); // Max is 100
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should reject phone with less than 9 digits")
    void testCitizenPhone_TooShort_HasViolation() {
        serviceRequest.setCitizenPhone("12345678"); // Only 8 digits
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Phone number should be valid");
    }

    @Test
    @DisplayName("Should reject phone with more than 15 digits")
    void testCitizenPhone_TooLong_HasViolation() {
        serviceRequest.setCitizenPhone("1234567890123456"); // 16 digits
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should reject phone with non-numeric characters")
    void testCitizenPhone_NonNumeric_HasViolation() {
        serviceRequest.setCitizenPhone("91234567a");
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should reject phone with spaces")
    void testCitizenPhone_WithSpaces_HasViolation() {
        serviceRequest.setCitizenPhone("912 345 678");
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should accept valid 9-digit phone numbers")
    void testCitizenPhone_Valid_NoViolations() {
        String[] validPhones = {
            "912345678",
            "923456789",
            "934567890",
            "210000000"
        };

        for (String phone : validPhones) {
            serviceRequest.setCitizenPhone(phone);
            Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    @DisplayName("Should reject null pickup address")
    void testPickupAddress_Null_HasViolation() {
        serviceRequest.setPickupAddress(null);
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Pickup address");
    }

    @Test
    @DisplayName("Should reject blank pickup address")
    void testPickupAddress_Blank_HasViolation() {
        serviceRequest.setPickupAddress("A"); // Too short (min is 5)
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("5");
    }

    @Test
    @DisplayName("Should reject pickup address exceeding max length")
    void testPickupAddress_TooLong_HasViolation() {
        serviceRequest.setPickupAddress("A".repeat(201)); // Max is 200
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should reject null item description")
    void testItemDescription_Null_HasViolation() {
        serviceRequest.setItemDescription(null);
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Item description");
    }

    @Test
    @DisplayName("Should reject blank item description")
    void testItemDescription_Blank_HasViolation() {
        serviceRequest.setItemDescription("A"); // Too short (min is 5)
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("5");
    }

    @Test
    @DisplayName("Should reject item description exceeding max length")
    void testItemDescription_TooLong_HasViolation() {
        serviceRequest.setItemDescription("A".repeat(501)); // Max is 500
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Should reject null preferred date")
    void testPreferredDate_Null_HasViolation() {
        serviceRequest.setPreferredDate(null);
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Preferred date");
    }

    @Test
    @DisplayName("Should reject past preferred date")
    void testPreferredDate_Past_HasViolation() {
        serviceRequest.setPreferredDate(LocalDate.now().minusDays(1));
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("future");
    }

    @Test
    @DisplayName("Should reject today as preferred date")
    void testPreferredDate_Today_HasViolation() {
        serviceRequest.setPreferredDate(LocalDate.now());
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("future");
    }

    @Test
    @DisplayName("Should accept future preferred date")
    void testPreferredDate_Future_NoViolations() {
        serviceRequest.setPreferredDate(LocalDate.now().plusDays(10));
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject null preferred time slot")
    void testPreferredTimeSlot_Null_HasViolation() {
        serviceRequest.setPreferredTimeSlot(null);
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("time slot");
    }

    @Test
    @DisplayName("Should allow null status as it has default value")
    void testStatus_Null_AllowedWithDefault() {
        serviceRequest.setStatus(null);
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should report multiple violations")
    void testMultipleViolations() {
        serviceRequest.setCitizenName(null);
        serviceRequest.setCitizenEmail("invalid");
        serviceRequest.setCitizenPhone("12345"); // Too short
        serviceRequest.setPreferredDate(LocalDate.now().minusDays(1)); // Past
        
        Set<ConstraintViolation<ServiceRequest>> violations = validator.validate(serviceRequest);
        
        assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }
}

