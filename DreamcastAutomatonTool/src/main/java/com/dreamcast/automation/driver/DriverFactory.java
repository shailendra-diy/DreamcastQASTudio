package com.dreamcast.automation.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class DriverFactory {

    public enum Browser { CHROME, FIREFOX, EDGE }

    public static WebDriver create(Browser browser) {
        switch (browser) {
            case FIREFOX:
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();
            case EDGE:
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();
            case CHROME:
            default:
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver();
        }
    }

    public static WebDriver create(String browserName) {
        switch (browserName.toLowerCase()) {
            case "firefox": return create(Browser.FIREFOX);
            case "edge":    return create(Browser.EDGE);
            default:        return create(Browser.CHROME);
        }
    }
}
