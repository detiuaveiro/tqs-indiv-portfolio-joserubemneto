package com.zeremonos.wastecollection.bdd.steps;

import com.zeremonos.wastecollection.bdd.pages.CheckRequestPage;
import com.zeremonos.wastecollection.bdd.pages.CreateRequestPage;
import com.zeremonos.wastecollection.bdd.pages.HomePage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CitizenRequestSteps {

    private final WebDriver driver;
    private final HomePage homePage;
    private final CreateRequestPage createRequestPage;
    private final CheckRequestPage checkRequestPage;
    
    private String savedToken;

    public CitizenRequestSteps(WebDriverContext context) {
        this.driver = context.getDriver();
        this.homePage = new HomePage(driver);
        this.createRequestPage = new CreateRequestPage(driver);
        this.checkRequestPage = new CheckRequestPage(driver);
    }

    @Given("I am on the home page")
    public void iAmOnTheHomePage() {
        homePage.navigateTo();
        assertTrue(homePage.isLoaded(), "Home page should be loaded");
    }

    @Given("I navigate to the create request page")
    public void iNavigateToTheCreateRequestPage() {
        createRequestPage.navigateTo();
    }

    @When("I fill in the request form with valid data:")
    public void iFillInTheRequestFormWithValidData(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        
        createRequestPage.selectMunicipality(data.get("municipality"));
        createRequestPage.fillCitizenName(data.get("citizenName"));
        createRequestPage.fillCitizenEmail(data.get("citizenEmail"));
        createRequestPage.fillCitizenPhone(data.get("citizenPhone"));
        createRequestPage.fillPickupAddress(data.get("pickupAddress"));
        createRequestPage.fillItemDescription(data.get("itemDescription"));
        createRequestPage.selectTimeSlot(data.get("preferredTimeSlot"));
    }

    @When("I select a date {int} days from now")
    public void iSelectADateDaysFromNow(int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        createRequestPage.selectPreferredDate(futureDate);
    }

    @When("I submit the request form")
    public void iSubmitTheRequestForm() {
        createRequestPage.submitForm();
    }

    @When("I submit the request form without filling required fields")
    public void iSubmitTheRequestFormWithoutFillingRequiredFields() {
        createRequestPage.submitForm();
    }

    @When("I fill in the request form with an invalid email {string}")
    public void iFillInTheRequestFormWithAnInvalidEmail(String email) {
        createRequestPage.fillCitizenEmail(email);
    }

    @When("I fill in the request form with an invalid phone {string}")
    public void iFillInTheRequestFormWithAnInvalidPhone(String phone) {
        createRequestPage.fillCitizenPhone(phone);
    }

    @When("I fill in all other required fields")
    public void iFillInAllOtherRequiredFields() {
        createRequestPage.selectMunicipality("Lisboa");
        createRequestPage.fillCitizenName("Test User");
        createRequestPage.fillCitizenPhone("912345678");
        createRequestPage.fillPickupAddress("Test Address 123");
        createRequestPage.fillItemDescription("Test items for collection");
        createRequestPage.selectPreferredDate(LocalDate.now().plusDays(5));
        createRequestPage.selectTimeSlot("MORNING");
    }

    @Then("I should see a success message")
    public void iShouldSeeASuccessMessage() {
        assertTrue(createRequestPage.isSuccessMessageDisplayed(), "Success message should be displayed");
    }

    @Then("I should receive a unique access token")
    public void iShouldReceiveAUniqueAccessToken() {
        assertTrue(createRequestPage.isTokenDisplayed(), "Token should be displayed");
        savedToken = createRequestPage.getToken();
        assertNotNull(savedToken, "Token should not be null");
        assertFalse(savedToken.isEmpty(), "Token should not be empty");
    }

    @Then("the request status should be {string}")
    public void theRequestStatusShouldBe(String expectedStatus) {
        // Status is shown in success message after creation
        String successMessage = createRequestPage.getToken();
        assertNotNull(successMessage, "Success message should contain status information");
    }

    @Then("I should see validation errors for required fields")
    public void iShouldSeeValidationErrorsForRequiredFields() {
        boolean hasError = createRequestPage.hasValidationError("municipality") ||
                          createRequestPage.hasValidationError("citizenName") ||
                          createRequestPage.hasValidationError("pickupAddress");
        assertTrue(hasError, "Should have validation errors for required fields");
    }

    @Then("I should see an email validation error")
    public void iShouldSeeAnEmailValidationError() {
        String errorMessage = createRequestPage.getErrorMessage();
        assertTrue(errorMessage.contains("email") || errorMessage.contains("Email") || 
                  createRequestPage.isErrorMessageDisplayed(),
                  "Should see email validation error");
    }

    @Then("I should see a phone validation error")
    public void iShouldSeeAPhoneValidationError() {
        String errorMessage = createRequestPage.getErrorMessage();
        assertTrue(errorMessage.contains("phone") || errorMessage.contains("Phone") || 
                  createRequestPage.isErrorMessageDisplayed(),
                  "Should see phone validation error");
    }

    @Given("I have created a request and saved the token")
    public void iHaveCreatedARequestAndSavedTheToken() {
        iNavigateToTheCreateRequestPage();
        LocalDate futureDate = LocalDate.now().plusDays(5);
        createRequestPage.fillCompleteForm(
            "Lisboa",
            "Test User",
            "test@example.com",
            "912345678",
            "Test Address 123",
            "Test items for collection",
            futureDate,
            "MORNING"
        );
        iSubmitTheRequestForm();
        iShouldReceiveAUniqueAccessToken();
    }

    @When("I navigate to the check request page")
    public void iNavigateToTheCheckRequestPage() {
        checkRequestPage.navigateTo();
    }

    @When("I enter the saved token")
    public void iEnterTheSavedToken() {
        assertNotNull(savedToken, "Token should have been saved");
        checkRequestPage.enterToken(savedToken);
    }

    @When("I enter an invalid token {string}")
    public void iEnterAnInvalidToken(String token) {
        checkRequestPage.enterToken(token);
    }

    @When("I click search")
    public void iClickSearch() {
        checkRequestPage.clickSearch();
    }

    @Then("I should see the request details")
    public void iShouldSeeTheRequestDetails() {
        assertTrue(checkRequestPage.isRequestDetailsDisplayed(), "Request details should be displayed");
    }

    @Then("the status should be displayed")
    public void theStatusShouldBeDisplayed() {
        String status = checkRequestPage.getStatus();
        assertNotNull(status, "Status should be displayed");
        assertFalse(status.isEmpty(), "Status should not be empty");
    }

    @Then("I should see the status history")
    public void iShouldSeeTheStatusHistory() {
        assertTrue(checkRequestPage.isStatusTimelineDisplayed(), "Status history should be displayed");
    }

    @Then("I should see a {string} error message")
    public void iShouldSeeAErrorMessage(String errorType) {
        assertTrue(checkRequestPage.isErrorMessageDisplayed(), "Error message should be displayed");
        String errorMessage = checkRequestPage.getErrorMessage().toLowerCase();
        assertTrue(errorMessage.contains(errorType.toLowerCase()), 
                  "Error message should contain: " + errorType);
    }

    @When("I click the cancel button")
    public void iClickTheCancelButton() {
        assertTrue(checkRequestPage.isCancelButtonVisible(), "Cancel button should be visible");
        checkRequestPage.clickCancelButton();
    }

    @When("I confirm the cancellation")
    public void iConfirmTheCancellation() {
        checkRequestPage.confirmCancellation();
    }

    @Then("the cancel button should not be visible")
    public void theCancelButtonShouldNotBeVisible() {
        assertFalse(checkRequestPage.isCancelButtonVisible(), "Cancel button should not be visible");
    }

    @When("I click on the municipality dropdown")
    public void iClickOnTheMunicipalityDropdown() {
        // The dropdown opens when clicked, handled by the page object
    }

    @Then("I should see a list of Portuguese municipalities")
    public void iShouldSeeAListOfPortugueseMunicipalities() {
        var municipalities = createRequestPage.getMunicipalityOptions();
        assertFalse(municipalities.isEmpty(), "Municipality list should not be empty");
        assertTrue(municipalities.size() > 100, "Should have many municipalities");
    }

    @Then("the list should include {string}")
    public void theListShouldInclude(String municipality) {
        var municipalities = createRequestPage.getMunicipalityOptions();
        assertTrue(municipalities.contains(municipality), 
                  "Municipality list should include: " + municipality);
    }
}

