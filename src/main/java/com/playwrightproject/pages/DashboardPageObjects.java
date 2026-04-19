package com.playwrightproject.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.playwrightproject.constants.AppEnum;

import java.util.Objects;

public class DashboardPageObjects {

    private final Page page;
    
    private String  DASHBOARD_HEADER = "//h6[@class='oxd-text oxd-text--h6 oxd-topbar-header-breadcrumb-module']";
    private String IMG_ORANGEHRM = "//img[@alt='client brand banner']";
    private String INPUT_SEARCH = "input[placeholder='Search']";
    private String LABEL_PANELOPTIONS ="//ul[@class='oxd-main-menu']/li";

    public DashboardPageObjects(Page page) {
        this.page = Objects.requireNonNull(page, "Playwright page is null. Ensure browser setup is completed before using HomePageObjects.");
    }

    public boolean isPageReady() {
        return !page.isClosed();
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public String getHomePageTitle() {
        return page.title();
    }

    public Locator dashboardHeader() {
        return page.locator(AppEnum.DASHBOARD_HEADER).first();
    }

    public boolean isDashboardHeaderVisible() {
        return dashboardHeader().isVisible();
    }
    
    public String getPageHeader() {
    	return page.locator(DASHBOARD_HEADER).textContent();
    }

    public boolean getDashboardPageImage() {
        boolean isDisplay = page.locator(IMG_ORANGEHRM).isVisible();
        return isDisplay;
    }

    public boolean getSearchTextField() {
        boolean isDisplay = page.locator(INPUT_SEARCH).isVisible();
        return isDisplay;
    }

    public void verifySidePanelOptions(java.util.List<String> expectedOptions) {
        java.util.List<Locator> optionElements = page.locator(LABEL_PANELOPTIONS).all();
        java.util.List<String> actualOptions = optionElements.stream()
                .map(Locator::textContent)
                .map(String::trim)
                .toList();

        if (!actualOptions.containsAll(expectedOptions)) {
            throw new AssertionError("Expected side panel options not found. Expected: " + expectedOptions + ", Actual: " + actualOptions);
        }
    }
}
