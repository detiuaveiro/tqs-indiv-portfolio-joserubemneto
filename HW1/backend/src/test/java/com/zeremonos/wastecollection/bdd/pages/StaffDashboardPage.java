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
import java.util.List;

public class StaffDashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "#municipalityFilter")
    private WebElement municipalityFilter;

    @FindBy(css = "#statusFilter")
    private WebElement statusFilter;

    @FindBy(css = ".btn-refresh")
    private WebElement refreshButton;

    @FindBy(css = ".stats-grid .stat-card")
    private List<WebElement> statCards;

    @FindBy(css = ".request-card")
    private List<WebElement> requestCards;

    @FindBy(css = ".btn-update")
    private WebElement updateStatusButton;

    @FindBy(css = ".modal-content")
    private WebElement modal;

    @FindBy(css = "input[name='newStatus']")
    private List<WebElement> statusOptions;

    @FindBy(css = "#notes")
    private WebElement notesTextarea;

    @FindBy(css = ".modal-actions .btn-primary")
    private WebElement confirmUpdateButton;

    @FindBy(css = ".empty-state")
    private WebElement emptyState;

    public StaffDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void navigateTo() {
        driver.get("http://localhost:5173/staff");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".stats-grid")));
    }

    public boolean isStatsDashboardDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfAllElements(statCards));
            return statCards.size() >= 5; // Should have at least 5 stat cards
        } catch (Exception e) {
            return false;
        }
    }

    public int getTotalRequestsCount() {
        return requestCards.size();
    }

    public String getStatValue(String statName) {
        try {
            WebElement stat = driver.findElement(By.xpath(
                String.format("//p[contains(text(), '%s')]/preceding-sibling::h3", statName)
            ));
            return stat.getText();
        } catch (Exception e) {
            return "0";
        }
    }

    public void selectMunicipalityFilter(String municipality) {
        Select select = new Select(municipalityFilter);
        select.selectByVisibleText(municipality);
        wait.until(ExpectedConditions.stalenessOf(requestCards.isEmpty() ? driver.findElement(By.cssSelector(".empty-state")) : requestCards.get(0)));
    }

    public void selectStatusFilter(String status) {
        Select select = new Select(statusFilter);
        select.selectByVisibleText(status);
        wait.until(ExpectedConditions.stalenessOf(requestCards.isEmpty() ? driver.findElement(By.cssSelector(".empty-state")) : requestCards.get(0)));
    }

    public void clickRefresh() {
        refreshButton.click();
        try {
            Thread.sleep(500); // Wait for refresh
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean areAllRequestsForMunicipality(String municipality) {
        for (WebElement card : requestCards) {
            String cardMunicipality = card.findElement(By.cssSelector(".info-content strong")).getText();
            if (!cardMunicipality.contains(municipality)) {
                return false;
            }
        }
        return true;
    }

    public boolean areAllRequestsWithStatus(String status) {
        for (WebElement card : requestCards) {
            String cardStatus = card.findElement(By.cssSelector(".status-badge")).getText();
            if (!cardStatus.contains(status)) {
                return false;
            }
        }
        return true;
    }

    public void clickUpdateStatusOnFirstRequest() {
        wait.until(ExpectedConditions.elementToBeClickable(requestCards.get(0).findElement(By.cssSelector(".btn-update"))));
        requestCards.get(0).findElement(By.cssSelector(".btn-update")).click();
        wait.until(ExpectedConditions.visibilityOf(modal));
    }

    public void clickUpdateStatusOnRequestWithStatus(String status) {
        for (WebElement card : requestCards) {
            String cardStatus = card.findElement(By.cssSelector(".status-badge")).getText();
            if (cardStatus.contains(status)) {
                card.findElement(By.cssSelector(".btn-update")).click();
                wait.until(ExpectedConditions.visibilityOf(modal));
                return;
            }
        }
    }

    public boolean isUpdateStatusButtonVisible(String status) {
        try {
            for (WebElement card : requestCards) {
                String cardStatus = card.findElement(By.cssSelector(".status-badge")).getText();
                if (cardStatus.contains(status)) {
                    return card.findElements(By.cssSelector(".btn-update")).size() > 0;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void selectNewStatus(String statusLabel) {
        for (WebElement option : statusOptions) {
            WebElement label = option.findElement(By.xpath("following-sibling::div//span[@class='status-label']"));
            if (label.getText().contains(statusLabel)) {
                option.click();
                return;
            }
        }
    }

    public List<String> getAvailableStatusOptions() {
        return statusOptions.stream()
                .map(option -> option.findElement(By.xpath("following-sibling::div//span[@class='status-label']")).getText())
                .toList();
    }

    public void enterNotes(String notes) {
        notesTextarea.clear();
        notesTextarea.sendKeys(notes);
    }

    public void confirmStatusUpdate() {
        confirmUpdateButton.click();
        wait.until(ExpectedConditions.invisibilityOf(modal));
    }

    public boolean isEmptyStateDisplayed() {
        try {
            return emptyState.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}

