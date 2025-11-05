package com.zeremonos.wastecollection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.zeremonos.wastecollection.dto.MunicipalityDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MunicipalityServiceTest {

    private WireMockServer wireMockServer;
    private MunicipalityService municipalityService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8089")
                .build();

        municipalityService = new MunicipalityService(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testGetAllMunicipalities_Success() throws Exception {
        // GeoAPI.pt returns a simple array of strings
        String[] mockMunicipalities = {"Lisboa", "Porto", "Braga"};

        String jsonResponse = objectMapper.writeValueAsString(mockMunicipalities);

        stubFor(get(urlEqualTo("/municipios"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)));

        List<MunicipalityDTO> result = municipalityService.getAllMunicipalities();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("Lisboa");
        assertThat(result.get(1).getName()).isEqualTo("Porto");
        assertThat(result.get(2).getName()).isEqualTo("Braga");
        // Check that codes are generated
        assertThat(result.get(0).getCode()).isNotNull().isNotEmpty();
        assertThat(result.get(1).getCode()).isNotNull().isNotEmpty();
        assertThat(result.get(2).getCode()).isNotNull().isNotEmpty();

        verify(getRequestedFor(urlEqualTo("/municipios")));
    }

    @Test
    void testGetAllMunicipalities_EmptyResponse() {
        stubFor(get(urlEqualTo("/municipios"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        List<MunicipalityDTO> result = municipalityService.getAllMunicipalities();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllMunicipalities_ApiError() {
        stubFor(get(urlEqualTo("/municipios"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        assertThrows(RuntimeException.class, () -> {
            municipalityService.getAllMunicipalities();
        });
    }

    @Test
    void testGetAllMunicipalities_Timeout() {
        stubFor(get(urlEqualTo("/municipios"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                        .withFixedDelay(15000)));

        assertThrows(RuntimeException.class, () -> {
            municipalityService.getAllMunicipalities();
        });
    }

    @Test
    void testGetAllMunicipalities_NetworkError() {
        stubFor(get(urlEqualTo("/municipios"))
                .willReturn(aResponse()
                        .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));

        assertThrows(RuntimeException.class, () -> {
            municipalityService.getAllMunicipalities();
        });
    }
}

