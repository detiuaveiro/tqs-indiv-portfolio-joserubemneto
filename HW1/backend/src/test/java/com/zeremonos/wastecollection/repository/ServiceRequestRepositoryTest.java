package com.zeremonos.wastecollection.repository;

import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.ServiceRequest;
import com.zeremonos.wastecollection.model.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ServiceRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ServiceRequestRepository repository;

    private ServiceRequest serviceRequest1;
    private ServiceRequest serviceRequest2;

    @BeforeEach
    void setUp() {
        serviceRequest1 = new ServiceRequest();
        serviceRequest1.setMunicipalityCode("1301");
        serviceRequest1.setMunicipalityName("Lisboa");
        serviceRequest1.setCitizenName("João Silva");
        serviceRequest1.setCitizenEmail("joao@example.com");
        serviceRequest1.setCitizenPhone("912345678");
        serviceRequest1.setPickupAddress("Rua Example, 123");
        serviceRequest1.setItemDescription("Old mattress");
        serviceRequest1.setPreferredDate(LocalDate.now().plusDays(5));
        serviceRequest1.setPreferredTimeSlot(TimeSlot.MORNING);
        serviceRequest1 = entityManager.persistAndFlush(serviceRequest1);

        serviceRequest2 = new ServiceRequest();
        serviceRequest2.setMunicipalityCode("0901");
        serviceRequest2.setMunicipalityName("Porto");
        serviceRequest2.setCitizenName("Maria Santos");
        serviceRequest2.setCitizenEmail("maria@example.com");
        serviceRequest2.setCitizenPhone("923456789");
        serviceRequest2.setPickupAddress("Avenida Test, 456");
        serviceRequest2.setItemDescription("Broken refrigerator");
        serviceRequest2.setPreferredDate(LocalDate.now().plusDays(7));
        serviceRequest2.setPreferredTimeSlot(TimeSlot.AFTERNOON);
        serviceRequest2 = entityManager.persistAndFlush(serviceRequest2);
    }

    @Test
    void testFindByToken() {
        Optional<ServiceRequest> found = repository.findByToken(serviceRequest1.getToken());
        
        assertThat(found).isPresent();
        assertThat(found.get().getCitizenName()).isEqualTo("João Silva");
    }

    @Test
    void testFindByToken_NotFound() {
        Optional<ServiceRequest> found = repository.findByToken("non-existent-token");
        
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByMunicipalityName() {
        List<ServiceRequest> requests = repository.findByMunicipalityName("Lisboa");
        
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getCitizenName()).isEqualTo("João Silva");
    }

    @Test
    void testFindByStatus() {
        List<ServiceRequest> requests = repository.findByStatus(RequestStatus.RECEIVED);
        
        assertThat(requests).hasSize(2);
    }

    @Test
    void testFindByMunicipalityNameAndStatus() {
        List<ServiceRequest> requests = repository.findByMunicipalityNameAndStatus(
            "Lisboa", RequestStatus.RECEIVED);
        
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getMunicipalityName()).isEqualTo("Lisboa");
    }

    @Test
    void testCountByMunicipalityNameAndPreferredDate() {
        LocalDate targetDate = LocalDate.now().plusDays(5);
        Long count = repository.countByMunicipalityNameAndPreferredDate("Lisboa", targetDate);
        
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void testFindAllByOrderByCreatedAtDesc() {
        List<ServiceRequest> requests = repository.findAllByOrderByCreatedAtDesc();
        
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getId()).isGreaterThan(requests.get(1).getId());
    }

    @Test
    void testExistsByToken() {
        boolean exists = repository.existsByToken(serviceRequest1.getToken());
        
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByToken_NotFound() {
        boolean exists = repository.existsByToken("non-existent-token");
        
        assertThat(exists).isFalse();
    }

    @Test
    void testCountActiveRequestsByMunicipalityAndDate() {
        LocalDate targetDate = LocalDate.now().plusDays(5);
        Long count = repository.countActiveRequestsByMunicipalityAndDate("Lisboa", targetDate);
        
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void testCountActiveRequestsByMunicipalityAndDate_ExcludesCancelled() {
        serviceRequest1.updateStatus(RequestStatus.CANCELLED, "Cancelled by user");
        entityManager.persistAndFlush(serviceRequest1);
        
        LocalDate targetDate = LocalDate.now().plusDays(5);
        Long count = repository.countActiveRequestsByMunicipalityAndDate("Lisboa", targetDate);
        
        assertThat(count).isZero();
    }
}

