package com.microsoft.azure.maven.containerapps.parser;

import com.microsoft.azure.maven.containerapps.AbstractMojoBase;
import com.microsoft.azure.maven.containerapps.config.AppContainerMavenConfig;
import com.microsoft.azure.maven.containerapps.config.DeploymentType;
import com.microsoft.azure.maven.containerapps.config.IngressMavenConfig;
import com.microsoft.azure.toolkit.lib.containerapps.config.ContainerAppConfig;
import com.microsoft.azure.toolkit.lib.containerapps.config.ContainerAppsEnvironmentConfig;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import com.microsoft.azure.toolkit.lib.containerapps.model.IngressConfig;
import com.microsoft.azure.toolkit.lib.containerapps.model.ResourceConfiguration;
import com.microsoft.azure.toolkit.lib.containerregistry.ContainerRegistry;
import com.microsoft.azure.toolkit.lib.containerregistry.config.ContainerRegistryConfig;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ConfigParser {

    protected AbstractMojoBase mojo;

    public ConfigParser(AbstractMojoBase mojo) {
        this.mojo = mojo;
    }

    public ContainerAppConfig getContainerAppConfig() {
        final ContainerAppConfig config = new ContainerAppConfig();
        final ContainerAppsEnvironmentConfig envConfig = new ContainerAppsEnvironmentConfig();
        envConfig.setSubscriptionId(mojo.getSubscriptionId());
        envConfig.setResourceGroup(mojo.getResourceGroup());
        envConfig.setAppEnvironmentName(mojo.getAppEnvironmentName());
        envConfig.setRegion(mojo.getRegion());
        config.setEnvironment(envConfig);
        config.setAppName(mojo.getAppName());
        config.setResourceConfiguration(getResourceConfigurationFromContainers(mojo.getContainers()));
        config.setIngressConfig(getIngressConfig(mojo.getIngress()));
        config.setRegistryConfig(getRegistryConfig());
        config.setImageConfig(getImageConfigFromContainers());
        config.setScaleConfig(mojo.getScale());
        return config;
    }


    //todo(ruoyuwang): whether we should support multi containers? CLI/Portal only support one container
    //todo(ruoyuwang): support container registry part
    public ContainerAppDraft.ImageConfig getImageConfigFromContainers() {
        List<AppContainerMavenConfig> containers = mojo.getContainers();
        if (containers == null || containers.isEmpty()) {
            return null;
        }

        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        // Define the format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        // Format the current time
        String formattedTimestamp = now.format(formatter);
        final String defaultImageName = String.format("%s%s/%s:%s", mojo.getRegistry().getRegistryName(), ContainerRegistry.ACR_IMAGE_SUFFIX, mojo.getAppName(), formattedTimestamp);
        final String fullImageName = Optional.ofNullable(containers.get(0).getImage()).orElse(defaultImageName);
        final ContainerAppDraft.ImageConfig imageConfig = new ContainerAppDraft.ImageConfig(fullImageName);
        if (containers.get(0).getEnvironment() != null) {
            imageConfig.setEnvironmentVariables(containers.get(0).getEnvironment());
        }
        if (containers.get(0).getType() == DeploymentType.Code || containers.get(0).getType() == DeploymentType.Artifact) {
            ContainerAppDraft.BuildImageConfig buildImageConfig = new ContainerAppDraft.BuildImageConfig();
            buildImageConfig.setSource(Paths.get(containers.get(0).getDirectory()));
            imageConfig.setBuildImageConfig(buildImageConfig);
        }
        return imageConfig;
    }

    public ResourceConfiguration getResourceConfigurationFromContainers(List<AppContainerMavenConfig> containers) {
        if (containers == null || containers.isEmpty()) {
            return null;
        }
        //todo(ruoyuwang): check the default cpu and memory logic here
        if (containers.get(0).getCpu() == null && containers.get(0).getMemory() == null) {
            return null;
        }
        final ResourceConfiguration resourceConfiguration = new ResourceConfiguration();
        resourceConfiguration.setCpu(containers.get(0).getCpu());
        resourceConfiguration.setMemory(containers.get(0).getMemory());
        return resourceConfiguration;
    }

    public IngressConfig getIngressConfig(IngressMavenConfig ingressMavenConfig) {
        if (ingressMavenConfig == null) {
            return null;
        }
        IngressConfig ingressConfig = new IngressConfig();
        ingressConfig.setEnableIngress(true);
        ingressConfig.setExternal(ingressMavenConfig.getExternal());
        ingressConfig.setTargetPort(ingressMavenConfig.getTargetPort());
        return ingressConfig;
    }

    public ContainerRegistryConfig getRegistryConfig() {
        if (mojo.getRegistry() == null) {
            return null;
        }
        ContainerRegistryConfig config = new ContainerRegistryConfig();

        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        // Define the format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // Format the current time
        String formattedTimestamp = now.format(formatter);
        final String defaultRegistryName = String.format("acr%s", formattedTimestamp);

        config.setRegistryName(Optional.ofNullable(mojo.getRegistry().getRegistryName()).orElse(defaultRegistryName));
        mojo.getRegistry().setRegistryName(config.getRegistryName());
        config.setResourceGroup(Optional.ofNullable(mojo.getRegistry().getResourceGroup()).orElse(mojo.getResourceGroup()));
        config.setSubscriptionId(Optional.ofNullable(mojo.getRegistry().getSubscriptionId()).orElse(mojo.getSubscriptionId()));
        config.setRegion(Optional.ofNullable(mojo.getRegistry().getRegion()).orElse(mojo.getRegion()));
        return config;
    }
}
