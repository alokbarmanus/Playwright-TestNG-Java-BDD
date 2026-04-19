package com.playwrightproject.hooks;

import com.playwrightproject.context.TestContext;
import com.playwrightproject.listeners.ExtentReportListener;
import com.playwrightproject.utils.JsonDataLoader;
import com.playwrightproject.utils.RuntimeMemoryManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Hooks {

    private final TestContext testContext;
    private static final ConcurrentMap<String, AtomicInteger> SCENARIO_ITERATION = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> FAILURE_SCREENSHOT_CAPTURED = ThreadLocal.withInitial(() -> false);

    public Hooks(TestContext testContext) {
        this.testContext = testContext;
    }

    @Before
    public void setup(Scenario scenario) {
        FAILURE_SCREENSHOT_CAPTURED.set(false);
        RuntimeMemoryManager.clear();
        testContext.getTestBase().setUp();

        Optional<String> dataFileTag = scenario.getSourceTagNames().stream()
                .filter(tag -> tag.startsWith("@dataFile:"))
                .findFirst();

        if (dataFileTag.isPresent()) {
            String rawPath = dataFileTag.get().substring("@dataFile:".length()).trim();
            String filePath = resolveDataFilePath(rawPath);
            int rowIndex = SCENARIO_ITERATION
                    .computeIfAbsent(scenario.getId(), key -> new AtomicInteger(0))
                    .getAndIncrement();
            Map<String, String> data = JsonDataLoader.loadRecordAsMap(filePath, rowIndex);
            testContext.setScenarioData(data);
            System.out.println("Loaded scenario data file: " + filePath + " [row=" + (rowIndex + 1) + "]");
        } else {
            testContext.setScenarioData(Collections.emptyMap());
        }
    }

    @AfterStep
    public void captureFailureStepScreenshot(Scenario scenario) {
        if (!scenario.isFailed() || Boolean.TRUE.equals(FAILURE_SCREENSHOT_CAPTURED.get())) {
            return;
        }

        String screenshotPath = ExtentReportListener.captureScreenshotToFile("step-failure-" + scenario.getName());
        if (screenshotPath != null) {
            ExtentReportListener.logFail("Step failed in scenario: " + scenario.getName());
            ExtentReportListener.attachScreenshot(screenshotPath, "Failed Step Screenshot");
        }
        FAILURE_SCREENSHOT_CAPTURED.set(true);
    }

    private String resolveDataFilePath(String rawPath) {
        String env = System.getProperty("env", "dev");
        return rawPath.replace("${env}", env);
    }

    @After
    public void teardown() {
        FAILURE_SCREENSHOT_CAPTURED.remove();
        testContext.getTestBase().tearDown();
        testContext.setScenarioData(Collections.emptyMap());
        RuntimeMemoryManager.clear();
    }
}
