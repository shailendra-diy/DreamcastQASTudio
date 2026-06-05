package com.dreamcast.automation.base;

import com.dreamcast.automation.config.ConfigReader;
import com.dreamcast.automation.report.ExtentReportManager;
import com.dreamcast.automation.util.LoggerUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    protected final WebDriver     driver;
    protected final WebDriverWait wait;
    protected final Actions       actions;

    protected BasePage(WebDriver driver) {
        this.driver  = driver;
        this.wait    = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.explicitWait()));
        this.actions = new Actions(driver);
    }

    // ── Core interactions ────────────────────────────────────────────────

    protected WebElement find(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement findClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        findClickable(locator).click();
        log("Clicked: " + locator);
    }

    protected void type(By locator, String text) {
        WebElement el = find(locator);
        el.clear();
        el.sendKeys(text);
        log("Typed '" + text + "' into: " + locator);
    }

    protected void selectByText(By locator, String text) {
        new Select(find(locator)).selectByVisibleText(text);
        log("Selected '" + text + "' from: " + locator);
    }

    protected void selectByValue(By locator, String value) {
        new Select(find(locator)).selectByValue(value);
    }

    protected void hover(By locator) {
        actions.moveToElement(find(locator)).perform();
    }

    protected void jsClick(By locator) {
        WebElement el = find(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        log("JS Clicked: " + locator);
    }

    protected void scrollTo(By locator) {
        WebElement el = find(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    // ── Getters ──────────────────────────────────────────────────────────

    protected String getText(By locator) {
        return find(locator).getText();
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ── Waits ────────────────────────────────────────────────────────────

    protected boolean isDisplayed(By locator) {
        try {
            return find(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void waitForUrl(String urlFragment) {
        wait.until(ExpectedConditions.urlContains(urlFragment));
    }

    protected void waitForTitle(String title) {
        wait.until(ExpectedConditions.titleContains(title));
    }

    // ── Alerts ───────────────────────────────────────────────────────────

    protected void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).accept();
    }

    protected void dismissAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
    }

    // ── Navigation ───────────────────────────────────────────────────────

    protected void navigateTo(String url) {
        driver.get(url);
        log("Navigated to: " + url);
    }

    protected void goBack()    { driver.navigate().back(); }
    protected void refresh()   { driver.navigate().refresh(); }

    // ── Logging helper ───────────────────────────────────────────────────

    private void log(String message) {
        LoggerUtil.info(message);
        try {
            if (ExtentReportManager.getTest() != null)
                ExtentReportManager.getTest().info(message);
        } catch (Exception ignored) {}
    }
}
