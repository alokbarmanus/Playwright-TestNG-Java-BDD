package com.playwrightproject.base;

import com.microsoft.playwright.*;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestBase {
    /** Thread-local Page holder so listeners can take screenshots without DI. */
    private static final ThreadLocal<Page> CURRENT_PAGE = new ThreadLocal<>();

    public static Page getCurrentPage() {
        return CURRENT_PAGE.get();
    }

    private static final int DEFAULT_VIEWPORT_WIDTH = 1920;
    private static final int DEFAULT_VIEWPORT_HEIGHT = 1080;
    private static final int DEFAULT_VIEWPORT_WIDTH_OFFSET = 16;
    private static final int DEFAULT_VIEWPORT_HEIGHT_OFFSET = 96;

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected Properties properties;

    public void setUp() {
        playwright = Playwright.create();
        loadProperties(); // Load properties first to get browser.name
        String browserName = getProperty("browser.name");
        boolean headless = Boolean.parseBoolean(getProperty("headless"));
        String viewportMode = getProperty("viewport.mode");
        System.out.println("Launching browser: " + browserName);
        System.out.println("Headless mode: " + headless);
        
        BrowserType browserType;
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
            .setHeadless(headless);

        if (!headless && (viewportMode == null || viewportMode.isBlank() || viewportMode.equalsIgnoreCase("dynamic"))) {
            Rectangle usableBounds = getUsableScreenBounds();
            if (isChromiumFamily(browserName)) {
                launchOptions.setArgs(java.util.Arrays.asList("--window-size=" + usableBounds.width + "," + usableBounds.height));
            } else if (browserName.equalsIgnoreCase("firefox")) {
                launchOptions.setArgs(java.util.Arrays.asList("--width=" + usableBounds.width, "--height=" + usableBounds.height));
            }
        }

        try {
            switch (browserName.toLowerCase()) {
                case "firefox":
                    browserType = playwright.firefox();
                    break;
                case "webkit":
                    browserType = playwright.webkit();
                    break;
                case "chrome":
                    browserType = playwright.chromium();
                    launchOptions.setChannel("chrome");
                    break;
                case "edge":
                    browserType = playwright.chromium();
                    launchOptions.setChannel("msedge");
                    break;
                default:
                    browserType = playwright.chromium();
                    break;
            }
            browser = browserType.launch(launchOptions);
            System.out.println("Browser launched successfully: " + browserName);
        } catch (Exception e) {
            System.out.println("Failed to launch " + browserName + " browser: " + e.getMessage());
            throw e;
        }
        
        int viewportWidth = DEFAULT_VIEWPORT_WIDTH;
        int viewportHeight = DEFAULT_VIEWPORT_HEIGHT;

        if (viewportMode == null || viewportMode.isBlank() || viewportMode.equalsIgnoreCase("dynamic")) {
            try {
                Rectangle usableBounds = getUsableScreenBounds();
                int widthOffset = getIntProperty("viewport.width.offset", DEFAULT_VIEWPORT_WIDTH_OFFSET);
                int heightOffset = getIntProperty("viewport.height.offset", DEFAULT_VIEWPORT_HEIGHT_OFFSET);
                viewportWidth = Math.max(1280, usableBounds.width - widthOffset);
                viewportHeight = Math.max(720, usableBounds.height - heightOffset);
            } catch (HeadlessException e) {
                System.out.println("Headless environment detected. Using default viewport 1920x1080.");
            }
        } else if (viewportMode.equalsIgnoreCase("fixed")) {
            viewportWidth = getIntProperty("viewport.width", DEFAULT_VIEWPORT_WIDTH);
            viewportHeight = getIntProperty("viewport.height", DEFAULT_VIEWPORT_HEIGHT);
        }

        System.out.println("Viewport mode: " + (viewportMode == null || viewportMode.isBlank() ? "dynamic" : viewportMode));
        System.out.println("Viewport size: " + viewportWidth + "x" + viewportHeight);

        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(viewportWidth, viewportHeight));
        page = context.newPage();
        CURRENT_PAGE.set(page);
    }

    public void tearDown() {
        CURRENT_PAGE.remove();
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    private void loadProperties() {
        properties = new Properties();

        // Load common application defaults first
        String appPropsPath = "application.properties";
        try (InputStream appInput = getRequiredResourceAsStream(appPropsPath)) {
            properties.load(appInput);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties from: " + appPropsPath, e);
        }

        // Load environment-specific properties (overrides application.properties values)
        String env = System.getProperty("env", "dev");
        String envPath = "env/" + env + "/env.properties";
        System.out.println("========================================");
        System.out.println("Loading environment: " + env);
        System.out.println("Property file path: " + envPath);
        try (InputStream input = getRequiredResourceAsStream(envPath)) {
            properties.load(input);
            System.out.println("App URL: " + properties.getProperty("app.url"));
            System.out.println("Browser: " + properties.getProperty("browser.name"));
            System.out.println("========================================");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load env properties from: " + envPath, e);
        }
    }

    private InputStream getRequiredResourceAsStream(String path) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalStateException("Classpath resource not found: " + path);
        }
        return inputStream;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid value for " + key + ": " + value + ". Using default: " + defaultValue);
            return defaultValue;
        }
    }

    private Rectangle getUsableScreenBounds() {
        try {
            return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        } catch (HeadlessException e) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return new Rectangle((int) screenSize.getWidth(), (int) screenSize.getHeight());
        }
    }

    private boolean isChromiumFamily(String browserName) {
        return browserName.equalsIgnoreCase("chromium")
                || browserName.equalsIgnoreCase("chrome")
                || browserName.equalsIgnoreCase("edge");
    }

    public Page getPage() {
        return page;
    }
}