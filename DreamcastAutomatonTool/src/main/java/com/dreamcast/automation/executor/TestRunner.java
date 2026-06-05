package com.dreamcast.automation.executor;

import com.dreamcast.automation.model.TestResult;
import com.dreamcast.automation.model.TestStep;
import com.dreamcast.automation.util.ScreenshotUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    private final WebDriver driver;
    private final String    screenshotDir;

    public TestRunner(WebDriver driver, String screenshotDir) {
        this.driver        = driver;
        this.screenshotDir = screenshotDir;
    }

    public List<TestResult> runAll(String startUrl, List<TestStep> steps) {
        List<TestResult> results = new ArrayList<>();
        driver.get(startUrl);
        Actions actions = new Actions(driver);

        for (TestStep step : steps) {
            results.add(runStep(step, actions));
        }
        return results;
    }

    private TestResult runStep(TestStep step, Actions actions) {
        try {
            if (step.getWaitSeconds() > 0) {
                Thread.sleep(step.getWaitSeconds() * 1000L);
            }

            WebElement el = null;
            if (step.getXpath() != null && !step.getXpath().isBlank()) {
                el = driver.findElement(By.xpath(step.getXpath()));
            }

            executeAction(step, el, actions);
            return new TestResult(step.getName(), TestResult.Status.PASS, "OK", null);

        } catch (Exception ex) {
            String ssPath = ScreenshotUtil.capture(driver, "FAIL_" + step.getName(), screenshotDir);
            return new TestResult(step.getName(), TestResult.Status.FAIL, ex.getMessage(), ssPath);
        }
    }

    private void executeAction(TestStep step, WebElement el, Actions actions) throws Exception {
        String action = step.getAction();
        String value  = step.getValue();

        switch (action) {
            case "click":           el.click(); break;
            case "sendKeys":        el.sendKeys(value); break;
            case "clear":           el.clear(); break;
            case "submit":          el.submit(); break;
            case "mouseHover":      actions.moveToElement(el).perform(); break;
            case "doubleClick":     actions.doubleClick(el).perform(); break;
            case "rightClick":      actions.contextClick(el).perform(); break;
            case "getText":         System.out.println("[TEXT] " + el.getText()); break;
            case "getAttribute":    System.out.println("[ATTR] " + el.getAttribute(value)); break;
            case "selectByText":    new Select(el).selectByVisibleText(value); break;
            case "selectByValue":   new Select(el).selectByValue(value); break;
            case "selectByIndex":   new Select(el).selectByIndex(Integer.parseInt(value)); break;
            case "isDisplayed":     System.out.println("[CHECK] isDisplayed=" + el.isDisplayed()); break;
            case "isEnabled":       System.out.println("[CHECK] isEnabled=" + el.isEnabled()); break;
            case "scrollIntoView":
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el); break;
            case "executeJS":
                ((JavascriptExecutor) driver).executeScript(value, el); break;
            case "alertAccept":     driver.switchTo().alert().accept(); break;
            case "alertDismiss":    driver.switchTo().alert().dismiss(); break;
            case "navigateBack":    driver.navigate().back(); break;
            case "navigateForward": driver.navigate().forward(); break;
            case "refresh":         driver.navigate().refresh(); break;
            case "takeScreenshot":  ScreenshotUtil.capture(driver, value.isEmpty() ? "step" : value, screenshotDir); break;
            case "verifyText":
                String actual = el.getText();
                if (!actual.equals(value))
                    throw new AssertionError("Expected: '" + value + "' Got: '" + actual + "'");
                break;
            case "clearAndType":    el.clear(); el.sendKeys(value); break;
            case "wait":            Thread.sleep(Long.parseLong(value.isEmpty() ? "1000" : value)); break;
            default:
                System.out.println("[WARN] Unknown action: " + action);
        }
    }
}
