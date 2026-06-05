package com.dreamcast.automation.listeners;

import com.dreamcast.automation.config.ConfigReader;
import com.dreamcast.automation.util.LoggerUtil;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempt = 0;
    private final int maxRetries = ConfigReader.retryCount();

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < maxRetries) {
            attempt++;
            LoggerUtil.warn("Retrying test [" + result.getName() + "] — attempt " + attempt + " of " + maxRetries);
            return true;
        }
        return false;
    }
}
