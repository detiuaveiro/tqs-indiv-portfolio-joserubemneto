package com.zeremonos.wastecollection.bdd;

import com.zeremonos.wastecollection.WasteCollectionApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
    classes = WasteCollectionApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}

