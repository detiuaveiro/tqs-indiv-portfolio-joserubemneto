package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static io.gatling.javaapi.http.HttpDsl.http;

public abstract class BaseSimulation extends Simulation {

    protected static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    
    protected static final String[] MUNICIPALITY_CODES = {"LISB34", "PORT45", "CBRN56", "AVRO67", "FARO78"};
    protected static final String[] MUNICIPALITY_NAMES = {"Lisboa", "Porto", "Coimbra", "Aveiro", "Faro"};
    protected static final String[] TIME_SLOTS = {"MORNING", "AFTERNOON", "EVENING"};
    protected static final String[] STATUSES = {"RECEIVED", "ASSIGNED", "IN_PROGRESS", "COMPLETED"};
    
    protected static final Random random = new Random();
    
    protected HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling Performance Test");

    protected static String randomCitizenName() {
        String[] firstNames = {"João", "Maria", "Pedro", "Ana", "Carlos", "Sofia", "Miguel", "Beatriz", "Ricardo", "Inês"};
        String[] lastNames = {"Silva", "Santos", "Ferreira", "Oliveira", "Costa", "Rodrigues", "Martins", "Sousa", "Pereira", "Carvalho"};
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }

    protected static String randomEmail() {
        return "test.user" + random.nextInt(100000) + "@example.com";
    }

    protected static String randomPhone() {
        return "9" + (random.nextInt(90000000) + 10000000);
    }

    protected static String randomAddress() {
        String[] streets = {"Rua das Flores", "Avenida da Liberdade", "Rua do Comércio", "Rua de São Pedro", "Avenida Central"};
        int number = random.nextInt(500) + 1;
        int postalCode = random.nextInt(9000) + 1000;
        String municipality = MUNICIPALITY_NAMES[random.nextInt(MUNICIPALITY_NAMES.length)];
        return streets[random.nextInt(streets.length)] + ", " + number + ", " + postalCode + "-000 " + municipality;
    }

    protected static String randomItemDescription() {
        String[] items = {
            "Large sofa that needs to be collected from apartment on 3rd floor",
            "Old refrigerator, working condition, needs proper disposal",
            "Wooden furniture including table and 4 chairs from office renovation",
            "Electronic waste including old TV, computer monitor and cables",
            "Garden waste including branches, leaves and soil from landscaping",
            "Construction debris including wood, tiles and small amount of concrete",
            "Household items including mattress, carpet and various plastic containers",
            "Metal items including old bicycle, filing cabinet and shelving units"
        };
        return items[random.nextInt(items.length)];
    }

    protected static String randomMunicipalityCode() {
        return MUNICIPALITY_CODES[random.nextInt(MUNICIPALITY_CODES.length)];
    }

    protected static String randomMunicipalityName() {
        return MUNICIPALITY_NAMES[random.nextInt(MUNICIPALITY_NAMES.length)];
    }

    protected static String randomTimeSlot() {
        return TIME_SLOTS[random.nextInt(TIME_SLOTS.length)];
    }

    protected static String randomStatus() {
        return STATUSES[random.nextInt(STATUSES.length)];
    }

    protected static String futureDate() {
        LocalDate date = LocalDate.now().plusDays(random.nextInt(8) + 3);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    protected static String generateServiceRequestBody() {
        String municipalityCode = randomMunicipalityCode();
        int index = java.util.Arrays.asList(MUNICIPALITY_CODES).indexOf(municipalityCode);
        String municipalityName = MUNICIPALITY_NAMES[index];
        
        return String.format("""
            {
                "municipalityCode": "%s",
                "municipalityName": "%s",
                "citizenName": "%s",
                "citizenEmail": "%s",
                "citizenPhone": "%s",
                "pickupAddress": "%s",
                "itemDescription": "%s",
                "preferredDate": "%s",
                "preferredTimeSlot": "%s"
            }
            """,
            municipalityCode,
            municipalityName,
            randomCitizenName(),
            randomEmail(),
            randomPhone(),
            randomAddress(),
            randomItemDescription(),
            futureDate(),
            randomTimeSlot()
        );
    }

    protected static String generateUpdateStatusBody() {
        return String.format("""
            {
                "newStatus": "%s",
                "notes": "Performance test status update - %s"
            }
            """,
            randomStatus(),
            System.currentTimeMillis()
        );
    }
}

