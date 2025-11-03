
# Eaze Search Engine API

A powerful Java library for seamless integration with the Google Custom Search API, featuring robust tools for website downloading and screenshot capture.

## Overview

This library provides a complete solution for integrating with Google's Custom Search API (Programmable Search Engine). It automates the setup process, including OAuth 2.0 authentication and API key generation.

Beyond search, it offers powerful utilities built on Selenium to **download entire websites** for offline analysis and **capture high-quality screenshots** of any web page.

## Features

1.  **Powerful Website Screenshot Tool:** Capture full-page or specific-area screenshots of any website using Selenium and Chrome.
2.  **Complete Website Downloader:** Download entire websites, including all assets (CSS, JS, images), for offline viewing or analysis.
3.  **Automated Google API Setup:** Programmatically handles OAuth 2.0, project creation, and API key generation.
4.  **Seamless Search Integration:** A simple client to execute searches against Google's Programmable Search Engine.
5.  **Smart Content Extraction:** Utilities to parse and extract meaningful content from search results using Jsoup.
6.  **Website Downloading and Screenshot Features:** Built-in support for downloading websites and capturing screenshots using Selenium WebDriver with Chrome.

## Requirements

-   Java 8 or higher
-   Gradle build tool
-   Google Chrome browser. The screenshot and download features have been tested with **Chrome V124**.

## Installation

Add this dependency to your `build.gradle` file:

```gradle
dependencies {
    implementation 'com.eazeeditor:searchengineapi:1.0.0'
}
```

## Usage

### Setup Process

```java
// Initialize the library components
GoogleAuthService authService = new GoogleAuthService();
GoogleProjectService projectService = new GoogleProjectService();
GoogleApiService apiService = new GoogleApiService();
ApiKeyService apiKeyService = new ApiKeyService();

// Authenticate user with client secrets file
Credential credential = authService.authenticateUser("path/to/client_secret.json");

// Select or create project
String projectId = projectService.selectOrCreateProject(credential, "MyProjectName");

// Enable required APIs
apiService.enableApis(credential, projectId);

// Create restricted API key
String apiKey = apiKeyService.createRestrictedApiKey(credential, projectId);
```

### Search Execution

```java
// Initialize search client with API key and search engine ID
CustomSearchClient searchClient = new CustomSearchClient(apiKey, "your_search_engine_id");

// Perform search
Search searchResults = searchClient.executeSearch("query");

// Extract links from results
List<String> links = searchClient.extractLinksFromSearchResults(searchResults);

// Extract useful content from results
List<String> content = searchClient.extractUsefulContentFromSearchResults(searchResults);
```

### Website Downloading

Effortlessly download a complete, browsable copy of any website. This tool is ideal for creating offline archives or for local data analysis. It recursively downloads HTML, CSS, JavaScript, and images.

**Note:** This feature has been tested with **Chrome V124**.

```java
// Initialize website download service with Chrome binary path
WebsiteDownloadService websiteService = new WebsiteDownloadService("path/to/chrome");

// Download entire website to a local directory
websiteService.downloadEntireWebsite("https://example.com", "./downloads/website");
```

### Screenshot Capture

Capture pixel-perfect screenshots of any URL. This is perfect for visual regression testing, content verification, or generating thumbnails. The service supports both standard and full-page (scrolling) screenshots.

**Note:** This feature has been tested with **Chrome V124**.

```java
// Initialize screenshot service with Chrome binary path
ScreenshotService screenshotService = new ScreenshotService("path/to/chrome");

// Capture a standard screenshot of the visible part of the web page
screenshotService.captureScreenshot("https://example.com", "./screenshots/screenshot.png");

// Capture a long, full-page screenshot
screenshotService.captureLongScreenshot("https://example.com", "./screenshots/full_page_screenshot.png");
```

### Direct Google Search Scraping (Selenium-based)

For scenarios requiring direct scraping of Google's search results page, the library provides the `GoogleSearchAPI`. This tool uses Selenium WebDriver to automate Google Chrome, providing a robust way to get organic search results without an API key.

**Robust CAPTCHA Handling:** The `GoogleSearchAPI` is designed to handle Google's CAPTCHA challenges gracefully. It initially runs the browser in headless mode for efficiency. If a CAPTCHA is detected, it automatically relaunches in a visible (non-headless) browser window, allowing you to solve the puzzle manually. The program will wait for you to complete the CAPTCHA before proceeding to scrape the results.

**Isolated Browser Profile:** To avoid conflicts with your personal browsing data, the API creates and uses a separate, isolated Chrome profile for all automation tasks. This ensures that your bookmarks, history, and sessions are not affected.

#### GoogleSearchAPI Class

This class provides the core functionality for performing searches and scraping results.

```java
// Initialize the API. You can provide a path to your ChromeDriver.
// If no path is provided, it will use the one in the system's PATH.
GoogleSearchAPI googleSearchAPI = new GoogleSearchAPI();

// Perform a search
List<GoogleSearchResultObject> results = googleSearchAPI.search("your search query");

// Process the results
for (GoogleSearchResultObject result : results) {
    System.out.println("Title: " + result.getTitle());
    System.out.println("Link: " + result.getLink());
    System.out.println("Description: " + result.getDescription());
    System.out.println("--------------------");
}
```

#### GoogleSearchResultObject Class

This is a simple data object that holds the information for a single search result scraped from Google.

```java
public class GoogleSearchResultObject {
    public String getTitle();
    public String getLink();
    public String getDescription();
}
```

## How to Obtain Search Engine ID

1.  Go to the [Google Programmable Search Engine control panel](https://programmablesearchengine.google.com/)
2.  Click "Add" to create a new search engine
3.  In the "What to search?" section, select the option to **"Search the entire web"**
4.  After creation, go to the "Setup" page for the new search engine
5.  Under the "Basics" tab, find and copy the **"Search engine ID"**
6.  This ID is the string that must be passed to the `CustomSearchClient` constructor

## Dependencies

-   Google API Client Library for Java
-   Google OAuth Client for Java 6 (and higher)
-   Google OAuth Client Jetty extensions
-   Google Custom Search JSON API client
-   Google Cloud Resource Manager API client
-   Google Service Usage API client
-   Google API Keys API client
-   Gson for JSON parsing
-   Jsoup HTML parser
-   Selenium WebDriver with Chrome driver

## Tutorial: Complete Implementation Example

```java
// This example demonstrates how to use the complete library for search and website download

import com.eazeeditor.searchengineapi.*;
import com.eazeeditor.searchengineapi.website.WebsiteDownloadService;
import com.eazeeditor.searchengineapi.screenshot.ScreenshotService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.customsearch.v1.model.Search;
import java.util.List;

public class Example {
    public static void main(String[] args) throws Exception {
        // Step 1: Setup authentication
        GoogleAuthService authService = new GoogleAuthService();
        Credential credential = authService.authenticateUser("path/to/client_secret.json");

        // Step 2: Create or select project
        GoogleProjectService projectService = new GoogleProjectService();
        String projectId = projectService.selectOrCreateProject(credential, "MyProjectName");

        // Step 3: Enable required APIs
        GoogleApiService apiService = new GoogleApiService();
        apiService.enableApis(credential, projectId);

        // Step 4: Create restricted API key
        ApiKeyService apiKeyService = new ApiKeyService();
        String apiKey = apiKeyService.createRestrictedApiKey(credential, projectId);

        // Step 5: Use search client
        CustomSearchClient searchClient = new CustomSearchClient(apiKey, "your_search_engine_id");
        Search searchResults = searchClient.executeSearch("query");

        // Extract content from search results
        List<String> content = searchClient.extractUsefulContentFromSearchResults(searchResults);
        System.out.println("Extracted Content: " + content);

        // Step 6: Download website (optional)
        WebsiteDownloadService websiteService = new WebsiteDownloadService("path/to/chrome");
        websiteService.downloadEntireWebsite("https://example.com", "./downloads/website");

        // Step 7: Capture screenshot (optional)
        ScreenshotService screenshotService = new ScreenshotService("path/to/chrome");
        screenshotService.captureScreenshot("https://example.com", "./screenshots/screenshot.png");

        System.out.println("All operations completed successfully!");
    }
}
```

## API Documentation

### GoogleAuthService Class

```java
public class GoogleAuthService {
    public Credential authenticateUser(String clientSecretsJsonPath) throws IOException;
    public Credential authenticateUser(String clientId, String clientSecret) throws IOException;
    public Credential authenticateFromEnvVars() throws IOException;
}
```

### GoogleProjectService Class

```java
public class GoogleProjectService {
    public String selectOrCreateProject(Credential credential, String desiredProjectName) throws IOException, InterruptedException;
}
```

### GoogleApiService Class

```java
public class GoogleApiService {
    public void enableApis(Credential credential, String projectId) throws IOException, InterruptedException;
}
```

### ApiKeyService Class

```java
public class ApiKeyService {
    public String createRestrictedApiKey(Credential credential, String projectId) throws IOException, InterruptedException;
}
```

### CustomSearchClient Class

```java
public class CustomSearchClient {
    public Search executeSearch(String query);
    public List<String> extractLinksFromSearchResults(Search searchResult);
    public List<String> extractUsefulContentFromSearchResults(Search searchResult);
    public List<String> extractFilteredContentFromSearchResults(Search searchResult);
}
```

### WebsiteDownloadService Class

```java
public class WebsiteDownloadService {
    public WebsiteDownloadService(String chromeBinaryPath);
    public void downloadEntireWebsite(String url, String localPath);
}
```

### ScreenshotService Class

```java
public class ScreenshotService {
    public ScreenshotService(String chromeBinaryPath);
    public void captureScreenshot(String url, String localPath);
    public void captureLongScreenshot(String url, String localPath);
}
```

### GoogleSearchAPI Class

```java
public class GoogleSearchAPI {
    public GoogleSearchAPI();
    public GoogleSearchAPI(String driverPath);
    public List<GoogleSearchResultObject> search(String query) throws UnsupportedEncodingException;
}
```

### GoogleSearchResultObject Class

```java
public class GoogleSearchResultObject {
    public String getTitle();
    public String getLink();
    public String getDescription();
}
```

## Environment Variables

-   `GOOGLE_CLIENT_ID`: Your OAuth 2.0 client ID
-   `GOOGLE_CLIENT_SECRET`: Your OAuth 2.0 client secret
-   `CHROME_BINARY_PATH`: Path to Chrome binary for screenshot and website downloading services

## Troubleshooting

### Common Issues

1.  **API Not Enabled**: Make sure APIs are enabled manually in Google Cloud Console if the automated process fails.
2.  **Authentication Failed**: Check that client secrets file is properly formatted and accessible.
3.  **Invalid Project ID**: Ensure project name is unique and follows Google's naming conventions.
4.  **Chrome Driver Issues**: Verify the `CHROME_BINARY_PATH` environment variable is set correctly. Ensure your installed Chrome version (e.g., V124) is compatible with the Selenium WebDriver used by the library.

### Error Handling

```java
try {
    // Handle errors appropriately
    Credential credential = authService.authenticateUser("path/to/client_secret.json");
} catch (IOException e) {
    System.err.println("Authentication failed: " + e.getMessage());
} catch (IllegalStateException e) {
    System.err.println("Environment variables not set: " + e.getMessage());
}
```
### Credits

Developed by the Eaze Editor Team, this library is generated using advanced AI tools to streamline Google Custom Search API integration and enhance web scraping capabilities.