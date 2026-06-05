package com.selenium.studio.engine;

import com.selenium.studio.capture.ApiCaptureManager;
import com.selenium.studio.driver.DriverFactory;
import com.selenium.studio.model.TestConfig;
import com.selenium.studio.model.TestStep;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.HasDevTools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestEngine {

    public final List<String>         runLog     = new CopyOnWriteArrayList<>();
    public final List<String>         apiLog     = new CopyOnWriteArrayList<>();
    public final Map<Integer, String> stepStatus = new ConcurrentHashMap<>();

    public final AtomicBoolean isRunning   = new AtomicBoolean(false);
    public final AtomicBoolean captureApi  = new AtomicBoolean(false);
    public final AtomicInteger passCount   = new AtomicInteger(0);
    public final AtomicInteger failCount   = new AtomicInteger(0);
    public final AtomicInteger currentStep = new AtomicInteger(-1);

    private volatile WebDriver activeDriver = null;

    public void execute(TestConfig config) {
        isRunning.set(true);
        passCount.set(0);
        failCount.set(0);
        currentStep.set(-1);
        runLog.clear();
        stepStatus.clear();
        apiLog.clear();

        List<String>   browsers = config.getBrowsers();
        List<TestStep> steps    = config.getSteps();

        log("INFO", "Selenium Automation Studio v2.0 - Starting");
        log("INFO", "Steps: " + steps.size() + "  |  Browsers: " + browsers);
        log("INFO", "URL: " + config.getUrl());
        log("INFO", "API Capture: " + (captureApi.get() ? "ON" : "OFF"));

        for (String browser : browsers) {
            if (!isRunning.get()) break;
            log("INFO", "\n=== Browser: " + browser.toUpperCase() + " ===");

            try {
                activeDriver = DriverFactory.create(browser, config);

                if (captureApi.get()
                        && browser.equalsIgnoreCase("chrome")
                        && activeDriver instanceof HasDevTools) {

                    ApiCaptureManager mgr = new ApiCaptureManager(
                        apiLog,
                        () -> log("INFO", "CDP API Capture active")
                    );
                    boolean ok = mgr.setup((HasDevTools) activeDriver);
                    if (!ok) log("WARN", "CDP not available - API capture disabled");
                }

                activeDriver.get(config.getUrl());
                log("INFO", "Opened: " + config.getUrl());

                StepRunner runner = new StepRunner(activeDriver, runLog, this::log);

                for (int i = 0; i < steps.size(); i++) {
                    if (!isRunning.get()) break;
                    currentStep.set(i);
                    TestStep step = steps.get(i);

                    log("INFO", "Step " + (i + 1) + ": " + step.getAction()
                        + (step.getXpath().isEmpty() ? "" : " | " + step.getXpath()));

                    boolean passed = runner.run(step, i, config.getScreenshotDir(), config.isAutoScreenshot());

                    if (passed) {
                        stepStatus.put(i, "pass");
                        passCount.incrementAndGet();
                        log("PASS", "Step " + (i + 1) + " PASSED");
                    } else {
                        stepStatus.put(i, "fail");
                        failCount.incrementAndGet();
                        log("FAIL", "Step " + (i + 1) + " FAILED");
                    }

                    if (step.getWait() > 0) Thread.sleep(step.getWait());
                }

            } catch (Exception e) {
                log("FAIL", "Browser error: " + e.getMessage());
            } finally {
                if (activeDriver != null) {
                    try { activeDriver.quit(); } catch (Exception ignored) {}
                    activeDriver = null;
                }
            }
        }

        currentStep.set(-1);
        isRunning.set(false);
        log("INFO", "===========================");
        log(failCount.get() == 0 ? "PASS" : "FAIL",
            "DONE - PASS: " + passCount.get() + "  FAIL: " + failCount.get());
        log("INFO", "===========================");
    }

    public void stop() {
        isRunning.set(false);
        if (activeDriver != null) {
            try { activeDriver.quit(); } catch (Exception ignored) {}
            activeDriver = null;
        }
    }

    private void log(String type, String msg) {
        String entry = ts() + "|" + type + "|" + msg;
        runLog.add(entry);
        System.out.println("[" + type + "] " + msg);
    }

    private static String ts() {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
    }
}
