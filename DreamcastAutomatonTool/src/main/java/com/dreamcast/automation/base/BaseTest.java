package com.dreamcast.automation.base;

import com.dreamcast.automation.config.ConfigReader;
import com.dreamcast.automation.driver.DriverFactory;
import com.dreamcast.automation.listeners.RetryAnalyzer;
import com.dreamcast.automation.util.LoggerUtil;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import java.time.Duration;

@Listeners(com.dreamcast.automation.listeners.TestListener.class)
public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeClass
    public void setUp() {
        LoggerUtil.info("Launching browser: " + ConfigReader.browser());
        driver = DriverFactory.create(ConfigReader.browser());
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.implicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.pageLoadTimeout()));
        driver.get(ConfigReader.baseUrl());
        LoggerUtil.info("Opened URL: " + ConfigReader.baseUrl());
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            LoggerUtil.info("Browser closed.");
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    protected String getRetryAnalyzer() {
        return RetryAnalyzer.class.getName();
    }
}
