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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StaffSteps {

    @Autowired
    private WebDriver driver;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private WebDriverWait wait;

    @Given("I am on the staff dashboard")
    public void i_am_on_the_staff_dashboard() {
        driver.get(frontendUrl + "/staff");
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".staff-dashboard")));
    }

    @Given("there are {int} service requests in the system")
    public void there_are_service_requests_in_the_system(Integer count) {
        // This would typically involve API calls or database setup
        // For BDD, we assume the state is prepared by Background or API calls
        // count is used for verification purposes
    }

    @When("I view the dashboard statistics")
    public void i_view_the_dashboard_statistics() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".stats-card")));
    }

    @When("I filter requests by municipality {string}")
    public void i_filter_requests_by_municipality(String municipality) {
        WebElement municipalityFilter = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("municipalityFilter")));
        Select select = new Select(municipalityFilter);
        select.selectByVisibleText(municipality);
        
        // Wait for filter to apply
        try {
            Thread.sleep(1000); // Wait for debounce/filter application
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @When("I filter requests by status {string}")
    public void i_filter_requests_by_status(String status) {
        WebElement statusFilter = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("statusFilter")));
        Select select = new Select(statusFilter);
        select.selectByValue(status);
        
        // Wait for filter to apply
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @When("I click on the first request card")
    public void i_click_on_the_first_request_card() {
        WebElement firstCard = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".request-card")));
        firstCard.click();
    }

    @When("I click the {string} button on the first request")
    public void i_click_the_button_on_the_first_request(String buttonText) {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), '" + buttonText + "')]")));
        button.click();
    }

    @When("I select new status {string}")
    public void i_select_new_status(String status) {
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".modal")));
        
        WebElement statusSelect = modal.findElement(By.id("newStatus"));
        Select select = new Select(statusSelect);
        select.selectByValue(status);
    }

    @When("I enter notes {string}")
    public void i_enter_notes(String notes) {
        WebElement notesField = driver.findElement(By.id("notes"));
        notesField.clear();
        notesField.sendKeys(notes);
    }

    @When("I click the update button in the modal")
    public void i_click_the_update_button_in_the_modal() {
        WebElement updateButton = driver.findElement(
            By.xpath("//div[@class='modal']//button[contains(text(), 'Update')]"));
        updateButton.click();
    }

    @When("I refresh the page")
    public void i_refresh_the_page() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".staff-dashboard")));
    }

    @Then("I should see the staff dashboard")
    public void i_should_see_the_staff_dashboard() {
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".staff-dashboard")));
        assertTrue(dashboard.isDisplayed());
    }

    @Then("I should see statistics cards")
    public void i_should_see_statistics_cards() {
        List<WebElement> statsCards = driver.findElements(By.cssSelector(".stats-card"));
        assertTrue(statsCards.size() >= 3, "Should have at least 3 statistics cards");
    }

    @Then("I should see a list of service requests")
    public void i_should_see_a_list_of_service_requests() {
        List<WebElement> requestCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".request-card")));
        assertFalse(requestCards.isEmpty(), "Should display at least one request card");
    }

    @Then("I should see at least {int} request cards")
    public void i_should_see_at_least_request_cards(Integer count) {
        List<WebElement> requestCards = driver.findElements(By.cssSelector(".request-card"));
        assertTrue(requestCards.size() >= count, 
            String.format("Expected at least %d cards, found %d", count, requestCards.size()));
    }

    @Then("I should see only requests from {string}")
    public void i_should_see_only_requests_from(String municipality) {
        List<WebElement> requestCards = driver.findElements(By.cssSelector(".request-card"));
        for (WebElement card : requestCards) {
            String cardText = card.getText();
            assertTrue(cardText.contains(municipality), 
                "Request card should contain municipality: " + municipality);
        }
    }

    @Then("I should see only requests with status {string}")
    public void i_should_see_only_requests_with_status(String status) {
        List<WebElement> requestCards = driver.findElements(By.cssSelector(".request-card"));
        for (WebElement card : requestCards) {
            WebElement statusBadge = card.findElement(By.cssSelector(".status-badge"));
            String badgeText = statusBadge.getText().toUpperCase();
            assertTrue(badgeText.contains(status.toUpperCase()) || 
                      badgeText.contains(status.replace("_", " ").toUpperCase()),
                "Status badge should show: " + status);
        }
    }

    @Then("I should see the update status modal")
    public void i_should_see_the_update_status_modal() {
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".modal")));
        assertTrue(modal.isDisplayed());
        assertTrue(modal.getText().contains("Update Status"));
    }

    @Then("the modal should show current status")
    public void the_modal_should_show_current_status() {
        WebElement currentStatusElement = driver.findElement(
            By.xpath("//*[contains(text(), 'Current Status')]"));
        assertTrue(currentStatusElement.isDisplayed());
    }

    @Then("the modal should have a status dropdown")
    public void the_modal_should_have_a_status_dropdown() {
        WebElement statusSelect = driver.findElement(By.id("newStatus"));
        assertTrue(statusSelect.isDisplayed());
        assertEquals("select", statusSelect.getTagName().toLowerCase());
    }

    @Then("the modal should have a notes field")
    public void the_modal_should_have_a_notes_field() {
        WebElement notesField = driver.findElement(By.id("notes"));
        assertTrue(notesField.isDisplayed());
    }

    @Then("the modal should close")
    public void the_modal_should_close() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".modal")));
    }

    @Then("I should see a success notification")
    public void i_should_see_a_success_notification() {
        WebElement notification = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-success, .notification-success")));
        assertTrue(notification.isDisplayed());
    }

    @Then("the request status should be updated to {string}")
    public void the_request_status_should_be_updated_to(String expectedStatus) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".request-card")));
        
        WebElement firstCard = driver.findElement(By.cssSelector(".request-card"));
        WebElement statusBadge = firstCard.findElement(By.cssSelector(".status-badge"));
        String actualStatus = statusBadge.getText().toUpperCase();
        
        assertTrue(actualStatus.contains(expectedStatus.toUpperCase()) || 
                   actualStatus.contains(expectedStatus.replace("_", " ").toUpperCase()),
            String.format("Expected status '%s' but found '%s'", expectedStatus, actualStatus));
    }

    @Then("the total requests count should be {int}")
    public void the_total_requests_count_should_be(Integer expectedCount) {
        WebElement totalCard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'stats-card')]//h3[contains(text(), 'Total')]/..")));
        String countText = totalCard.getText().replaceAll("[^0-9]", "");
        int actualCount = Integer.parseInt(countText);
        assertEquals(expectedCount.intValue(), actualCount, 
            "Total requests count should match");
    }
}

