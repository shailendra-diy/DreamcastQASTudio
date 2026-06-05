package com.dreamcast.automation.tests.flipkart;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

public class FlipkartTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("https://www.flipkart.com");
    }

    @Test(priority = 1, description = "Close login popup if visible")
    public void closeLoginPopup() {
        try {
            WebElement closeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class,'_2KpZ6l')]")));
            closeBtn.click();
        } catch (Exception e) {
            System.out.println("No login popup found.");
        }
    }

    @Test(priority = 2, description = "Search for iPhone and verify results")
    public void searchIPhone() {
        WebElement searchBox = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='q']")));
        searchBox.sendKeys("iPhone");
        searchBox.submit();

        WebElement firstResult = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[@class='KzDlHZ'])[1]")));
        Assert.assertTrue(firstResult.isDisplayed(), "Search results should be visible");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
