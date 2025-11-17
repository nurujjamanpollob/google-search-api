package com.eazeeditor.searchengineapi.screenshot;

import javadev.stringcollections.textreplacor.io.PathResolver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v142.emulation.Emulation;
import org.openqa.selenium.devtools.v142.page.Page;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

public class ScreenshotService {

    private final String chromeBinary;
    private String userDataDir;

    public ScreenshotService() throws IllegalStateException {
        String chromeBinaryPath = System.getenv("CHROME_BINARY_PATH");
        if (chromeBinaryPath == null || chromeBinaryPath.isEmpty()) {
            throw new IllegalStateException("CHROME_BINARY_PATH environment variable is not set.");
        }
        this.chromeBinary = chromeBinaryPath;
    }

    public ScreenshotService(String chromeBinaryPath) {
        if (chromeBinaryPath == null || chromeBinaryPath.isEmpty() || !PathResolver.isPathExists(chromeBinaryPath)) {
            throw new IllegalArgumentException("Invalid chromeBinaryPath");
        }
        this.chromeBinary = chromeBinaryPath;
    }

    public void setUserDataDir(String userDataDir) {
        this.userDataDir = userDataDir;
    }

    public void captureScreenshot(String url, String localPath) {
        captureScreenshot(url, localPath, 0);
    }

    public void captureScreenshot(String url, String localPath, long delayInMillis) {
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.setBinary(this.chromeBinary);
            options.addArguments(
                    "--headless=new",
                    "--disable-gpu",
                    "--window-size=1920,1080",
                    "--no-sandbox",
                    "--disable-dev-shm-usage"
            );

            if (this.userDataDir != null && !this.userDataDir.isEmpty()) {
                options.addArguments("--user-data-dir=" + this.userDataDir);
            }

            driver = new ChromeDriver(options);
            driver.get(url);

            if (delayInMillis > 0) {
                Thread.sleep(delayInMillis);
            }

            File screenshotFile = ((org.openqa.selenium.TakesScreenshot)driver).getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            Files.copy(screenshotFile.toPath(), new File(localPath).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Screenshot captured for " + url + " at " + localPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to capture standard screenshot for " + url, e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public void captureLongScreenshot(String url, String localPath) {
        captureLongScreenshot(url, localPath, 3000);
    }

    public void captureLongScreenshot(String url, String localPath, long delayInMillis) {
        ChromeDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.setBinary(this.chromeBinary);
            options.addArguments(
                    "--headless=new",
                    "--disable-gpu",
                    "--window-size=1920,1080",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-web-security",
                    "--disable-features=VizDisplayCompositor",
                    "--disable-background-timer-throttling",
                    "--disable-renderer-backgrounding",
                    "--disable-backgrounding-occluded-windows"
            );

            if (this.userDataDir != null && !this.userDataDir.isEmpty()) {
                options.addArguments("--user-data-dir=" + this.userDataDir);
            }

            driver = new ChromeDriver(options);
            DevTools devTools = driver.getDevTools();
            devTools.createSession();

            driver.get(url);

            robustScrollToBottom(driver, delayInMillis);

            // Get actual page dimensions
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long finalHeight = (Long) js.executeScript(
                    "return Math.max(" +
                            "document.body.scrollHeight, document.body.offsetHeight, " +
                            "document.documentElement.clientHeight, document.documentElement.scrollHeight, " +
                            "document.documentElement.offsetHeight);"
            );
            Long finalWidth = (Long) js.executeScript(
                    "return Math.max(" +
                            "document.body.scrollWidth, document.body.offsetWidth, " +
                            "document.documentElement.clientWidth, document.documentElement.scrollWidth, " +
                            "document.documentElement.offsetWidth);"
            );

            // Set device metrics for full page capture
            // This is the "zoom out viewport until whole content fit" part.
            // It emulates a viewport as tall and wide as the entire page.
            devTools.send(Emulation.setDeviceMetricsOverride(
                    finalWidth.intValue(), finalHeight.intValue(), 1, false,
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()
            ));

            // Capture screenshot
            String data = devTools.send(Page.captureScreenshot(
                    Optional.of(Page.CaptureScreenshotFormat.PNG),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(true), // captureBeyondViewport
                    Optional.empty(),
                    Optional.empty()
            ));

            byte[] imageBytes = Base64.getDecoder().decode(data);
            try (FileOutputStream fos = new FileOutputStream(localPath)) {
                fos.write(imageBytes);
            }

            System.out.println("Long screenshot captured for " + url + " at " + localPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to capture long screenshot for " + url, e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // NEW: Overloaded method for convenience
    public void capturePartialHeightScreenshot(String url, String localPath, int percentage) {
        capturePartialHeightScreenshot(url, localPath, percentage, 3000);
    }

    /**
     * NEW: Captures a screenshot of the page, but only up to a certain percentage
     * of the total page height.
     *
     * @param url           The URL to capture.
     * @param localPath     The file path to save the screenshot.
     * @param percentage    The percentage of the total page height to capture (1-100).
     * @param delayInMillis The initial delay to wait for the page to settle.
     */
    public void capturePartialHeightScreenshot(String url, String localPath, int percentage, long delayInMillis) {
        if (percentage < 1 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 1 and 100.");
        }

        ChromeDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.setBinary(this.chromeBinary);
            options.addArguments(
                    "--headless=new",
                    "--disable-gpu",
                    "--window-size=1920,1080",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-web-security",
                    "--disable-features=VizDisplayCompositor",
                    "--disable-background-timer-throttling",
                    "--disable-renderer-backgrounding",
                    "--disable-backgrounding-occluded-windows"
            );

            if (this.userDataDir != null && !this.userDataDir.isEmpty()) {
                options.addArguments("--user-data-dir=" + this.userDataDir);
            }

            driver = new ChromeDriver(options);
            DevTools devTools = driver.getDevTools();
            devTools.createSession();

            driver.get(url);

            // Scroll to bottom to load all content, which ensures we get the *true* full height
            robustScrollToBottom(driver, delayInMillis);

            // Get actual page dimensions
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long fullHeight = (Long) js.executeScript(
                    "return Math.max(" +
                            "document.body.scrollHeight, document.body.offsetHeight, " +
                            "document.documentElement.clientHeight, document.documentElement.scrollHeight, " +
                            "document.documentElement.offsetHeight);"
            );
            Long fullWidth = (Long) js.executeScript(
                    "return Math.max(" +
                            "document.body.scrollWidth, document.body.offsetWidth, " +
                            "document.documentElement.clientWidth, document.documentElement.scrollWidth, " +
                            "document.documentElement.offsetWidth);"
            );

            // NEW: Calculate the target height based on the percentage
            int targetHeight = (int) (fullHeight * (percentage / 100.0));
            // Ensure height is at least 1px
            targetHeight = Math.max(targetHeight, 1);

            // Set device metrics for the *partial* page capture
            devTools.send(Emulation.setDeviceMetricsOverride(
                    fullWidth.intValue(), targetHeight, 1, false,
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()
            ));

            // Capture screenshot
            String data = devTools.send(Page.captureScreenshot(
                    Optional.of(Page.CaptureScreenshotFormat.PNG),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(true), // captureBeyondViewport
                    Optional.empty(),
                    Optional.empty()
            ));

            byte[] imageBytes = Base64.getDecoder().decode(data);
            try (FileOutputStream fos = new FileOutputStream(localPath)) {
                fos.write(imageBytes);
            }

            System.out.println("Partial screenshot (" + percentage + "%) captured for " + url + " at " + localPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to capture partial screenshot for " + url, e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }


    /**
     * Replaces both waitForAnimations and scrollToBottomWithAnimationSupport.
     * This method scrolls down the page iteratively until the page height stabilizes,
     * ensuring all lazy-loaded content is triggered and loaded.
     */
    private void robustScrollToBottom(ChromeDriver driver, long initialDelayInMillis) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Initial wait for the page to settle (using the provided delay)
        if (initialDelayInMillis > 0) {
            Thread.sleep(initialDelayInMillis);
        }
        wait.until(d -> driver.executeScript("return document.readyState").equals("complete"));

        long lastHeight = (Long) driver.executeScript("return document.body.scrollHeight");
        long currentScroll = 0;
        long viewportHeight = (Long) driver.executeScript("return window.innerHeight");
        long scrollStep = (long) (viewportHeight * 0.85); // Scroll in 85% viewport steps

        while (true) {
            // 2. Scroll down by one step
            currentScroll += scrollStep;
            driver.executeScript("window.scrollTo(0, " + currentScroll + ");");

            // 3. Give a brief, pragmatic pause to trigger JS-based lazy-loading
            Thread.sleep(400);

            // 4. Wait for JS/jQuery to be idle
            try {
                wait.until(d -> (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "return (typeof jQuery === 'undefined' || jQuery.active === 0)"
                ));
            } catch (Exception e) {
                // Ignore if jQuery check fails (e.g., jQuery not present or timeout)
            }

            // 5. Check the new page height after loading
            long newHeight = (Long) driver.executeScript("return document.body.scrollHeight");

            // 6. Check if we're done:
            // If the height hasn't changed AND we've scrolled past the bottom, we're done.
            if (newHeight == lastHeight && currentScroll >= newHeight) {
                break;
            }

            // If height has changed, update it and continue the loop
            lastHeight = newHeight;

            // Don't scroll unnecessarily past the new bottom
            if (currentScroll > newHeight) {
                currentScroll = newHeight;
            }
        }

        // 7. One final scroll to the absolute bottom
        driver.executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // 8. A final pragmatic wait for any footer animations/content
        Thread.sleep(2000);
    }


    public static String getAutomationProfilePath() {
        String userHome = System.getProperty("user.home");
        String separator = File.separator;
        return userHome + separator + "SeleniumChromeAutomationProfile";
    }
}