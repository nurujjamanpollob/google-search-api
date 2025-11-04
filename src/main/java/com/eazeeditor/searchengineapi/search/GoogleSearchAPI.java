package com.eazeeditor.searchengineapi.search;

import com.eazeeditor.searchengineapi.objects.GoogleSearchResultObject;
import javadev.stringcollections.textreplacor.console.ColoredConsoleOutput;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GoogleSearchAPI {

    private static final String GOOGLE_SEARCH_URL_PREFIX = "https://www.google.com/search?q=";
    private static final String GOOGLE_SEARCH_URL_SUFFIX = "&sourceid=chrome&ie=UTF-8";
    private static final By SEARCH_RESULTS_CONTAINER = By.id("search");
    private static final By CAPTCHA_IDENTIFIER = By.id("recaptcha");
    // Updated selector for individual search results
    private static final By INDIVIDUAL_RESULT_SELECTOR = By.cssSelector("div.MjjYud");
    private static final By LINK_SELECTOR = By.cssSelector("a");
    private static final By TITLE_SELECTOR = By.cssSelector("h3");
    // More specific selector for the description
    private static final By DESCRIPTION_SELECTOR = By.cssSelector("div.VwiC3b");

    private final String driverPath;

    /**
     * Default constructor uses system PATH for ChromeDriver.
     */
    public GoogleSearchAPI() {
        this.driverPath = null;
    }

    /**
     * Constructor to specify a custom ChromeDriver path.
     * @param driverPath The path to the ChromeDriver executable.
     */
    public GoogleSearchAPI(String driverPath) {
        this.driverPath = driverPath;
    }


    /**
     * Searches Google for the given query and returns a list of search results.
     * It initially runs in headless mode. If a CAPTCHA is detected, it relaunches
     * in a visible browser for the user to solve it.
     *
     * @param query The search query.
     * @return A list of GoogleSearchResultObject.
     * @throws UnsupportedEncodingException if the query string cannot be URL encoded.
     */
    public List<GoogleSearchResultObject> search(String query) throws UnsupportedEncodingException {
        String searchUrl = GOOGLE_SEARCH_URL_PREFIX + URLEncoder.encode(query, StandardCharsets.UTF_8) + GOOGLE_SEARCH_URL_SUFFIX;

        // Start headless
        WebDriver driver = createDriver(true);
        try {
            driver.get(searchUrl);

            // Check for CAPTCHA
            if (isCaptchaPresent(driver)) {
                ColoredConsoleOutput.printYellowText("[GoogleSearchAPI] CAPTCHA detected. Relaunching in non-headless mode for manual intervention.");
                // Relaunch in non-headless mode for user to solve CAPTCHA
                driver.quit();
                driver = createDriver(false);
                driver.get(searchUrl);

                // Wait for user to solve CAPTCHA and for search results to appear
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofMinutes(5)); // Long timeout for manual intervention
                wait.until(ExpectedConditions.presenceOfElementLocated(SEARCH_RESULTS_CONTAINER));
            }

            return scrapeResults(driver);
        } finally {
            driver.quit();
        }
    }

    /**
     * Creates a new ChromeDriver instance.
     *
     * @param headless If true, the browser will run in headless mode.
     * @return A new WebDriver instance.
     */
    private WebDriver createDriver(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        // Set custom binary path if provided
        if (this.driverPath != null && !this.driverPath.isEmpty()) {
            options.setBinary(this.driverPath);
        }
        if (headless) {
            options.addArguments("--headless=new");
        }

        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        String automationUserDataPath = getAutomationProfilePath();
        java.io.File automationUserDataDir = new java.io.File(automationUserDataPath);
        java.io.File automationProfileDir = new java.io.File(automationUserDataDir, "Default");

        // If the specific "Default" profile doesn't exist in our automation folder, create it.
        if (!automationProfileDir.exists()) {
            String sourceProfilePath = getDefaultChromeProfilePath();
            if (sourceProfilePath != null) {
                ColoredConsoleOutput.printYellowText("[GoogleSearchAPI] Automation profile not found. Creating a copy of your default profile...");
                try {
                    copyDirectory(new java.io.File(sourceProfilePath), automationProfileDir);
                    ColoredConsoleOutput.printGreenText("[GoogleSearchAPI] Automation profile created successfully at: " + automationProfileDir.getAbsolutePath());
                } catch (java.io.IOException e) {
                    ColoredConsoleOutput.printRedText("[GoogleSearchAPI] Failed to copy Chrome profile: " + e.getMessage());
                    // Don't use the copied profile if it fails
                }
            }
        }

        // Point to the parent User Data directory, not the Default profile itself.
        options.addArguments("user-data-dir=" + automationUserDataPath);
        // Explicitly tell Chrome to use the "Default" profile within that User Data directory.
        options.addArguments("--profile-directory=Default");

        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1200");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");

        return new ChromeDriver(options);
    }

    /**
     * Deletes the automation profile directory for debugging purposes.
     * This will force a fresh copy of the default profile on the next run.
     */
    public void deleteAutomationProfile() {
        String automationProfilePath = getAutomationProfilePath();
        java.io.File profileDir = new java.io.File(automationProfilePath);
        if (profileDir.exists()) {
            ColoredConsoleOutput.printYellowText("[GoogleSearchAPI] Deleting automation profile at: " + automationProfilePath);
            try {
                deleteDirectory(profileDir);
                ColoredConsoleOutput.printGreenText("[GoogleSearchAPI] Automation profile deleted successfully.");
            } catch (Exception e) {
                ColoredConsoleOutput.printRedText("[GoogleSearchAPI] Error deleting automation profile: " + e.getMessage());
            }
        }
    }

    /**
     * Recursively deletes a directory.
     * @param directory The directory to delete.
     */
    private void deleteDirectory(java.io.File directory) {
        java.io.File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (java.io.File file : allContents) {
                deleteDirectory(file);
            }
        }
        if (!directory.delete()) {
            System.err.println("Failed to delete: " + directory.getAbsolutePath());
        }
    }


    /**
     * Gets the OS-specific path to the default Chrome profile directory.
     *
     * @return The path to the default profile directory, or null if the OS is not supported.
     */
    private String getDefaultChromeProfilePath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        String separator = java.io.File.separator;

        if (os.contains("win")) {
            return userHome + separator + "AppData" + separator + "Local" + separator + "Google" + separator + "Chrome" + separator + "User Data" + separator + "Default";
        } else if (os.contains("mac")) {
            return userHome + separator + "Library" + separator + "Application Support" + separator + "Google" + separator + "Chrome" + separator + "Default";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return userHome + separator + ".config" + separator + "google-chrome" + separator + "Default";
        }
        return null;
    }

    /**
     * Gets the OS-specific path for the dedicated automation profile directory.
     * This directory is outside the standard Chrome user data location to avoid conflicts.
     *
     * @return The path to the automation profile directory, or null if the OS is not supported.
     */
    private String getAutomationProfilePath() {
        String userHome = System.getProperty("user.home");
        String separator = java.io.File.separator;
        // Use a path in the user's home directory to avoid conflicts
        return userHome + separator + "SeleniumChromeAutomationProfile";
    }

    /**
     * Recursively copies a directory using the more performant java.nio API.
     * It skips files and directories known to cause locking issues or are unnecessary (e.g., cache).
     *
     * @param sourceDir      The source directory to copy.
     * @param targetDir The destination directory.
     * @throws java.io.IOException if an I/O error occurs.
     */
    private void copyDirectory(java.io.File sourceDir, java.io.File targetDir) throws java.io.IOException {
        java.nio.file.Path sourcePath = sourceDir.toPath();
        java.nio.file.Path targetPath = targetDir.toPath();

        java.nio.file.Files.walkFileTree(sourcePath, new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
            @NotNull
            @Override
            public java.nio.file.FileVisitResult preVisitDirectory(@NotNull java.nio.file.Path dir, @NotNull java.nio.file.attribute.BasicFileAttributes attrs) throws java.io.IOException {
                String dirName = dir.getFileName().toString().toLowerCase();
                // Skip cache directories as they are large, volatile, and can cause issues.
                if (dirName.contains("cache")) {
                    return java.nio.file.FileVisitResult.SKIP_SUBTREE;
                }
                java.nio.file.Path newDir = targetPath.resolve(sourcePath.relativize(dir));
                try {
                    java.nio.file.Files.copy(dir, newDir);
                } catch (java.nio.file.FileAlreadyExistsException e) {
                    // Ignore if the directory already exists
                }
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            @NotNull
            @Override
            public java.nio.file.FileVisitResult visitFile(@NotNull java.nio.file.Path file, @NotNull java.nio.file.attribute.BasicFileAttributes attrs) {
                String fileName = file.getFileName().toString().toLowerCase();
                // Skip lock files, singleton/session state files to prevent crashes.
                if (fileName.equals("lockfile") || fileName.startsWith("singleton") || fileName.contains("session")) {
                    return java.nio.file.FileVisitResult.CONTINUE;
                }
                try {
                    java.nio.file.Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
                } catch (java.io.IOException e) {
                    // Log error but continue trying to copy other files.
                    System.err.println("Could not copy file: " + file + " - " + e.getMessage());
                }
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            @NotNull
            @Override
            public java.nio.file.FileVisitResult visitFileFailed(@NotNull java.nio.file.Path file, @NotNull java.io.IOException exc) {
                System.err.println("Failed to access file: " + file + " - " + exc.getMessage());
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }



    /**
     * Checks if a CAPTCHA challenge is present on the page.
     *
     * @param driver The WebDriver instance.
     * @return true if CAPTCHA is detected, false otherwise.
     */
    private boolean isCaptchaPresent(WebDriver driver) {
        try {
            // A short wait to see if the CAPTCHA element appears quickly.
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(CAPTCHA_IDENTIFIER),
                            ExpectedConditions.titleContains("reCAPTCHA")
                    ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Scrapes the search results from the current page.
     *
     * @param driver The WebDriver instance.
     * @return A list of GoogleSearchResultObject.
     */
    private List<GoogleSearchResultObject> scrapeResults(WebDriver driver) {
        List<GoogleSearchResultObject> results = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(SEARCH_RESULTS_CONTAINER));

        List<WebElement> resultElements = driver.findElements(INDIVIDUAL_RESULT_SELECTOR);
        for (WebElement resultElement : resultElements) {
            try {
                String title = resultElement.findElement(TITLE_SELECTOR).getText();
                String link = resultElement.findElement(LINK_SELECTOR).getAttribute("href");
                String description = "";

                try {
                    // Attempt to find the description, but don't fail if it's not there
                    description = resultElement.findElement(DESCRIPTION_SELECTOR).getText();
                } catch (NoSuchElementException e) {
                    // Description is optional, so we can ignore this.
                }


                if (link != null && !link.isEmpty() && !title.isEmpty()) {
                    results.add(new GoogleSearchResultObject(title, link, description));
                }
            } catch (NoSuchElementException e) {
                // Ignore elements that don't conform to the expected structure (e.g., "People also ask" boxes)
                ColoredConsoleOutput.printRedText("[GoogleSearchAPI] Skipping a result due to missing title or link.");
            }
        }
        return results;
    }

    /**
     * Update the automation profile by copying the latest default profile.
     * This can be used to refresh cookies and session data.
     */
    public void updateAutomationProfile() {
        String automationProfilePath = getAutomationProfilePath();
        java.io.File automationProfileDir = new java.io.File(automationProfilePath);

        // Delete existing automation profile
        if (automationProfileDir.exists()) {
            ColoredConsoleOutput.printYellowText("[GoogleSearchAPI] Deleting existing automation profile for update...");
            try {
                deleteDirectory(automationProfileDir);
                ColoredConsoleOutput.printGreenText("[GoogleSearchAPI] Existing automation profile deleted.");
            } catch (Exception e) {
                ColoredConsoleOutput.printRedText("[GoogleSearchAPI] Error deleting automation profile: " + e.getMessage());
                return;
            }
        }

        // Copy the latest default profile
        String sourceProfilePath = getDefaultChromeProfilePath();
        if (sourceProfilePath != null) {
            ColoredConsoleOutput.printYellowText("[GoogleSearchAPI] Copying latest default Chrome profile to automation profile...");
            try {
                copyDirectory(new java.io.File(sourceProfilePath), automationProfileDir);
                ColoredConsoleOutput.printGreenText("[GoogleSearchAPI] Automation profile updated successfully at: " + automationProfileDir.getAbsolutePath());
            } catch (java.io.IOException e) {
                ColoredConsoleOutput.printRedText("[GoogleSearchAPI] Failed to copy Chrome profile: " + e.getMessage());
            }
        }
    }

    /**
     * Main method for testing the GoogleSearchAPI.
     * @param args Command line arguments.
     * @throws UnsupportedEncodingException if URL encoding fails.
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        GoogleSearchAPI googleSearchAPI = new GoogleSearchAPI();

        String searchPhrase;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your search phrase: ");
        searchPhrase = scanner.nextLine();
        List<GoogleSearchResultObject> results = googleSearchAPI.search(searchPhrase);

        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            for (GoogleSearchResultObject result : results) {
                System.out.println("Title: " + result.getTitle());
                System.out.println("Link: " + result.getLink());
                System.out.println("Description: " + result.getDescription());
                System.out.println("--------------------------------------------------");
            }
        }
    }



}
