package com.playwrightproject.steps;

import com.playwrightproject.context.TestContext;
import com.playwrightproject.listeners.ExtentReportListener;
import com.playwrightproject.pages.HomePageObjects;
import com.playwrightproject.pages.LoginPageObjects;
import io.cucumber.java.DefaultParameterTransformer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.testng.Assert;

import java.lang.reflect.Type;
import java.util.Map;

public class LoginPageStepDefinitions {

    private final TestContext testContext;

    public LoginPageStepDefinitions(TestContext testContext) {
        this.testContext = testContext;
    }

    private void logStep(String message) {
        ExtentReportListener.logInfo(message);
    }

    @DefaultParameterTransformer
    public Object defaultParameterTransformer(Object fromValue, Type toValueType) {
        if (fromValue instanceof String
                && "java.util.Map<java.lang.String, java.lang.String>".equals(toValueType.getTypeName())) {
            return testContext.resolveMap((String) fromValue);
        }

        return fromValue;
    }

    @Given("I launch the application")
    public void i_launch_the_application() {
        String url = testContext.getTestBase().getProperty("app.url");
        logStep("Launching application URL: " + url);
        testContext.getTestBase().getPage().navigate(url);
        testContext.getTestBase().getPage().waitForTimeout(2000);
        ExtentReportListener.logPass("Application launched successfully");
    }

    @When("I enter username {string}")
    public void i_enter_username(String username) {
        //testContext.getTestBase().getPage().fill("input[name='username']", username);
        logStep("Entering username");
        System.out.println("I enter username");
    }
    @When("user login with {string} and {string}")
    public void user_login_with_and(String username, String password) {
        String resolvedUsername = testContext.resolveValue(username);
        String resolvedPassword = testContext.resolveValue(password);
        logStep("Logging in with resolved test data");

        LoginPageObjects pageObject = new LoginPageObjects(testContext.getTestBase().getPage());
        pageObject.enterUsername(resolvedUsername);
        testContext.getTestBase().getPage().waitForTimeout(1000);
        pageObject.enterPassword(resolvedPassword);
        testContext.getTestBase().getPage().waitForTimeout(1000);
        Assert.assertEquals(pageObject.getEnteredUsername(), resolvedUsername,
            "Username value is not entered correctly.");
        Assert.assertEquals(pageObject.getEnteredPassword(), resolvedPassword,
            "Password value is not entered correctly.");
        ExtentReportListener.logPass("Username and password entered and validated");
    }

    @When("^user enter address info using ['\"]([^'\"]*)['\"] in the form$")
    public void user_enter_address_info_using_in_the_form(Map<String, String> address) {
        logStep("Entering address data from nested JSON map");
        System.out.println("Address info: " + address.get("street") + ", " + address.get("city") + ", " + address.get("state") + " " + address.get("zip"));
        ExtentReportListener.logInfo("Address used: " + address.get("street") + ", "
            + address.get("city") + ", " + address.get("state") + " " + address.get("zip"));
    }

    @Then("I should see the dashboard")
    public void i_should_see_the_dashboard() {
        HomePageObjects homePageObjects = new HomePageObjects(testContext.getTestBase().getPage());
        logStep("Validating dashboard visibility");
        Assert.assertTrue(homePageObjects.isPageReady(), "Home page object has an invalid or closed page instance.");
        System.out.println("I should see the dashboard");
        ExtentReportListener.logPass("Dashboard validation step completed");
    }
}