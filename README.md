# Eaze Search Engine API

Java library for Google Custom Search API integration and setup.

## Overview

This library provides a complete solution for integrating with Google's Custom Search API. It handles the automated setup process required to use Google APIs, including OAuth 2.0 authentication, Google Cloud project selection/creation, enabling the necessary APIs (Custom Search, API Keys), and generating a restricted API key.

## Features

1. **Automated Setup:** Programmatically guide users through one-time setup process
2. **Search Execution:** Simple-to-use client method for performing searches
3. **Website Downloading:** Download entire websites including assets with Selenium WebDriver
4. **Screenshot Capture:** Capture screenshots of web pages using Chrome driver
5. **Content Filtering:** Extract and filter useful content from search results

## Requirements

- Java 8 or higher
- Gradle build tool

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

```java
// Initialize website download service with Chrome binary path
WebsiteDownloadService websiteService = new WebsiteDownloadService("path/to/chrome");

// Download entire website
websiteService.downloadEntireWebsite("https://example.com", "./downloads/website");
```

### Screenshot Capture

```java
// Initialize screenshot service with Chrome binary path
ScreenshotService screenshotService = new ScreenshotService("path/to/chrome");

// Capture screenshot of web page
screenshotService.captureScreenshot("https://example.com", "./screenshots/screenshot.png");
```

## How to Obtain Search Engine ID

1. Go to the [Google Programmable Search Engine control panel](https://programmablesearchengine.google.com/)
2. Click "Add" to create a new search engine
3. In the "What to search?" section, select the option to **"Search the entire web"**
4. After creation, go to the "Setup" page for the new search engine
5. Under the "Basics" tab, find and copy the **"Search engine ID"**
6. This ID (which starts with `cx=...`) is the string that must be passed to the `CustomSearchClient` constructor

## Dependencies

- Google API Client Library for Java
- Google OAuth Client for Java 6 (and higher)
- Google OAuth Client Jetty extensions 
- Google Custom Search JSON API client
- Google Cloud Resource Manager API client
- Google Service Usage API client
- Google API Keys API client
- Gson for JSON parsing
- Jsoup HTML parser
- Selenium WebDriver with Chrome driver

## Tutorial: Complete Implementation Example

```java
// This example demonstrates how to use the complete library for search and website download

import com.eazeeditor.searchengineapi.*;
import com.eazeeditor.searchengineapi.website.WebsiteDownloadService;
import com.eazeeditor.searchengineapi.screenshot.ScreenshotService;
import com.google.api.client.auth.oauth2.Credential;

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

## Environment Variables

- `GOOGLE_CLIENT_ID`: Your OAuth 2.0 client ID
- `GOOGLE_CLIENT_SECRET`: Your OAuth 2.0 client secret
- `CHROME_BINARY_PATH`: Path to Chrome binary for screenshot and website downloading services

## Troubleshooting

### Common Issues

1. **API Not Enabled**: Make sure APIs are enabled manually in Google Cloud Console if the automated process fails
2. **Authentication Failed**: Check that client secrets file is properly formatted and accessible
3. **Invalid Project ID**: Ensure project name is unique and follows Google's naming conventions
4. **Chrome Driver Issues**: Verify Chrome binary path and version compatibility

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

## License

This project is licensed under the MIT License - see the LICENSE file for details.