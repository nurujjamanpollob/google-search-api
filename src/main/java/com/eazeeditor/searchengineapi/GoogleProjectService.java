package com.eazeeditor.searchengineapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.ListProjectsResponse;
import com.google.api.services.cloudresourcemanager.model.Operation;
import com.google.api.services.cloudresourcemanager.model.Project;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author nurujjamanpollob
 * Service to handle Google Cloud Project selection or creation.
 */
public class GoogleProjectService {

    public String selectOrCreateProject(Credential credential, String desiredProjectName) throws IOException, InterruptedException {
        CloudResourceManager service = new CloudResourceManager.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("EazeEditor")
                .build();

        // List projects
        ListProjectsResponse response = service.projects().list().execute();
        List<Project> projects = response.getProjects();

        // Check if project exists
        if (projects != null) {
            for (Project project : projects) {
                if (desiredProjectName.equals(project.getName()) && "ACTIVE".equals(project.getLifecycleState())) {
                    System.out.println("Found existing active project: " + project.getProjectId());
                    return project.getProjectId();
                }
            }
        }

        // Create new project
        String projectId = desiredProjectName.toLowerCase().replaceAll("[^a-z0-9-]", "") + "-" + System.currentTimeMillis();
        Project newProject = new Project()
                .setName(desiredProjectName)
                .setProjectId(projectId);

        Operation operation = service.projects().create(newProject).execute();
        System.out.println("Project creation started. Operation: " + operation.getName());

        // Poll for project creation completion
        System.out.println("Waiting for project creation to complete...");
        while (true) {
            Operation op = service.operations().get(operation.getName()).execute();
            if (op.getDone() != null && op.getDone()) {
                if (op.getError() != null) {
                    throw new IOException("Error creating project: " + op.getError().getMessage());
                }
                System.out.println("Project created successfully.");
                break;
            }
            TimeUnit.SECONDS.sleep(5);
        }

        return newProject.getProjectId();
    }
}
