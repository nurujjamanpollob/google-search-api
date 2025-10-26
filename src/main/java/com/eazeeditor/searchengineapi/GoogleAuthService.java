package com.eazeeditor.searchengineapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author nurujjamanpollob
 * Service to handle Google OAuth2 authentication. When you create a oauth2 client ID in Google Cloud Console,
 * add <b><a href="http://localhost/Callback">http://localhost/Callback</a></b> in the Authorized redirect URIs, to make the API work properly.
 */
public class GoogleAuthService {

    public Credential authenticateUser(String clientSecretsJsonPath) throws IOException {
        // Load the client secrets file
        InputStream in = Files.newInputStream(Paths.get(clientSecretsJsonPath));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(GsonFactory.getDefaultInstance(), new InputStreamReader(in));

        // Create authorization flow
        AuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                List.of("https://www.googleapis.com/auth/cloud-platform"))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("./credentials")))
                .setAccessType("offline")
                .build();

        // Create the authorization code installed app
        AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow,
                new LocalServerReceiver());

        // Perform authentication
        return app.authorize("user");
    }

    /**
     * Authenticates the user using the provided client ID and client secret.
     * @param clientId The OAuth 2.0 client ID.
     * @param clientSecret The OAuth 2.0 client secret.
     * @return An authorized Credential object.
     * @throws IOException If an I/O error occurs.
     */
    public Credential authenticateUser(String clientId, String clientSecret) throws IOException {
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        details.setAuthUri("https://accounts.google.com/o/oauth2/auth");
        details.setTokenUri("https://oauth2.googleapis.com/token");

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setInstalled(details);

        AuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                List.of("https://www.googleapis.com/auth/cloud-platform"))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("./credentials")))
                .setAccessType("offline")
                .build();

        AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver());

        return app.authorize("user");
    }

    /**
     * Authenticates the user using client ID and secret from environment variables.
     * It expects GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET to be set.
     * @return An authorized Credential object.
     * @throws IOException If an I/O error occurs.
     * @throws IllegalStateException If environment variables are not set.
     */
    public Credential authenticateFromEnvVars() throws IOException {
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");

        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            throw new IllegalStateException("'GOOGLE_CLIENT_ID' and 'GOOGLE_CLIENT_SECRET' environment variables must be set.");
        }

        return authenticateUser(clientId, clientSecret);
    }




}
