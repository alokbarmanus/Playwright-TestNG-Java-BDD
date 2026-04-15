package com.playwrightproject.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.playwrightproject.constants.AppEnum;

import java.util.Objects;

public class HomePageObjects {

    private final Page page;

    public HomePageObjects(Page page) {
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
}
