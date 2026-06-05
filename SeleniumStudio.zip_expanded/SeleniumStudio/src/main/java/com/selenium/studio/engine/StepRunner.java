package com.selenium.studio.engine;

import com.selenium.studio.model.TestStep;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

public class StepRunner {

    private final WebDriver              driver;
    private final List<String>           runLog;
    private final BiConsumer<String, String> logFn;

    public StepRunner(WebDriver driver, List<String> runLog, BiConsumer<String, String> logFn) {
        this.driver = driver;
        this.runLog = runLog;
        this.logFn  = logFn;
    }

    public boolean run(TestStep step, int idx, String ssDir, boolean autoSS) {
        String action = step.getAction();
        String xpath  = step.getXpath();
        String value  = step.getValue();
        String value2 = step.getValue2();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            switch (action) {

                case "click":
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
                    break;

                case "type":
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).sendKeys(value);
                    break;

                case "clearField":
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).clear();
                    break;

                case "dropdown": {
                    WebElement el  = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
                    Select     sel = new Select(el);
                    switch (value) {
                        case "selectByIndex": sel.selectByIndex(Integer.parseInt(value2.trim())); break;
                        case "selectByValue": sel.selectByValue(value2); break;
                        default:              sel.selectByVisibleText(value2);
                    }
                    break;
                }

                case "getText": {
                    String text = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).getText();
                    log("INFO", "   Text: " + text);
                    break;
                }

                case "getAttribute": {
                    String attr = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).getAttribute(value);
                    log("INFO", "   [" + value + "]: " + attr);
                    break;
                }

                case "verifyText": {
                    String actual = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).getText();
                    if (!actual.equals(value))
                        throw new AssertionError("Expected: '" + value + "'  Got: '" + actual + "'");
                    log("INFO", "   Text verified: " + actual);
                    break;
                }

                case "verifyElement":
                    if (!wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).isDisplayed())
                        throw new AssertionError("Element not visible");
                    break;

                case "navigate":
                    driver.get(value);
                    break;

                case "screenshot":
                    takeScreenshot(value.isEmpty() ? "step_" + (idx + 1) : value, ssDir);
                    break;

                case "scrollTo": {
                    WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", el);
                    break;
                }

                case "hover": {
                    WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
                    new Actions(driver).moveToElement(el).perform();
                    break;
                }

                case "jsClick": {
                    WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                    break;
                }

                case "acceptAlert": {
                    Alert alert = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.alertIsPresent());
                    if ("dismiss".equals(value)) alert.dismiss(); else alert.accept();
                    break;
                }

                case "switchFrame": {
                    try {
                        driver.switchTo().frame(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        driver.switchTo().frame(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(value))));
                    }
                    break;
                }

                case "switchWindow": {
                    String main = driver.getWindowHandle();
                    for (String h : driver.getWindowHandles())
                        if (!h.equals(main)) { driver.switchTo().window(h); break; }
                    break;
                }

                case "print":
                    log("INFO", "   " + value);
                    break;

                case "wait":
                    Thread.sleep(Long.parseLong(value.isEmpty() ? "1000" : value));
                    break;

                default:
                    log("INFO", "   Unknown action: " + action);
            }

            return true;

        } catch (Exception e) {
            log("FAIL", "   " + e.getMessage());
            if (autoSS) takeScreenshot("FAIL_step" + (idx + 1), ssDir);
            return false;
        }
    }

    private void takeScreenshot(String name, String dir) {
        try {
            new File(dir).mkdirs();
            String ts   = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            File   src  = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File   dest = new File(dir + "/" + name + "_" + ts + ".png");
            FileUtils.copyFile(src, dest);
            log("INFO", "   Screenshot: " + dest.getAbsolutePath());
        } catch (Exception e) {
            log("FAIL", "   Screenshot error: " + e.getMessage());
        }
    }

    private void log(String type, String msg) {
        logFn.accept(type, msg);
    }
}
