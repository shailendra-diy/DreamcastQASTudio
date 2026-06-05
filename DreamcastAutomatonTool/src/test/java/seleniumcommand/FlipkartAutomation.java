package seleniumcommand;

import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FlipkartAutomation {

    public static void main(String[] args) throws InterruptedException {

        // Launch Chrome Browser
        WebDriver driver = new ChromeDriver();

        // Maximize Window
        driver.manage().window().maximize();

        // Implicit Wait
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Explicit Wait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Open Flipkart
        driver.get("https://www.flipkart.com/");

        // Close Login Popup if displayed
        try {
            WebElement closeBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(text(),'✕')]")));

            closeBtn.click();

        } catch (Exception e) {

            System.out.println("Login popup not displayed");
        }

        // Search Product
        WebElement searchBox = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.name("q")));

        searchBox.sendKeys("iPhone");
        searchBox.sendKeys(Keys.ENTER);

        // Dynamic XPath for Product Selection
        WebElement product = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//div[contains(text(),'Apple iPhone')])[1]")));

        // Capture Parent Window
        String parentWindow = driver.getWindowHandle();

        // Click Product
        product.click();

        // Get All Windows
        Set<String> allWindows = driver.getWindowHandles();

        // Switch to Child Window
        for (String window : allWindows) {

            if (!window.equals(parentWindow)) {

                driver.switchTo().window(window);
                break;
            }
        }

        // Validate Product Title
        String productTitle = driver.getTitle();

        System.out.println("Product Title : " + productTitle);

        if (productTitle.contains("iPhone")) {

            System.out.println("Product Title Validated Successfully");

        } else {

            System.out.println("Product Title Validation Failed");
        }

        // Add Product To Cart
        WebElement addToCartBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Add to cart')]")));

        addToCartBtn.click();

        System.out.println("Product Added To Cart Successfully");

        Thread.sleep(5000);

        // Close Browser
        driver.quit();
    }
}