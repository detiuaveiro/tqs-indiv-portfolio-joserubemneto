package com.zeremonos.wastecollection.service;

import com.zeremonos.wastecollection.dto.MunicipalityDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class MunicipalityService {

    private final WebClient geoApiWebClient;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    public MunicipalityService(@Qualifier("geoApiWebClient") WebClient geoApiWebClient) {
        this.geoApiWebClient = geoApiWebClient;
    }

    @Cacheable("municipalities")
    public List<MunicipalityDTO> getAllMunicipalities() {
        log.info("Fetching municipalities from GeoAPI.pt");
        
        try {
            // GeoAPI.pt returns a simple array of strings (municipality names)
            String[] municipalityNames = geoApiWebClient
                    .get()
                    .uri("/municipios")
                    .retrieve()
                    .bodyToMono(String[].class)
                    .timeout(TIMEOUT)
                    .block();

            if (municipalityNames == null || municipalityNames.length == 0) {
                log.warn("No municipalities returned from GeoAPI.pt");
                return Collections.emptyList();
            }

            List<MunicipalityDTO> municipalities = Arrays.stream(municipalityNames)
                    .map(MunicipalityDTO::new)
                    .toList();

            log.info("Successfully fetched {} municipalities", municipalities.size());
            return municipalities;
            
        } catch (Exception e) {
            log.error("Error fetching municipalities from GeoAPI.pt: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch municipalities from external API", e);
        }
    }

    public Mono<List<MunicipalityDTO>> getAllMunicipalitiesAsync() {
        log.debug("Fetching municipalities from GeoAPI.pt (async)");
        
        return geoApiWebClient
                .get()
                .uri("/municipios")
                .retrieve()
                .bodyToFlux(String.class)
                .map(MunicipalityDTO::new)
                .timeout(TIMEOUT)
                .collectList()
                .doOnSuccess(list -> log.info("Successfully fetched {} municipalities (async)", list.size()))
                .doOnError(e -> log.error("Error fetching municipalities (async): {}", e.getMessage()));
    }
}

