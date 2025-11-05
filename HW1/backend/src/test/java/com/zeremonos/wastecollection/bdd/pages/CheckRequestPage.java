package com.zeremonos.wastecollection.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CheckRequestPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = ".search-group input")
    private WebElement tokenInput;

    @FindBy(css = ".search-group button")
    private WebElement searchButton;

    @FindBy(css = ".request-details")
    private WebElement requestDetails;

    @FindBy(css = ".alert-error")
    private WebElement errorMessage;

    @FindBy(css = ".status-badge")
    private WebElement statusBadge;

    @FindBy(css = ".btn-danger")
    private WebElement cancelButton;

    @FindBy(css = ".cancel-confirm .btn-danger")
    private WebElement confirmCancelButton;

    @FindBy(css = ".timeline")
    private WebElement statusTimeline;

    public CheckRequestPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void navigateTo() {
        driver.get("http://localhost:5173/check");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".search-group input")));
    }

    public void enterToken(String token) {
        wait.until(ExpectedConditions.elementToBeClickable(tokenInput));
        tokenInput.clear();
        tokenInput.sendKeys(token);
    }

    public void clickSearch() {
        searchButton.click();
    }

    public boolean isRequestDetailsDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(requestDetails));
            return requestDetails.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getStatus() {
        wait.until(ExpectedConditions.visibilityOf(statusBadge));
        return statusBadge.getText();
    }

    public boolean isStatusTimelineDisplayed() {
        try {
            return statusTimeline.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCancelButtonVisible() {
        try {
            return cancelButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickCancelButton() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton));
        cancelButton.click();
    }

    public void confirmCancellation() {
        wait.until(ExpectedConditions.elementToBeClickable(confirmCancelButton));
        confirmCancelButton.click();
        // Wait for the status to update
        wait.until(ExpectedConditions.textToBePresentInElement(statusBadge, "Cancelled"));
    }

    public String getRequestDetail(String fieldName) {
        try {
            WebElement detail = driver.findElement(By.xpath(
                String.format("//strong[contains(text(), '%s')]/following-sibling::text()", fieldName)
            ));
            return detail.getText();
        } catch (Exception e) {
            return "";
        }
    }
}

