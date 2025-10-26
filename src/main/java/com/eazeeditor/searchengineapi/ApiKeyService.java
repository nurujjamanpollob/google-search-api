package com.eazeeditor.searchengineapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.apikeys.v2.ApiKeysService;
import com.google.api.services.apikeys.v2.model.Operation;
import com.google.api.services.apikeys.v2.model.V2ApiTarget;
import com.google.api.services.apikeys.v2.model.V2Key;
import com.google.api.services.apikeys.v2.model.V2Restrictions;

import java.io.IOException;
import java.util.Collections;

/**
 * @author nurujjamanpollob
 * Service to create restricted API keys for Google APIs.
 */
public class ApiKeyService {

    /**
     * Creates a new restricted API key for the Custom Search API.
     *
     * @param credential Authorized Credential object.
     * @param projectId  The ID of the Google Cloud project.
     * @return The name of the operation to track the key creation.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public String createRestrictedApiKey(Credential credential, String projectId) throws IOException, InterruptedException {
        ApiKeysService service = new ApiKeysService.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Eaze Editor")
                .build();

        V2Key key = new V2Key()
                .setDisplayName("EazeEditor Search API Key")
                .setRestrictions(new V2Restrictions()
                        .setApiTargets(Collections.singletonList(new V2ApiTarget()
                                .setService("customsearch.googleapis.com"))));

        String parent = String.format("projects/%s/locations/global", projectId);

        int maxRetries = 6;
        long delay = 5000; // Start with 5 seconds

        for (int i = 0; i < maxRetries; i++) {
            try {
                System.out.println("Attempting to create API key...");
                Operation operation = service.projects().locations().keys().create(parent, key).execute();
                System.out.println("API key creation operation started: " + operation.getName());
                return operation.getName();
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 403 && e.getDetails() != null && e.getDetails().getMessage().contains("SERVICE_DISABLED")) {
                    if (i == maxRetries - 1) {
                        throw new IOException("Failed to create API key due to SERVICE_DISABLED after multiple retries.", e);
                    }
                    System.out.println("API service not ready, retrying in " + delay / 1000 + " seconds...");
                    Thread.sleep(delay);
                    delay *= 2; // Exponential backoff
                } else {
                    throw e; // Re-throw other errors
                }
            }
        }
        throw new IOException("Failed to initiate API key creation after multiple retries.");
    }
}
