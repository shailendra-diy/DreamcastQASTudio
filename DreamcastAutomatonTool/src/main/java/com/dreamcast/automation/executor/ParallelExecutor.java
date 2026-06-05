package com.dreamcast.automation.executor;

import com.dreamcast.automation.driver.DriverFactory;
import com.dreamcast.automation.model.TestResult;
import com.dreamcast.automation.model.TestStep;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class ParallelExecutor {

    public static List<TestResult> runOnBrowsers(
            List<String> browsers,
            String url,
            List<TestStep> steps,
            String screenshotDir,
            BiConsumer<String, List<TestResult>> onBrowserComplete) {

        ExecutorService pool = Executors.newFixedThreadPool(browsers.size());
        List<Future<List<TestResult>>> futures = new ArrayList<>();

        for (String browser : browsers) {
            futures.add(pool.submit(() -> {
                WebDriver driver = DriverFactory.create(browser);
                driver.manage().window().maximize();
                try {
                    TestRunner runner  = new TestRunner(driver, screenshotDir);
                    List<TestResult> results = runner.runAll(url, steps);
                    if (onBrowserComplete != null) onBrowserComplete.accept(browser, results);
                    return results;
                } finally {
                    try { driver.quit(); } catch (Exception ignored) {}
                }
            }));
        }

        pool.shutdown();

        List<TestResult> all = new ArrayList<>();
        for (Future<List<TestResult>> f : futures) {
            try {
                all.addAll(f.get());
            } catch (Exception e) {
                System.err.println("Browser thread error: " + e.getMessage());
            }
        }
        return all;
    }
}
