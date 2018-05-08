/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.AzureWebAppExtension;
import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.gradle.webapp.configuration.ContainerSettings;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import org.gradle.api.GradleException;

public class PrivateDockerHubRuntimeHandlerImpl implements RuntimeHandler {
    private static final String SERVER_ID_NOT_FOUND = "Server Id not found in gradle.properties. ServerId=";
    private static final String CREDENTIALS_NOT_FOUND = "Container registry credentials not found in containerSettings.";

    private DeployTask task;
    private AzureWebAppExtension extension;

    public PrivateDockerHubRuntimeHandlerImpl(final DeployTask task) {
        this.task = task;
        this.extension = task.getAzureWebAppExtension();
    }

    @Override
    public WithCreate defineAppWithRuntime() throws Exception {
        final ContainerSettings containerSetting = extension.getContainerSettings();
        if (containerSetting.getServerId() == null) {
            throw new GradleException(SERVER_ID_NOT_FOUND + containerSetting.getServerId());
        }
        if (containerSetting.getUsername() == null || containerSetting.getPassword() == null) {
            throw new GradleException(CREDENTIALS_NOT_FOUND);
        }
        return WebAppUtils.defineApp(task)
                .withNewLinuxPlan(extension.getPricingTier())
                .withPrivateDockerHubImage(containerSetting.getImageName())
                .withCredentials(containerSetting.getUsername(), containerSetting.getPassword());
    }

    @Override
    public WebApp.Update updateAppRuntime(final WebApp app) throws Exception {
        WebAppUtils.assureLinuxWebApp(app);
        final ContainerSettings containerSetting = extension.getContainerSettings();
        if (containerSetting == null) {
            throw new GradleException(SERVER_ID_NOT_FOUND + containerSetting.getServerId());
        }
        if (containerSetting.getUsername() == null || containerSetting.getPassword() == null) {
            throw new GradleException(CREDENTIALS_NOT_FOUND);
        }
        return app.update()
                .withPrivateDockerHubImage(containerSetting.getImageName())
                .withCredentials(containerSetting.getUsername(), containerSetting.getPassword());
    }
}
