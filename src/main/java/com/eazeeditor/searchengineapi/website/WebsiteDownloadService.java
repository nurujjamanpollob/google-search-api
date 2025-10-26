package com.eazeeditor.searchengineapi.website;

import com.google.api.services.customsearch.v1.model.Result;
import com.google.api.services.customsearch.v1.model.Search;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to download an entire website including its assets (CSS, JS, images).
 * Note: This implementation uses Selenium WebDriver with Chrome in headless mode to render dynamic content,
 * and tested working with chrome v142 only, may not work with other versions.
 * <p>
 * Some protected resources or dynamically loaded content may not be downloaded correctly due to website restrictions or complex loading mechanisms.
 * @author nurujjamanpollob
 * @version 1.0
 */
public class WebsiteDownloadService {

    private final String chromeBinary;

    public WebsiteDownloadService() throws IllegalStateException {
        String chromeBinaryPath = System.getenv("CHROME_BINARY_PATH");
        if (chromeBinaryPath == null || chromeBinaryPath.isEmpty()) {
            throw new IllegalStateException("CHROME_BINARY_PATH environment variable is not set. use export CHROME_BINARY_PATH=/path/to/chrome or initialize this class with WebsiteDownloadService(String chromeBinaryPath)");
        }
        this.chromeBinary = chromeBinaryPath;
    }

    public WebsiteDownloadService(String chromeBinaryPath) {
        if (chromeBinaryPath == null || chromeBinaryPath.isEmpty() || !new File(chromeBinaryPath).exists()) {
            throw new IllegalArgumentException("chromeBinaryPath cannot be null, empty, or invalid");
        }
        this.chromeBinary = chromeBinaryPath;
    }

    public void downloadEntireWebsite(String url, String localPath) {
        WebDriver driver = null;
        try {
            Path localDirPath = Paths.get(localPath);
            Files.createDirectories(localDirPath);
            URL baseUrl = new URL(url);

            ChromeOptions options = new ChromeOptions();
            options.setBinary(this.chromeBinary);
            options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");

            driver = new ChromeDriver(options);
            driver.get(url);

            // Optional: wait for dynamic content to load
            Thread.sleep(5000);

            String pageSource = driver.getPageSource();

            if (pageSource == null) {
                System.err.println("Failed to retrieve page source for URL: " + url);
                return;
            }
            Document doc = Jsoup.parse(pageSource, url);

            // Select all elements with href or src attributes
            Elements elements = doc.select("[href], [src]");

            for (Element element : elements) {
                String attr = element.hasAttr("href") ? "href" : "src";
                String originalUrl = element.attr(attr);

                if (shouldDownload(element.tagName(), attr, originalUrl)) {
                    String localAssetPath = downloadResource(baseUrl, originalUrl, localPath);
                    if (localAssetPath != null) {
                        // Adjust path for HTML by removing potential leading slashes for correct relative linking
                        String relativePathForHtml = localAssetPath.startsWith("/") ? localAssetPath.substring(1) : localAssetPath;
                        element.attr(attr, relativePathForHtml);
                    }
                }
            }

            // Save the modified HTML file
            String fileName = getFileNameFromUrl(baseUrl);
            if (!fileName.contains(".htm")) {
                fileName = "index.html";
            }
            Path htmlPath = localDirPath.resolve(fileName);
            Files.write(htmlPath, doc.outerHtml().getBytes());

            System.out.println("Website downloaded successfully to " + localPath);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }


    private boolean shouldDownload(String tagName, String attr, String url) {
        if (url.isEmpty() || url.startsWith("data:") || url.startsWith("#")) {
            return false;
        }
        return (tagName.equals("link") && attr.equals("href")) ||
                (tagName.equals("script") && attr.equals("src")) ||
                (tagName.equals("img") && attr.equals("src"));
    }

    private String downloadResource(URL baseUrl, String resourceUrl, String localBasePath) {
        try {
            URL absoluteUrl = new URL(baseUrl, resourceUrl);
            String host = absoluteUrl.getHost();
            String path = absoluteUrl.getPath();

            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (path.isEmpty()) {
                return null;
            }

            String relativePath = Paths.get(host, path.startsWith("/") ? path.substring(1) : path).toString();
            Path localFilePath = Paths.get(localBasePath, relativePath);

            Files.createDirectories(localFilePath.getParent());

            Connection.Response response = Jsoup.connect(absoluteUrl.toExternalForm()).ignoreContentType(true).execute();
            String contentType = response.contentType();

            // Check if the resource is a CSS file to parse its contents for more resources
            if (contentType != null && contentType.contains("text/css")) {
                String cssContent = response.body();
                String modifiedCss = parseAndDownloadCssResources(cssContent, absoluteUrl, localBasePath);
                Files.write(localFilePath, modifiedCss.getBytes(StandardCharsets.UTF_8));
            } else if (contentType != null && (contentType.contains("javascript") || contentType.contains("ecmascript"))) {
                String jsContent = response.body();
                String modifiedJs = parseAndDownloadJsResources(jsContent, absoluteUrl, localBasePath);
                Files.write(localFilePath, modifiedJs.getBytes(StandardCharsets.UTF_8));
            } else {
                try (InputStream in = response.bodyStream()) {
                    Files.copy(in, localFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            return relativePath.replace('\\', '/');
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL for resource: " + resourceUrl + " - " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("Could not download resource: " + resourceUrl + " - " + e.getMessage());
            return null;
        }
    }

    private String parseAndDownloadJsResources(String jsContent, URL jsBaseUrl, String localBasePath) {
        // This regex is a simple heuristic to find asset paths in string literals.
        // It may not catch dynamically generated paths.
        Pattern pattern = Pattern.compile("(['\"])([^'\"]*?\\.(?:png|jpe?g|gif|svg|webp|woff2?|ttf|eot))\\1");
        Matcher matcher = pattern.matcher(jsContent);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String originalUrl = matcher.group(2);
            if (originalUrl.isEmpty() || originalUrl.startsWith("data:") || originalUrl.startsWith("http")) {
                continue;
            }

            try {
                String newRelativePath = downloadResource(jsBaseUrl, originalUrl, localBasePath);
                if (newRelativePath != null) {
                    Path jsSavedPath = Paths.get(localBasePath, jsBaseUrl.getHost(), jsBaseUrl.getPath().substring(1));
                    Path resourceSavedPath = Paths.get(localBasePath, newRelativePath);
                    String finalRelativePath = jsSavedPath.getParent().relativize(resourceSavedPath).toString().replace('\\', '/');

                    // Replace the original URL with the new relative path, preserving original quotes
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + finalRelativePath + matcher.group(1)));
                }
            } catch (Exception e) {
                System.err.println("Failed to process JS resource " + originalUrl + ": " + e.getMessage());
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    private String parseAndDownloadCssResources(String cssContent, URL cssBaseUrl, String localBasePath) {
        // Pattern to find url(...) declarations in CSS
        Pattern pattern = Pattern.compile("url\\((['\"]?)(.*?)\\1\\)");
        Matcher matcher = pattern.matcher(cssContent);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String originalUrl = matcher.group(2);
            if (originalUrl.isEmpty() || originalUrl.startsWith("data:")) {
                continue;
            }

            try {
                // Download the resource found inside url()
                String newRelativePath = downloadResource(cssBaseUrl, originalUrl, localBasePath);
                if (newRelativePath != null) {
                    // Calculate the path of the downloaded resource relative to the CSS file's location
                    Path cssSavedPath = Paths.get(localBasePath, cssBaseUrl.getHost(), cssBaseUrl.getPath().substring(1));
                    Path resourceSavedPath = Paths.get(localBasePath, newRelativePath);
                    String finalRelativePath = cssSavedPath.getParent().relativize(resourceSavedPath).toString().replace('\\', '/');

                    // Replace the original URL with the new relative path
                    matcher.appendReplacement(sb, "url('" + Matcher.quoteReplacement(finalRelativePath) + "')");
                }
            } catch (Exception e) {
                System.err.println("Failed to process CSS resource " + originalUrl + ": " + e.getMessage());
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    private String getFileNameFromUrl(URL url) {
        String path = url.getPath();
        if (path.isEmpty() || path.endsWith("/")) {
            return "";
        }
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public List<String> extractLinksFromSearchResults(Search searchResult) {
        List<String> links = new ArrayList<>();
        if (searchResult != null && searchResult.getItems() != null) {
            for (Result item : searchResult.getItems()) {
                if (item.getLink() != null) {
                    links.add(item.getLink());
                }
            }
        }
        return links;
    }

    public List<String> extractUsefulContentFromSearchResults(Search searchResult) {
        List<String> contentList = new ArrayList<>();
        if (searchResult != null && searchResult.getItems() != null) {
            for (Result item : searchResult.getItems()) {
                String link = item.getLink();
                if (link != null) {
                    try {
                        Document doc = Jsoup.connect(link).get();
                        // A more refined selector for main content
                        doc.select("nav, footer, header, aside, script, style, .ad, .ads, #ad, #ads").remove();
                        contentList.add(doc.body().text());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return contentList;
    }
}
