package com.playwrightproject.steps;

import com.playwrightproject.base.TestBase;
import com.playwrightproject.constants.AppEnum;
import com.playwrightproject.context.TestContext;
import com.playwrightproject.listeners.ExtentReportListener;
import io.cucumber.java.en.Then;
import org.testng.Assert;

public class DashboardPageStepDefinitions extends TestBase {

	public DashboardPageStepDefinitions(TestContext testContext) {
		this.testContext = testContext;
	}

	@Then("I confirm homepage object is initialized")
	public void i_confirm_homepage_object_is_initialized() {
		Assert.assertTrue(dashboardPage().isPageReady(), "Home page object has an invalid or closed page instance.");
		ExtentReportListener.logPass("Home page object initialized successfully with non-null page instance");
	}

	// Dashboard Page
	@Then("I should see the dashboard")
	public void i_should_see_the_dashboard() {
		ExtentReportListener.logInfo("Validating dashboard visibility");
		Assert.assertTrue(dashboardPage().isPageReady(), "Home page object has an invalid or closed page instance.");
		System.out.println("I should see the dashboard");
		ExtentReportListener.logPass("Dashboard validation step completed");
	}

	@Then("user verifies page header as Dashboard")
	public void user_verifies_page_header_as_Dashboard() {
		ExtentReportListener.logInfo("Validating dashboard visibility");
		testContext.getTestBase().getPage().waitForTimeout(3000);
		String actualPageHeader = dashboardPage().getPageHeader();
		Assert.assertEquals(actualPageHeader, AppEnum.DASHBOARD_HEADER);
		ExtentReportListener.logPass("User verifies page header as " + AppEnum.DASHBOARD_HEADER);
	}

	@Then("user verifies OrangeHRM image is display in left side panel in dashboard page")
	public void user_verifies_orange_hrm_image_is_display_in_left_side_panel_in_dashboard_page() {
		ExtentReportListener.logInfo("Validating Dashboard Page Image OrangeHRM");
		Assert.assertTrue(dashboardPage().getDashboardPageImage(), "OrangeHRM image is not visible in the left side panel of the dashboard page.");
		ExtentReportListener.logPass("User verifies OrangeHRM image is displayed in the left side panel of the dashboard page");
	}

	@Then("user verifies Search box is display in left side panel in dashboard page")
	public void user_verifies_search_box_is_display_in_left_side_panel_in_dashboard_page() {
		ExtentReportListener.logInfo("Validating Search Input Field in Dashboard Page");
		Assert.assertTrue(dashboardPage().getSearchTextField(), "Search input field is not visible in the left side panel of the dashboard page.");
		ExtentReportListener.logPass("User verifies Search input field is displayed in the left side panel of the dashboard page");
	}

	@Then("user verifies side panel options in dashboard page as below")
	public void user_verifies_side_panel_options_in_dashboard_page_as_below(io.cucumber.datatable.DataTable dataTable) {
		ExtentReportListener.logInfo("Validating Side Panel Options in Dashboard Page");
		java.util.List<String> expectedOptions = dataTable.asList();
		dashboardPage().verifySidePanelOptions(expectedOptions);
		ExtentReportListener.logPass("User verifies side panel options in dashboard page as expected");
	}

}
