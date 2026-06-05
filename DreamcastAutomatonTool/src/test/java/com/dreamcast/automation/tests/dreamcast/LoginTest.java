package com.dreamcast.automation.tests.dreamcast;

import com.dreamcast.automation.base.BaseTest;
import com.dreamcast.automation.config.ConfigReader;
import com.dreamcast.automation.listeners.RetryAnalyzer;
import com.dreamcast.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void initPage() {
        loginPage = new LoginPage(driver);
        loginPage.navigateToLogin();
    }

    @Test(description = "Valid credentials should log in successfully",
          retryAnalyzer = RetryAnalyzer.class)
    public void testValidLogin() {
        loginPage.loginWith(ConfigReader.username(), ConfigReader.password());
        Assert.assertTrue(loginPage.isLoginSuccessful(),
            "Login failed — dashboard not reached after valid credentials.");
    }

    @Test(description = "Invalid password should show error message")
    public void testInvalidPassword() {
        loginPage.loginWith(ConfigReader.username(), "WrongPassword@999");
        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "No error message shown for invalid password.");
    }

    @Test(description = "Empty credentials should show validation error")
    public void testEmptyCredentials() {
        loginPage.clickLogin();
        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "No validation error shown for empty credentials.");
    }
}
