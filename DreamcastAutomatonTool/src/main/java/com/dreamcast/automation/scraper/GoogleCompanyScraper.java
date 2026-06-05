package com.dreamcast.automation.scraper;

import com.dreamcast.automation.util.ExcelUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.List;

public class GoogleCompanyScraper {

    private static final String SEARCH_URL =
        "https://www.google.com/search?q=software+company+in+jaipur&udm=1";
    private static final String OUTPUT_FILE = "Companies.xlsx";

    public static void main(String[] args) throws Exception {
        new GoogleCompanyScraper().run();
    }

    public void run() throws Exception {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(SEARCH_URL);
        Thread.sleep(3000);

        List<String[]> rows = scrapeAllPages(driver);
        driver.quit();

        ExcelUtil.writeToNewSheet(
            OUTPUT_FILE,
            ExcelUtil.timestampedSheetName(),
            new String[]{"Company Name"},
            rows
        );

        System.out.println("Scraped " + rows.size() + " companies. Saved to " + OUTPUT_FILE);
    }

    private List<String[]> scrapeAllPages(WebDriver driver) throws InterruptedException {
        List<String[]> rows = new ArrayList<>();

        while (true) {
            List<WebElement> companies = driver.findElements(By.xpath("//div[@role='heading']"));
            System.out.println("Found " + companies.size() + " on this page");

            for (WebElement company : companies) {
                String name = company.getText().trim();
                if (!name.isEmpty()) {
                    rows.add(new String[]{name});
                    System.out.println(name);
                }
            }

            try {
                WebElement nextBtn = driver.findElement(By.xpath("//g-fab[@class='sr9hec OvQkSb s3IB3']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextBtn);
                System.out.println("Next page...");
                Thread.sleep(4000);
            } catch (Exception e) {
                System.out.println("No more pages.");
                break;
            }
        }

        return rows;
    }
}
