package com.selenium.studio;

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
    private final BiConsumer<String,String> logFn;

    public StepRunner(WebDriver driver, List<String> runLog, BiConsumer<String,String> logFn) {
        this.driver = driver;
        this.runLog = runLog;
        this.logFn  = logFn;
    }

    public boolean run(TestStep step, int idx, String ssDir, boolean autoSS) {
        String action = step.getAction();
        String value  = step.getValue();
        String value2 = step.getValue2();
        String name   = step.getName().isEmpty() ? "Step " + (idx+1) : step.getName();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            switch (action) {

                case "click":
                    findElement(step, wait).click();
                    break;

                case "type":
                    findElement(step, wait).sendKeys(value);
                    break;

                case "clearField":
                    findElement(step, wait).clear();
                    break;

                case "dropdown": {
                    Select sel = new Select(findElement(step, wait));
                    switch (value) {
                        case "selectByIndex": sel.selectByIndex(Integer.parseInt(value2.trim())); break;
                        case "selectByValue": sel.selectByValue(value2); break;
                        default:              sel.selectByVisibleText(value2);
                    }
                    break;
                }

                case "getText": {
                    String text = findElement(step, wait).getText();
                    log("INFO", "   [" + name + "] Text: " + text);
                    break;
                }

                case "getAttribute": {
                    String attr = findElement(step, wait).getAttribute(value);
                    log("INFO", "   [" + name + "] " + value + ": " + attr);
                    break;
                }

                case "verifyText": {
                    String actual = findElement(step, wait).getText();
                    if (!actual.equals(value))
                        throw new AssertionError("Expected: '" + value + "'  Got: '" + actual + "'");
                    log("INFO", "   [" + name + "] Verified: " + actual);
                    break;
                }

                case "verifyElement":
                    if (!findElement(step, wait).isDisplayed())
                        throw new AssertionError("Element not visible");
                    break;

                case "navigate":
                    driver.get(value);
                    break;

                case "screenshot":
                    takeScreenshot(value.isEmpty() ? "step_" + (idx+1) : value, ssDir);
                    break;

                case "scrollTo":
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});",
                        findElement(step, wait));
                    break;

                case "hover":
                    new Actions(driver).moveToElement(findElement(step, wait)).perform();
                    break;

                case "jsClick":
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].click();", findElement(step, wait));
                    break;

                case "acceptAlert": {
                    Alert alert = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.alertIsPresent());
                    if ("dismiss".equals(value)) alert.dismiss(); else alert.accept();
                    break;
                }

                case "switchFrame": {
                    try { driver.switchTo().frame(Integer.parseInt(value)); }
                    catch (NumberFormatException e) {
                        driver.switchTo().frame(
                            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(value))));
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
                    log("INFO", "   [" + name + "] " + value);
                    break;

                case "wait":
                    Thread.sleep(Long.parseLong(value.isEmpty() ? "1000" : value));
                    break;

                default:
                    log("INFO", "   Unknown action: " + action);
            }
            return true;

        } catch (Exception e) {
            log("FAIL", "   [" + name + "] " + e.getMessage());
            if (autoSS) takeScreenshot("FAIL_step" + (idx+1), ssDir);
            return false;
        }
    }

    /**
     * Multiple XPath fallback — pehla try karo, fail ho to doosra, phir teesra
     */
    private WebElement findElement(TestStep step, WebDriverWait wait) {
        List<String> xpaths = step.getXpaths();

        // Filter empty xpaths
        List<String> validXpaths = xpaths.stream()
            .filter(x -> x != null && !x.trim().isEmpty())
            .toList();

        if (validXpaths.isEmpty()) {
            throw new RuntimeException("Koi XPath nahi diya gaya");
        }

        Exception lastEx = null;
        for (int i = 0; i < validXpaths.size(); i++) {
            String xpath = validXpaths.get(i).trim();
            try {
                WebElement el = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                if (i > 0) log("INFO", "   XPath #" + (i+1) + " kaam aaya: " + xpath);
                return el;
            } catch (Exception e) {
                lastEx = e;
                if (validXpaths.size() > 1) {
                    log("WARN", "   XPath #" + (i+1) + " fail, next try: " + xpath);
                }
            }
        }
        throw new RuntimeException("Saare XPath fail: " + lastEx.getMessage());
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

    private void log(String type, String msg) { logFn.accept(type, msg); }
}