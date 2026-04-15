package com.playwrightproject.listeners;

import org.testng.IAlterSuiteListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentListener implements IAlterSuiteListener, ISuiteListener {

    /**
     * IAlterSuiteListener.alter() fires BEFORE TestNG processes group filters into its method
     * selection plan. This is the correct place to:
     *  1. Read the <groups><run><include> names as Cucumber tag selectors.
     *  2. Set cucumber.filter.tags and env system properties.
     *  3. Clear the TestNG group filter so runScenario (which belongs to no user group) is never
     *     excluded by the method selector.
     */
    @Override
    public void alter(List<XmlSuite> suites) {
        for (XmlSuite suite : suites) {
            for (XmlTest test : suite.getTests()) {
                String env = extractEnvFromResource(test.getParameter("env.resources"));
                System.setProperty("env", env);

                String tagExpression = buildTagExpression(test.getIncludedGroups());
                if (tagExpression == null || tagExpression.isBlank()) {
                    tagExpression = test.getParameter("cucumber.tags");
                }
                if (tagExpression != null && !tagExpression.isBlank()) {
                    System.setProperty("cucumber.filter.tags", tagExpression);
                }

                // Clear group filter — groups served only as Cucumber tag config, not method filters.
                test.setIncludedGroups(Collections.emptyList());
            }
        }
    }

    @Override
    public void onStart(ISuite suite) {
        System.out.println("Environment set to: " + System.getProperty("env") + " from suite: " + suite.getName());
        System.out.println("Cucumber tag filter set to: " + System.getProperty("cucumber.filter.tags"));
    }

    @Override
    public void onFinish(ISuite suite) {
        // Not needed
    }

    private String buildTagExpression(List<String> includedGroups) {
        if (includedGroups == null || includedGroups.isEmpty()) {
            return null;
        }
        List<String> tagGroups = includedGroups.stream()
                .filter(g -> !g.equalsIgnoreCase("cucumber"))
                .collect(Collectors.toList());
        if (tagGroups.isEmpty()) {
            return null;
        }
        return tagGroups.stream()
                .map(g -> "@" + g)
                .collect(Collectors.joining(" or "));
    }

    private String extractEnvFromResource(String envResources) {
        if (envResources != null) {
            if (envResources.contains("/dev") || envResources.contains("\\dev")) return "dev";
            if (envResources.contains("/sit") || envResources.contains("\\sit")) return "sit";
            if (envResources.contains("/uat") || envResources.contains("\\uat")) return "uat";
        }
        return "dev";
    }
}
