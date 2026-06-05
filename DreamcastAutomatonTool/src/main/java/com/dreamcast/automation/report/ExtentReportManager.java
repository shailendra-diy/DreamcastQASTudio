package com.dreamcast.automation.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.dreamcast.automation.config.ConfigReader;

import java.io.File;

public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String dir  = ConfigReader.reportDir();
            String name = ConfigReader.reportName();
            new File(dir).mkdirs();

            ExtentSparkReporter spark = new ExtentSparkReporter(dir + "/" + name + ".html");
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("Dreamcast Automation Report");
            spark.config().setReportName("Test Execution Report");
            spark.config().setTimeStampFormat("dd-MM-yyyy HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Project",     "Dreamcast Automation");
            extent.setSystemInfo("Environment", ConfigReader.baseUrl());
            extent.setSystemInfo("Browser",     ConfigReader.browser());
            extent.setSystemInfo("Tester",      "Shailendra");
        }
        return extent;
    }

    public static ExtentTest createTest(String testName) {
        ExtentTest test = getInstance().createTest(testName);
        testThread.set(test);
        return test;
    }

    public static ExtentTest createTest(String testName, String description) {
        ExtentTest test = getInstance().createTest(testName, description);
        testThread.set(test);
        return test;
    }

    public static ExtentTest getTest() {
        return testThread.get();
    }

    public static void flush() {
        if (extent != null) extent.flush();
    }
}
