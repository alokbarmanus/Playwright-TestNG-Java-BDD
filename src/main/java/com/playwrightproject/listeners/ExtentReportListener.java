package com.playwrightproject.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.microsoft.playwright.Page;
import com.playwrightproject.base.TestBase;
import io.cucumber.testng.PickleWrapper;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentReportListener implements ITestListener {

    private static final String REPORT_PATH = "target/extent-reports/TestExecutionReport.html";
    private static final String REPORT_DIR = "target/extent-reports";
    private static final String IMG_DIR = REPORT_DIR + "/img";
    private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private static final ExtentReports EXTENT = buildExtentReports();
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static final ThreadLocal<ITestResult> CURRENT_RESULT = new ThreadLocal<>();

    public static void logInfo(String message) {
        log(Status.INFO, message);
    }

    public static void logPass(String message) {
        log(Status.PASS, message);
    }

    public static void logWarning(String message) {
        log(Status.WARNING, message);
    }

    public static void logFail(String message) {
        log(Status.FAIL, message);
    }

    private static void log(Status status, String message) {
        // Bind log records to current TestNG test method so reporter output is captured.
        ITestResult currentResult = CURRENT_RESULT.get();
        if (currentResult != null) {
            Reporter.setCurrentTestResult(currentResult);
        }
        Reporter.log("[" + status + "] " + message);
        if (currentResult != null) {
            Reporter.setCurrentTestResult(null);
        }

        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.log(status, message);
        }
    }

    private static ExtentReports buildExtentReports() {
        ensureReportDirectories();
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        spark.config().setTheme(Theme.DARK);
        spark.config().setReportName("Playwright BDD Test Report");
        spark.config().setDocumentTitle("Test Execution Report");

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Framework", "Playwright + Cucumber + TestNG");
        extent.setSystemInfo("Author", "Alok Barman");
        extent.setSystemInfo("Environment", System.getProperty("env", "dev").toUpperCase());
        return extent;
    }

    // ------------------------------------------------------------------ lifecycle

    @Override
    public void onStart(ITestContext context) {
        System.out.println("[ExtentReport] Suite started: " + context.getSuite().getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("[ExtentReport] Suite finished. Flushing report to: " + REPORT_PATH);
        EXTENT.flush();
        CURRENT_TEST.remove();
        CURRENT_RESULT.remove();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String scenarioName = resolveScenarioName(result);
        List<String> tags = resolveScenarioTags(result);

        ExtentTest test = EXTENT.createTest(scenarioName);
        tags.forEach(test::assignCategory);
        test.assignDevice(System.getProperty("env", "dev").toUpperCase());

        CURRENT_TEST.set(test);
        CURRENT_RESULT.set(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.pass("Scenario passed");
        }
        CURRENT_TEST.remove();
        CURRENT_RESULT.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null) {
            CURRENT_RESULT.remove();
            return;
        }
        try {
            String screenshotPath = captureScreenshotToFile("test-failure");
            if (screenshotPath != null) {
                test.fail(result.getThrowable(),
                    MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath, "Failure Screenshot").build());
            } else {
                test.fail(result.getThrowable());
            }
        } catch (Exception e) {
            test.fail("Screenshot capture failed: " + e.getMessage());
            test.fail(result.getThrowable());
        } finally {
            CURRENT_TEST.remove();
            CURRENT_RESULT.remove();
        }
    }

    public static String captureScreenshotToFile(String namePrefix) {
        Page page = TestBase.getCurrentPage();
        if (page == null) {
            return null;
        }

        ensureReportDirectories();
        String safePrefix = sanitizeFileName(namePrefix == null || namePrefix.isBlank() ? "screenshot" : namePrefix);
        String fileName = safePrefix + "_" + TS_FORMATTER.format(LocalDateTime.now())
            + "_t" + Thread.currentThread().getId() + ".png";
        Path outputPath = Paths.get(IMG_DIR, fileName);
        page.screenshot(new Page.ScreenshotOptions().setPath(outputPath).setFullPage(false));
        return "img/" + fileName;
    }

    public static void attachScreenshot(String screenshotPath, String title) {
        if (screenshotPath == null || screenshotPath.isBlank()) {
            return;
        }

        ExtentTest test = CURRENT_TEST.get();
        if (test == null) {
            return;
        }

        String label = (title == null || title.isBlank()) ? "Screenshot" : title;
        try {
            test.info(label, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath, label).build());
        } catch (Exception e) {
            test.warning("Unable to attach screenshot to report: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null) {
            CURRENT_RESULT.remove();
            return;
        }
        if (result.getThrowable() != null) {
            test.skip(result.getThrowable());
        } else {
            test.skip("Scenario skipped");
        }
        CURRENT_TEST.remove();
        CURRENT_RESULT.remove();
    }

    // ------------------------------------------------------------------ helpers

    /** Extracts the Cucumber scenario name from TestNG parameters. */
    private String resolveScenarioName(ITestResult result) {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof PickleWrapper) {
            return ((PickleWrapper) params[0]).getPickle().getName();
        }
        return result.getMethod().getMethodName();
    }

    /** Extracts Cucumber tags (e.g. @smoke, @regression) from the scenario. */
    private List<String> resolveScenarioTags(ITestResult result) {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof PickleWrapper) {
            return ((PickleWrapper) params[0]).getPickle().getTags();
        }
        return List.of();
    }

    private static void ensureReportDirectories() {
        try {
            Files.createDirectories(Paths.get(IMG_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create extent report image folder: " + IMG_DIR, e);
        }
    }

    private static String sanitizeFileName(String value) {
        return value.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
