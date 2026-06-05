package com.dreamcast.automation.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.dreamcast.automation.base.BaseTest;
import com.dreamcast.automation.config.ConfigReader;
import com.dreamcast.automation.report.ExtentReportManager;
import com.dreamcast.automation.util.LoggerUtil;
import com.dreamcast.automation.util.ScreenshotUtil;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getDescription().isEmpty()
            ? result.getName()
            : result.getMethod().getDescription();
        ExtentReportManager.createTest(name);
        LoggerUtil.info("START: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentReportManager.getTest().log(Status.PASS, "Test PASSED");
        LoggerUtil.pass("PASS: " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        LoggerUtil.fail("FAIL: " + result.getName() + " — " + result.getThrowable().getMessage());

        if (ConfigReader.screenshotOnFail()) {
            try {
                Object instance = result.getInstance();
                if (instance instanceof BaseTest) {
                    String path = ScreenshotUtil.capture(
                        ((BaseTest) instance).getDriver(),
                        "FAIL_" + result.getName(),
                        ConfigReader.screenshotDir()
                    );
                    if (path != null) {
                        test.fail("Screenshot",
                            MediaEntityBuilder.createScreenCaptureFromPath(path).build());
                    }
                }
            } catch (Exception e) {
                LoggerUtil.warn("Could not capture screenshot: " + e.getMessage());
            }
        }

        test.log(Status.FAIL, result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentReportManager.getTest().log(Status.SKIP, "Test SKIPPED: " + result.getThrowable());
        LoggerUtil.warn("SKIP: " + result.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
        LoggerUtil.info("Suite finished. Pass=" + context.getPassedTests().size()
            + " Fail=" + context.getFailedTests().size()
            + " Skip=" + context.getSkippedTests().size());
    }

    @Override public void onStart(ITestContext context) {}
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
}
