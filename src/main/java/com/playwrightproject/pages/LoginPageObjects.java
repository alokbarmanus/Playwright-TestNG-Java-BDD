package com.playwrightproject.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class LoginPageObjects {
    public static final String USERNAME_INPUT = "input[name='username']";
    public static final String PASSWORD_INPUT = "input[name='password']";

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

    public String getEnteredUsername() {
        return usernameField().inputValue();
    }

    public String getEnteredPassword() {
        return passwordField().inputValue();
    }
}