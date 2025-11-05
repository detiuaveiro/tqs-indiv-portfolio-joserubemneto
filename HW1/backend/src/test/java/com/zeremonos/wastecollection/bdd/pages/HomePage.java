package com.zeremonos.wastecollection.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(linkText = "New Request")
    private WebElement newRequestLink;

    @FindBy(linkText = "Check Status")
    private WebElement checkStatusLink;

    @FindBy(linkText = "Staff Dashboard")
    private WebElement staffDashboardLink;

    @FindBy(css = ".hero-section")
    private WebElement heroSection;

    @FindBy(css = ".feature-card")
    private WebElement featureCards;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void navigateTo() {
        driver.get("http://localhost:5173");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".hero-section")));
    }

    public boolean isLoaded() {
        try {
            return heroSection.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickNewRequest() {
        newRequestLink.click();
    }

    public void clickCheckStatus() {
        checkStatusLink.click();
    }

    public void clickStaffDashboard() {
        staffDashboardLink.click();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}

