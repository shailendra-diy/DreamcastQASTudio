package com.selenium.studio;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Creates and configures WebDriver instances for Chrome, Firefox, and Edge.
 */
public class DriverFactory {

    /**
     * Creates a configured WebDriver for the given browser name.
     *
     * @param browser    "chrome" | "firefox" | "edge"
     * @param config     TestConfig with window/timeout settings
     * @return           ready-to-use WebDriver
     */
    public static WebDriver create(String browser, TestConfig config) {
        WebDriver driver;

        switch (browser.toLowerCase()) {

            case "firefox": {
                FirefoxOptions opts = new FirefoxOptions();
                if (config.isHeadless()) opts.addArguments("-headless");
                driver = new FirefoxDriver(opts);
                break;
            }

            case "edge": {
                EdgeOptions opts = new EdgeOptions();
                if (config.isHeadless()) opts.addArguments("--headless", "--no-sandbox");
                driver = new EdgeDriver(opts);
                break;
            }

            default: { // chrome
                ChromeOptions opts = new ChromeOptions();
                if (config.isHeadless())
                    opts.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                driver = new ChromeDriver(opts);
                break;
            }
        }

        // Timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getPageLoad()));

        // Window size
        if (config.isMaximize()) {
            driver.manage().window().maximize();
        } else {
            driver.manage().window().setSize(new Dimension(config.getResW(), config.getResH()));
        }

        return driver;
    }
}
