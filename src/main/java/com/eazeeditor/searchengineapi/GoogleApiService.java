package com.eazeeditor.searchengineapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.serviceusage.v1.ServiceUsage;
import com.google.api.services.serviceusage.v1.model.Operation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author nurujjamanpollob
 * Service to enable required Google APIs for a project.
 */
public class GoogleApiService {

    /**
     * Enables the required APIs for the Custom Search and API Keys services.
     * Note: This method will wait for each API enabling operation to complete before returning,
     * apis may not be immediately available after this method returns.
     * This method absolutely doesn't guarantee the APIs will be enabled, if there are any errors during the enabling process, an IOException will be thrown,
     * and If not work, enable the APIs manually from Google Cloud Console.
     * @param credential Authorized Credential object.
     * @param projectId  The ID of the Google Cloud project.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public void enableApis(Credential credential, String projectId) throws IOException, InterruptedException {
        ServiceUsage service = new ServiceUsage.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("EazeEditor")
                .build();

        List<String> servicesToEnable = Arrays.asList("customsearch.googleapis.com", "apikeys.googleapis.com");

        for (String serviceToEnable : servicesToEnable) {
            System.out.println("Enabling API: " + serviceToEnable + "...");
            Operation operation = service.services().enable(String.format("projects/%s/services/%s", projectId, serviceToEnable), null).execute();

            System.out.println("Waiting for operation " + operation.getName() + " to complete...");

            while (operation.getDone() == null || !operation.getDone()) {
                // Poll for the operation status every 5 seconds.
                Thread.sleep(5000);
                operation = service.operations().get(operation.getName()).execute();
            }

            if (operation.getError() != null) {
                System.err.println("Error enabling API " + serviceToEnable + ": " + operation.getError().getMessage());
                // Handle the error appropriately, maybe by throwing an exception.
                throw new IOException("Error enabling API " + serviceToEnable + ": " + operation.getError().getMessage());
            } else {
                System.out.println("API " + serviceToEnable + " enabled successfully.");
            }
        }

        // Add a delay to allow for API propagation.
        System.out.println("Waiting for 60 seconds for APIs to propagate before use...");
        Thread.sleep(60000);
    }
}
