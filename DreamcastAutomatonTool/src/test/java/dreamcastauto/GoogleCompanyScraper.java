package dreamcastauto;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GoogleCompanyScraper {

    public static void main(String[] args) throws Exception {

        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        driver.get("https://www.google.com/search?q=software+company+in+jaipur&udm=1");

        Thread.sleep(3000);

        String filePath = "Companies.xlsx";
        Workbook workbook;
        Sheet sheet;

        File file = new File(filePath);

        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
        } else {
            workbook = new XSSFWorkbook();
        }

        String sheetName = "Run_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss"));
        sheet = workbook.createSheet(sheetName);

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Company Name");

        int rowNum = 1;

        while (true) {

            // Get all company names
            List<WebElement> companies = driver.findElements(By.xpath("//div[@role='heading']"));

            System.out.println("Found: " + companies.size());

            for (WebElement company : companies) {
                String name = company.getText();

                if (!name.isEmpty()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(name);
                    System.out.println(name);
                }
            }

            try {
                // Next button click
                WebElement nextBtn = driver.findElement(By.xpath("//g-fab[@class='sr9hec OvQkSb s3IB3']"));

                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", nextBtn);

                System.out.println("Clicked NEXT...");

                Thread.sleep(4000);

            } catch (Exception e) {
                System.out.println("No more pages. Scraping finished.");
                break;
            }
        }

        sheet.autoSizeColumn(0);

        FileOutputStream fos = new FileOutputStream(filePath);
        workbook.write(fos);

        fos.close();
        workbook.close();

        driver.quit();

        System.out.println("Data saved successfully!");
    }
}