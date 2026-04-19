package com.playwrightproject.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class LoginPageObjects {
    
    public static final String USERNAME_INPUT = "input[name='username']";
    public static final String PASSWORD_INPUT = "input[name='password']";
    public static final String LOGIN_BUTTON = "button[type='submit']";

    protected Page page;

    public LoginPageObjects(Page page) {
        this.page = page;
    }

    public Locator usernameField() {
        return page.locator(USERNAME_INPUT);
    }

    public Locator passwordField() {
        return page.locator(PASSWORD_INPUT);
    }

    public void enterUsername(String username) {
        usernameField().fill(username);
    }

    public void enterPassword(String password) {
        passwordField().fill(password);
    }
    
    public void clickOnLoginButton() {
    	page.locator(LOGIN_BUTTON).click();
    }

    public String getEnteredUsername() {
        return usernameField().inputValue();
    }

    public String getEnteredPassword() {
        return passwordField().inputValue();
    }
    public void loginToApplication(String username, String password) {
    	page.locator(USERNAME_INPUT).fill(username);
    	page.waitForTimeout(1000);
    	page.locator(PASSWORD_INPUT).fill(password);
    	page.waitForTimeout(1000);
    	page.locator(LOGIN_BUTTON).click();
    	page.waitForTimeout(2000);
    }
}