package com.playwrightproject.steps;

import com.playwrightproject.context.TestContext;
import com.playwrightproject.listeners.ExtentReportListener;
import com.playwrightproject.pages.HomePageObjects;
import io.cucumber.java.en.Then;
import org.testng.Assert;

public class HomePageStepDefinitions {

    private final TestContext testContext;

    public HomePageStepDefinitions(TestContext testContext) {
        this.testContext = testContext;
    }

    @Then("I confirm homepage object is initialized")
    public void i_confirm_homepage_object_is_initialized() {
        HomePageObjects homePageObjects = new HomePageObjects(testContext.getTestBase().getPage());
        Assert.assertTrue(homePageObjects.isPageReady(), "Home page object has an invalid or closed page instance.");
        ExtentReportListener.logPass("Home page object initialized successfully with non-null page instance");
    }
}
