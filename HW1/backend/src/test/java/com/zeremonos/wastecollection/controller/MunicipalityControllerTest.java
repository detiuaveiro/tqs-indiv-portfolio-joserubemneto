package com.zeremonos.wastecollection.controller;

import com.zeremonos.wastecollection.dto.MunicipalityDTO;
import com.zeremonos.wastecollection.service.MunicipalityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MunicipalityController.class)
class MunicipalityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MunicipalityService municipalityService;

    @Test
    void testGetAllMunicipalities_Success() throws Exception {
        List<MunicipalityDTO> mockMunicipalities = Arrays.asList(
                new MunicipalityDTO("Lisboa", "LISB01"),
                new MunicipalityDTO("Porto", "PORT02"),
                new MunicipalityDTO("Braga", "BRAG03")
        );

        when(municipalityService.getAllMunicipalities()).thenReturn(mockMunicipalities);

        mockMvc.perform(get("/api/municipalities")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("Lisboa")))
                .andExpect(jsonPath("$[0].code", is("LISB01")))
                .andExpect(jsonPath("$[1].name", is("Porto")))
                .andExpect(jsonPath("$[2].name", is("Braga")));
    }

    @Test
    void testGetAllMunicipalities_EmptyList() throws Exception {
        when(municipalityService.getAllMunicipalities()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/municipalities")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAllMunicipalities_ServiceError() throws Exception {
        when(municipalityService.getAllMunicipalities())
                .thenThrow(new RuntimeException("External API error"));

        mockMvc.perform(get("/api/municipalities")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}

