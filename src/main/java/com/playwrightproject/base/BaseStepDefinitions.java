package com.playwrightproject.base;

import com.microsoft.playwright.Page;
import com.playwrightproject.context.TestContext;
import com.playwrightproject.pages.DashboardPageObjects;
import com.playwrightproject.pages.LoginPageObjects;

import java.util.Map;

public abstract class BaseStepDefinitions {

    protected TestContext testContext;

    protected Page page() {
        if (testContext == null) {
            throw new IllegalStateException("TestContext is not initialized in step definition.");
        }
        return testContext.getTestBase().getPage();
    }

    protected String property(String key) {
        if (testContext == null) {
            throw new IllegalStateException("TestContext is not initialized in step definition.");
        }
        return testContext.getTestBase().getProperty(key);
    }

    protected String resolveValue(String value) {
        if (testContext == null) {
            throw new IllegalStateException("TestContext is not initialized in step definition.");
        }
        return testContext.resolveValue(value);
    }

    protected Map<String, String> credentials(String username, String password) {
        return Map.of(
            "username", resolveValue(username),
            "password", resolveValue(password)
        );
    }

    protected LoginPageObjects loginPage() {
        return new LoginPageObjects(page());
    }

    protected DashboardPageObjects dashboardPage() {
        return new DashboardPageObjects(page());
    }
}
