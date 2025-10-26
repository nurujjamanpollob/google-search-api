package com.eazeeditor.searchengineapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.apikeys.v2.ApiKeysService;
import com.google.api.services.apikeys.v2.model.Operation;
import com.google.api.services.customsearch.v1.model.Result;
import com.google.api.services.customsearch.v1.model.Search;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author nurujjamanpollob
 * Test class to demonstrate the end-to-end functionality of the Eaze Search Engine API library.
 */
public class SearchEngineAPITest {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Eaze Search Engine API library initialized successfully");

            // 1. Get user inputs
            System.out.print("Enter the path to your client_secrets.json file: ");
            String clientSecretsJsonPath = scanner.nextLine();

            System.out.print("Enter your desired Google Cloud project name: ");
            String desiredProjectName = scanner.nextLine();

            System.out.print("Enter your Custom Search Engine ID (cx): ");
            String searchEngineId = scanner.nextLine();

            // 2. Authorize user
            System.out.println("\nAttempting to authorize user...");
            GoogleAuthService authService = new GoogleAuthService();
            Credential credential = authService.authenticateUser(clientSecretsJsonPath);
            System.out.println("User authorized successfully.");

            // 3. Select or create project
            System.out.println("Checking for or creating project '" + desiredProjectName + "'...");
            GoogleProjectService projectService = new GoogleProjectService();
            String projectId = projectService.selectOrCreateProject(credential, desiredProjectName);
            System.out.println("Using project with ID: " + projectId);

            // 4. Enable necessary APIs
            System.out.println("Enabling required APIs (Custom Search API, API Keys API)...");
            GoogleApiService apiService = new GoogleApiService();
            apiService.enableApis(credential, projectId);
            // The enableApis method now includes a wait, so the extra sleep here is removed.

            // 5. Create a restricted API key
            System.out.println("Creating a new restricted API key...");
            ApiKeyService apiKeyService = new ApiKeyService();
            String operationName = apiKeyService.createRestrictedApiKey(credential, projectId);
            System.out.println("API key creation started. Operation: " + operationName);

            // Poll the operation to get the created key
            String apiKey = pollForKey(credential, operationName);
            System.out.println("Successfully created API Key: " + apiKey);


            // 6. Perform a search
            System.out.println("\nPerforming a test search...");
            CustomSearchClient searchClient = new CustomSearchClient(apiKey, searchEngineId);
            Search results = searchClient.executeSearch("Google Custom Search API");

            if (results.getItems() != null && !results.getItems().isEmpty()) {
                System.out.println("Search successful! Found " + results.getItems().size() + " results.");
                for (Result item : results.getItems()) {
                    System.out.println("  - " + item.getTitle() + ": " + item.getLink());
                }
            } else {
                System.out.println("Search executed, but no results were found.");
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Polls the operation until the API key is created and returns the key string.
     * @param credential the authorized Credential object
     * @param operationName the name of the operation to poll
     * @return the created API key string
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the thread is interrupted while waiting
     */
    private static String pollForKey(Credential credential, String operationName) throws IOException, InterruptedException {
        ApiKeysService apiKeysService = new ApiKeysService.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                credential)
                .setApplicationName("Eaze Editor")
                .build();

        System.out.println("Polling for API key creation status...");
        for (int i = 0; i < 30; i++) { // Poll for up to 5 minutes (30 * 10s)
            Operation op = apiKeysService.operations().get(operationName).execute();
            if (op.getDone() != null && op.getDone()) {
                if (op.getResponse() != null) {
                    // The response is a map, we need to extract the keyString
                    java.util.Map<String, Object> responseMap = (java.util.Map<String, Object>) op.getResponse();
                    return (String) responseMap.get("keyString");
                } else if (op.getError() != null) {
                    throw new IOException("Error creating API key: " + op.getError().getMessage());
                }
                break; // Should not be reached if response or error is present
            }
            System.out.println("Waiting for key creation to complete... (10s)");
            TimeUnit.SECONDS.sleep(10);
        }
        throw new IOException("API key creation timed out.");
    }

}
