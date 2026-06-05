package com.dreamcast.automation.util;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {

    private static final String DEFAULT_DIR = "screenshots";

    public static String capture(WebDriver driver, String stepName) {
        return capture(driver, stepName, DEFAULT_DIR);
    }

    public static String capture(WebDriver driver, String stepName, String directory) {
        try {
            new File(directory).mkdirs();
            String ts   = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String safe = stepName.replaceAll("[^a-zA-Z0-9_-]", "_");
            File   src  = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File   dest = new File(directory + File.separator + safe + "_" + ts + ".png");
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return dest.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Screenshot failed: " + e.getMessage());
            return null;
        }
    }
}
