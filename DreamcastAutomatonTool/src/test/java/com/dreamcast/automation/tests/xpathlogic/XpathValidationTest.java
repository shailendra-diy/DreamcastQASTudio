package com.dreamcast.automation.tests.xpathlogic;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class XpathValidationTest {

    private WebDriver driver;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test(description = "Validate page title via XPath")
    public void validatePageTitle() {
        driver.get("https://dreamcast.co");
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(driver.getTitle(), "Page title should not be null");
        softAssert.assertTrue(driver.getTitle().length() > 0, "Page title should not be empty");
        softAssert.assertAll();
    }

    @Test(description = "Verify element presence on Dreamcast login page")
    public void verifyLoginPageElement() {
        driver.get("https://dreamcast.co/login");
        SoftAssert softAssert = new SoftAssert();
        try {
            WebElement el = driver.findElement(By.xpath("//input[@type='email']"));
            softAssert.assertTrue(el.isDisplayed(), "Email input should be visible");
        } catch (Exception e) {
            softAssert.fail("Email input not found: " + e.getMessage());
        }
        softAssert.assertAll();
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
