package com.dreamcast.automation.pages;

import com.dreamcast.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By EMAIL_FIELD    = By.id("email");
    private static final By PASSWORD_FIELD = By.id("password");
    private static final By LOGIN_BUTTON   = By.xpath("//button[@type='submit']");
    private static final By ERROR_MESSAGE  = By.cssSelector(".alert-danger, .error-message");
    private static final By USER_AVATAR    = By.cssSelector(".user-avatar, .user-menu, [data-user]");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage enterEmail(String email) {
        type(EMAIL_FIELD, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(PASSWORD_FIELD, password);
        return this;
    }

    public void clickLogin() {
        click(LOGIN_BUTTON);
    }

    public LoginPage loginWith(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
        return this;
    }

    public boolean isLoginSuccessful() {
        try {
            waitForUrl("dashboard");
            return true;
        } catch (Exception e) {
            return isDisplayed(USER_AVATAR);
        }
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(ERROR_MESSAGE);
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }

    public void navigateToLogin() {
        navigateTo(com.dreamcast.automation.config.ConfigReader.loginUrl());
    }
}
