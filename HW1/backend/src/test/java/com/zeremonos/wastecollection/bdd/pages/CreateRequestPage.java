package com.zeremonos.wastecollection.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CreateRequestPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "municipality")
    private WebElement municipalityDropdown;

    @FindBy(id = "citizenName")
    private WebElement citizenNameInput;

    @FindBy(id = "citizenEmail")
    private WebElement citizenEmailInput;

    @FindBy(id = "citizenPhone")
    private WebElement citizenPhoneInput;

    @FindBy(id = "pickupAddress")
    private WebElement pickupAddressInput;

    @FindBy(id = "itemDescription")
    private WebElement itemDescriptionInput;

    @FindBy(id = "preferredDate")
    private WebElement preferredDateInput;

    @FindBy(id = "preferredTimeSlot")
    private WebElement preferredTimeSlotSelect;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    @FindBy(css = ".alert-success")
    private WebElement successMessage;

    @FindBy(css = ".alert-error")
    private WebElement errorMessage;

    @FindBy(css = ".token-display code")
    private WebElement tokenDisplay;

    @FindBy(css = ".alert-success .token-display")
    private WebElement tokenContainer;

    public CreateRequestPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void navigateTo() {
        driver.get("http://localhost:5173/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("municipality")));
    }

    public void selectMunicipality(String municipality) {
        wait.until(ExpectedConditions.elementToBeClickable(municipalityDropdown));
        Select select = new Select(municipalityDropdown);
        select.selectByVisibleText(municipality);
    }

    public void fillCitizenName(String name) {
        citizenNameInput.clear();
        citizenNameInput.sendKeys(name);
    }

    public void fillCitizenEmail(String email) {
        citizenEmailInput.clear();
        citizenEmailInput.sendKeys(email);
    }

    public void fillCitizenPhone(String phone) {
        citizenPhoneInput.clear();
        citizenPhoneInput.sendKeys(phone);
    }

    public void fillPickupAddress(String address) {
        pickupAddressInput.clear();
        pickupAddressInput.sendKeys(address);
    }

    public void fillItemDescription(String description) {
        itemDescriptionInput.clear();
        itemDescriptionInput.sendKeys(description);
    }

    public void selectPreferredDate(LocalDate date) {
        String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        preferredDateInput.clear();
        preferredDateInput.sendKeys(dateString);
    }

    public void selectTimeSlot(String timeSlot) {
        Select select = new Select(preferredTimeSlotSelect);
        select.selectByValue(timeSlot);
    }

    public void submitForm() {
        submitButton.click();
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getToken() {
        wait.until(ExpectedConditions.visibilityOf(tokenDisplay));
        return tokenDisplay.getText();
    }

    public boolean isTokenDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(tokenContainer));
            return tokenContainer.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getMunicipalityOptions() {
        Select select = new Select(municipalityDropdown);
        return select.getOptions().stream()
                .map(WebElement::getText)
                .filter(text -> !text.isEmpty() && !text.equals("Select a municipality"))
                .toList();
    }

    public boolean hasValidationError(String fieldName) {
        try {
            WebElement field = driver.findElement(By.id(fieldName));
            String validationMessage = field.getAttribute("validationMessage");
            return validationMessage != null && !validationMessage.isEmpty();
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

    public void fillCompleteForm(String municipality, String name, String email, 
                                  String phone, String address, String description, 
                                  LocalDate date, String timeSlot) {
        selectMunicipality(municipality);
        fillCitizenName(name);
        if (email != null && !email.isEmpty()) {
            fillCitizenEmail(email);
        }
        fillCitizenPhone(phone);
        fillPickupAddress(address);
        fillItemDescription(description);
        selectPreferredDate(date);
        selectTimeSlot(timeSlot);
    }
}

