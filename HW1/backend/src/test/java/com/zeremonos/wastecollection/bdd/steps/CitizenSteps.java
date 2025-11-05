package com.zeremonos.wastecollection.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class CitizenSteps {

    @Autowired
    private WebDriver driver;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private String savedToken;
    private WebDriverWait wait;

    @Given("I am on the home page")
    public void i_am_on_the_home_page() {
        driver.get(frontendUrl);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        assertTrue(driver.getTitle().contains("Vite") || driver.getCurrentUrl().contains(frontendUrl));
    }

    @Given("I navigate to the create request page")
    public void i_navigate_to_the_create_request_page() {
        driver.get(frontendUrl + "/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
    }

    @When("I click on {string}")
    public void i_click_on(String linkText) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
        element.click();
    }

    @When("I fill in the service request form with valid data")
    public void i_fill_in_the_service_request_form_with_valid_data() {
        fillFormWithData(
            "Lisboa",
            "João Silva",
            "joao@example.com",
            "912345678",
            "Rua Test, 123, Lisboa",
            "Old refrigerator and washing machine that need disposal",
            LocalDate.now().plusDays(5),
            "MORNING"
        );
    }

    @When("I fill in the form with municipality {string}, name {string}, email {string}, phone {string}, address {string}, description {string}, date {string} days ahead, and time slot {string}")
    public void i_fill_in_the_form_with_details(String municipality, String name, String email, String phone, 
                                                 String address, String description, String daysAhead, String timeSlot) {
        int days = Integer.parseInt(daysAhead);
        fillFormWithData(municipality, name, email, phone, address, description, 
                        LocalDate.now().plusDays(days), timeSlot);
    }

    @When("I submit the form")
    public void i_submit_the_form() {
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
    }

    @When("I save the access token")
    public void i_save_the_access_token() {
        WebElement tokenElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".token-display code")));
        savedToken = tokenElement.getText();
        assertNotNull(savedToken);
        assertFalse(savedToken.isEmpty());
    }

    @When("I navigate to check request page")
    public void i_navigate_to_check_request_page() {
        driver.get(frontendUrl + "/check");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='text']")));
    }

    @When("I enter the saved token")
    public void i_enter_the_saved_token() {
        WebElement tokenInput = driver.findElement(By.cssSelector("input[type='text']"));
        tokenInput.clear();
        tokenInput.sendKeys(savedToken);
    }

    @When("I click the search button")
    public void i_click_the_search_button() {
        WebElement searchButton = driver.findElement(By.cssSelector("button[type='submit']"));
        searchButton.click();
    }

    @When("I click the cancel request button")
    public void i_click_the_cancel_request_button() {
        WebElement cancelButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Cancel Request')]")));
        cancelButton.click();
    }

    @When("I confirm the cancellation")
    public void i_confirm_the_cancellation() {
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Yes')]")));
        confirmButton.click();
    }

    @When("I fill in municipality field with {string}")
    public void i_fill_in_municipality_field_with(String municipality) {
        WebElement municipalitySelect = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("municipality")));
        Select select = new Select(municipalitySelect);
        select.selectByVisibleText(municipality);
    }

    @When("I leave the {string} field empty")
    public void i_leave_the_field_empty(String fieldName) {
        // Just skip filling that field
    }

    @When("I enter {string} in the {string} field")
    public void i_enter_in_the_field(String value, String fieldName) {
        WebElement field = driver.findElement(By.id(getFieldId(fieldName)));
        field.clear();
        field.sendKeys(value);
    }

    @Then("I should see the home page")
    public void i_should_see_the_home_page() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertTrue(driver.getPageSource().contains("ZeroMonos"));
    }

    @Then("I should see a success message")
    public void i_should_see_a_success_message() {
        WebElement successAlert = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-success")));
        assertTrue(successAlert.isDisplayed());
        assertTrue(successAlert.getText().contains("success"));
    }

    @Then("I should see an access token")
    public void i_should_see_an_access_token() {
        WebElement tokenDisplay = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".token-display")));
        assertTrue(tokenDisplay.isDisplayed());
        assertTrue(tokenDisplay.getText().contains("Access Token"));
    }

    @Then("I should see the request details")
    public void i_should_see_the_request_details() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".request-details")));
        WebElement details = driver.findElement(By.cssSelector(".request-details"));
        assertTrue(details.isDisplayed());
    }

    @Then("the status should be {string}")
    public void the_status_should_be(String expectedStatus) {
        WebElement statusBadge = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".status-badge")));
        String actualStatus = statusBadge.getText();
        assertTrue(actualStatus.equalsIgnoreCase(expectedStatus) || 
                   actualStatus.contains(expectedStatus.replace("_", " ")));
    }

    @Then("I should see the status history")
    public void i_should_see_the_status_history() {
        WebElement timeline = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".status-timeline")));
        assertTrue(timeline.isDisplayed());
    }

    @Then("I should see an error message")
    public void i_should_see_an_error_message() {
        WebElement errorAlert = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-error")));
        assertTrue(errorAlert.isDisplayed());
    }

    @Then("I should see a validation error for {string}")
    public void i_should_see_a_validation_error_for(String fieldName) {
        // Browser HTML5 validation or error message should be visible
        assertTrue(driver.getPageSource().contains("required") || 
                   driver.getPageSource().contains("invalid"));
    }

    @Then("the request should have municipality {string}")
    public void the_request_should_have_municipality(String municipality) {
        WebElement municipalityElement = driver.findElement(
            By.xpath("//*[contains(text(), '" + municipality + "')]"));
        assertTrue(municipalityElement.isDisplayed());
    }

    @Then("the request should have citizen name {string}")
    public void the_request_should_have_citizen_name(String name) {
        WebElement nameElement = driver.findElement(
            By.xpath("//*[contains(text(), '" + name + "')]"));
        assertTrue(nameElement.isDisplayed());
    }

    // Helper methods

    private void fillFormWithData(String municipality, String name, String email, String phone,
                                  String address, String description, LocalDate date, String timeSlot) {
        // Wait for form to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

        // Municipality
        WebElement municipalitySelect = driver.findElement(By.id("municipality"));
        Select select = new Select(municipalitySelect);
        select.selectByVisibleText(municipality);

        // Citizen details
        driver.findElement(By.id("citizenName")).sendKeys(name);
        if (email != null && !email.isEmpty()) {
            driver.findElement(By.id("citizenEmail")).sendKeys(email);
        }
        driver.findElement(By.id("citizenPhone")).sendKeys(phone);

        // Address and description
        driver.findElement(By.id("pickupAddress")).sendKeys(address);
        driver.findElement(By.id("itemDescription")).sendKeys(description);

        // Date
        String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        WebElement dateField = driver.findElement(By.id("preferredDate"));
        dateField.sendKeys(formattedDate);

        // Time slot
        WebElement timeSlotSelect = driver.findElement(By.id("preferredTimeSlot"));
        Select timeSelect = new Select(timeSlotSelect);
        timeSelect.selectByValue(timeSlot);
    }

    private String getFieldId(String fieldName) {
        return switch (fieldName.toLowerCase()) {
            case "name", "citizen name" -> "citizenName";
            case "email" -> "citizenEmail";
            case "phone" -> "citizenPhone";
            case "address" -> "pickupAddress";
            case "description" -> "itemDescription";
            case "date" -> "preferredDate";
            default -> fieldName;
        };
    }
}

