package com.eazeeditor.searchengineapi.screenshot;

import javadev.stringcollections.textreplacor.io.PathResolver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v142.dom.model.Rect;
import org.openqa.selenium.devtools.v142.page.Page;
import org.openqa.selenium.devtools.v142.page.model.Viewport;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;

/**
 * @author nurujjamanpollob
 * Service to capture screenshots of web pages using Selenium WebDriver and Chrome.
 * This class supports chrome binary path configuration via environment variable or constructor parameter,
 * and require chrome v142 to work correctly, haven't tested with other versions yet, and may not work with other versions.
 */
public class ScreenshotService {

    private final String chromeBinary;

    public ScreenshotService() throws IllegalStateException {
        // throw error if CHROME_BINARY_PATH env variable is not set
        String chromeBinaryPath = System.getenv("CHROME_BINARY_PATH");
        if (chromeBinaryPath == null || chromeBinaryPath.isEmpty()) {
            throw new IllegalStateException("CHROME_BINARY_PATH environment variable is not set. use export CHROME_BINARY_PATH=/path/to/chrome or initialize this class with ScreenshotService(String chromeBinaryPath)");
        }
        this.chromeBinary = chromeBinaryPath;
    }

    /**
     * Constructor to initialize ScreenshotService with a specific Chrome binary path.
     * Note: Tested with chrome v142 only, may not work with other versions, and not tested yet.
     * @param chromeBinaryPath the path to the Chrome binary
     */
    public ScreenshotService(String chromeBinaryPath) {
        if (chromeBinaryPath == null || chromeBinaryPath.isEmpty() || !PathResolver.isPathExists(chromeBinaryPath)) {
            throw new IllegalArgumentException("chromeBinaryPath cannot be null or empty");
        }
        this.chromeBinary = chromeBinaryPath;
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
                    "--headless",
                    "--disable-gpu",
                    "--window-size=1920,1080"
            );

            driver = new ChromeDriver(options);
            driver.get(url);

            if (delayInMillis > 0) {
                Thread.sleep(delayInMillis);
            }

            File screenshotFile = ((org.openqa.selenium.TakesScreenshot)driver).getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            Files.copy(screenshotFile.toPath(), new File(localPath).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Screenshot captured for " + url + " at " + localPath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * Capture long screenshot and save to the output path
     * @param url the url to capture screenshot
     * @param localPath the local path to save the screenshot
     */
    public void captureLongScreenshot(String url, String localPath) {
        captureLongScreenshot(url, localPath, 0);
    }


    /**
     * Capture long screenshot and save to the output path
     * @param url the url to capture screenshot
     * @param localPath the local path to save the screenshot
     * @param delayInMillis the delay in milliseconds before taking the screenshot
     */
    public void captureLongScreenshot(String url, String localPath, long delayInMillis) {
        ChromeDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.setBinary(this.chromeBinary);
            options.addArguments(
                    "--headless",
                    "--disable-gpu",
                    "--window-size=1920,1080"
            );

            driver = new ChromeDriver(options);
            DevTools devTools = driver.getDevTools();
            devTools.createSession();

            driver.get(url);

            if (delayInMillis > 0) {
                Thread.sleep(delayInMillis);
            }

            // Scroll down the page to trigger lazy-loaded elements
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
            long totalHeight = (long) js.executeScript("return document.body.scrollHeight");
            long viewportHeight = (long) js.executeScript("return window.innerHeight");
            long scrolledHeight = 0;

            while (scrolledHeight < totalHeight) {
                js.executeScript("window.scrollBy(0, arguments[0]);", viewportHeight);
                scrolledHeight += viewportHeight;
                Thread.sleep(1000); // Wait for content to load
                long newTotalHeight = (long) js.executeScript("return document.body.scrollHeight");
                if (newTotalHeight > totalHeight) {
                    totalHeight = newTotalHeight;
                }
            }
            // Wait for any final rendering
            Thread.sleep(2000);


            // get page max width and height
            Rect rect = devTools.send(Page.getLayoutMetrics())
                    .getContentSize();
            Number width = rect.getWidth();
            Number height = rect.getHeight();

            devTools.send(org.openqa.selenium.devtools.v142.emulation.Emulation.setDeviceMetricsOverride(
                    width.intValue(),
                    height.intValue(),
                    1,
                    false,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            ));

            String data = devTools.send(Page.captureScreenshot(
                    Optional.of(Page.CaptureScreenshotFormat.PNG),
                    Optional.empty(),
                    Optional.of(new Viewport(0, 0, width, height, 1)),
                    Optional.of(true), // Use default for fromSurface
                    Optional.empty(), // Not needed when clip is specified
                    Optional.empty()
            ));

            byte[] imageBytes = Base64.getDecoder().decode(data);
            try (FileOutputStream fos = new FileOutputStream(localPath)) {
                fos.write(imageBytes);
            }

            System.out.println("Long screenshot captured for " + url + " at " + localPath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
