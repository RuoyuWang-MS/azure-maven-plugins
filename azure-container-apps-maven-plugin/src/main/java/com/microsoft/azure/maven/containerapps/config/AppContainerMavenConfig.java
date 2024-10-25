/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.maven.containerapps.config;

import com.azure.resourcemanager.appcontainers.models.EnvironmentVar;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.List;

@Getter
@Setter
public class AppContainerMavenConfig {

    @Nullable
    private Double cpu;
    @Nullable
    private String memory;
    private DeploymentType type = DeploymentType.Image;
    @Nullable
    private String image;
    @Nullable
    private List<EnvironmentVar> environment;
    @Nullable
    private String directory;
}
