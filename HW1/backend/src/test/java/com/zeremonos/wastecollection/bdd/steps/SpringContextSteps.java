package com.zeremonos.wastecollection.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.frontend.url=http://localhost:5173"
})
public class SpringContextSteps {

    @LocalServerPort
    private int port;

    @Given("the application is running")
    public void the_application_is_running() {
        // Application is started by Spring Boot Test
        // Backend will be available at http://localhost:{port}
        // Frontend is expected to be running at http://localhost:5173
        System.out.println("Backend running on port: " + port);
    }
}

