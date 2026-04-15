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

import java.util.Base64;
import java.util.List;

public class ExtentReportListener implements ITestListener {

    private static final String REPORT_PATH = "target/extent-reports/TestExecutionReport.html";

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
            String base64 = captureScreenshotAsBase64();
            if (base64 != null) {
                test.fail(result.getThrowable(),
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64, "Failure Screenshot").build());
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

    /** Takes a Playwright screenshot of the current page and returns it as a Base64 string. */
    private String captureScreenshotAsBase64() {
        Page page = TestBase.getCurrentPage();
        if (page == null) {
            return null;
        }
        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
        return Base64.getEncoder().encodeToString(screenshot);
    }
}
