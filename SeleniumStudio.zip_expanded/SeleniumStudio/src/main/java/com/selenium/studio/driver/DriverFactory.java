package com.selenium.studio.driver;

import com.selenium.studio.model.TestConfig;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public class DriverFactory {

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

            default: {
                ChromeOptions opts = new ChromeOptions();
                if (config.isHeadless())
                    opts.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                driver = new ChromeDriver(opts);
                break;
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getPageLoad()));

        if (config.isMaximize()) {
            driver.manage().window().maximize();
        } else {
            driver.manage().window().setSize(new Dimension(config.getResW(), config.getResH()));
        }

        return driver;
    }
}
