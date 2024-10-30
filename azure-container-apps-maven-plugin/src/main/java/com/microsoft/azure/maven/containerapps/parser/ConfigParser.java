package com.microsoft.azure.maven.containerapps.parser;

import com.microsoft.azure.maven.containerapps.AbstractMojoBase;
import com.microsoft.azure.maven.containerapps.config.AppContainerMavenConfig;
import com.microsoft.azure.maven.containerapps.config.DeploymentType;
import com.microsoft.azure.maven.containerapps.config.IngressMavenConfig;
import com.microsoft.azure.maven.utils.MavenUtils;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.utils.Utils;
import com.microsoft.azure.toolkit.lib.containerapps.config.ContainerAppConfig;
import com.microsoft.azure.toolkit.lib.containerapps.config.ContainerAppsEnvironmentConfig;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import com.microsoft.azure.toolkit.lib.containerapps.model.IngressConfig;
import com.microsoft.azure.toolkit.lib.containerapps.model.ResourceConfiguration;
import com.microsoft.azure.toolkit.lib.containerregistry.ContainerRegistry;
import com.microsoft.azure.toolkit.lib.containerregistry.config.ContainerRegistryConfig;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigParser {

    protected AbstractMojoBase mojo;

    private final String timestamp;

    public ConfigParser(AbstractMojoBase mojo) {
        this.mojo = mojo;
        this.timestamp = Utils.getTimestamp();
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
        // todo(ruoyuwang): support other ACR_IMAGE_SUFFIX for different cloud environments
        final String defaultImageName = String.format("%s%s/%s:%s", mojo.getRegistry().getRegistryName(), ContainerRegistry.ACR_IMAGE_SUFFIX, mojo.getAppName(), timestamp);
        final String fullImageName = Optional.ofNullable(containers.get(0).getImage()).orElse(defaultImageName);
        final ContainerAppDraft.ImageConfig imageConfig = new ContainerAppDraft.ImageConfig(fullImageName);
        if (containers.get(0).getEnvironment() != null) {
            imageConfig.setEnvironmentVariables(containers.get(0).getEnvironment());
        }
        if (containers.get(0).getType() == DeploymentType.Code || containers.get(0).getType() == DeploymentType.Artifact) {
            ContainerAppDraft.BuildImageConfig buildImageConfig = new ContainerAppDraft.BuildImageConfig();
            buildImageConfig.setSource(Paths.get(containers.get(0).getDirectory()));
            //Check if we can generate dockerfile for this project. Currently only support spring boot project
            if (!imageConfig.sourceHasDockerFile()) {
                if (!MavenUtils.isSpringBootProject(mojo.getProject())) {
                    throw new AzureToolkitRuntimeException("Cannot generate Dockerfile for non-spring-boot project");
                }
            }
            //detect java version
            Map<String, String> sourceBuildEnv = new HashMap<>();
            String javaVersion = MavenUtils.getJavaVersion(mojo.getProject());
            if (StringUtils.isNotEmpty(javaVersion)) {
                sourceBuildEnv.put("JAVA_VERSION", javaVersion);
            }
            //todo(ruoyuwang): dockerfile path and some other properties into sourceBuildEnv
            buildImageConfig.setSourceBuildEnv(sourceBuildEnv);
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

        final String defaultRegistryName = String.format("acr%s", timestamp);

        config.setRegistryName(Optional.ofNullable(mojo.getRegistry().getRegistryName()).orElse(defaultRegistryName));
        mojo.getRegistry().setRegistryName(config.getRegistryName());
        config.setResourceGroup(Optional.ofNullable(mojo.getRegistry().getResourceGroup()).orElse(mojo.getResourceGroup()));
        config.setSubscriptionId(Optional.ofNullable(mojo.getRegistry().getSubscriptionId()).orElse(mojo.getSubscriptionId()));
        config.setRegion(Optional.ofNullable(mojo.getRegistry().getRegion()).orElse(mojo.getRegion()));
        return config;
    }
}
