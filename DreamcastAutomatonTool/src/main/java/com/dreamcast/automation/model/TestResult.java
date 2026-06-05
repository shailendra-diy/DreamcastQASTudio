package com.dreamcast.automation.model;

public class TestResult {

    public enum Status { PASS, FAIL, PENDING }

    private final String stepName;
    private final Status status;
    private final String message;
    private final String screenshotPath;

    public TestResult(String stepName, Status status, String message, String screenshotPath) {
        this.stepName       = stepName;
        this.status         = status;
        this.message        = message;
        this.screenshotPath = screenshotPath;
    }

    public String getStepName()       { return stepName; }
    public Status getStatus()         { return status; }
    public String getMessage()        { return message; }
    public String getScreenshotPath() { return screenshotPath; }

    public boolean isPassed() { return status == Status.PASS; }
}
