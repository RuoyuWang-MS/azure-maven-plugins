package com.microsoft.azure.maven.containerapps.parser;

import com.microsoft.azure.maven.containerapps.AbstractMojoBase;
import com.microsoft.azure.maven.containerapps.config.AppContainerMavenConfig;
import com.microsoft.azure.maven.containerapps.config.IngressMavenConfig;
import com.microsoft.azure.toolkit.lib.containerapps.config.ContainerAppConfig;
import com.microsoft.azure.toolkit.lib.containerapps.config.ContainerAppsEnvironmentConfig;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import com.microsoft.azure.toolkit.lib.containerapps.model.IngressConfig;
import com.microsoft.azure.toolkit.lib.containerapps.model.ResourceConfiguration;

import java.util.List;

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
        config.setImageConfig(getImageConfigFromContainers(mojo.getContainers()));
        config.setScaleConfig(mojo.getScale());
        return config;
    }


    //todo(ruoyuwang): whether we should support multi containers? CLI/Portal only support one container
    //todo(ruoyuwang): support container registry part
    public ContainerAppDraft.ImageConfig getImageConfigFromContainers(List<AppContainerMavenConfig> containers) {
        if (containers == null || containers.isEmpty()) {
            return null;
        }
        final ContainerAppDraft.ImageConfig imageConfig = new ContainerAppDraft.ImageConfig(containers.get(0).getImage());
        if (containers.get(0).getEnvironment() != null) {
            imageConfig.setEnvironmentVariables(containers.get(0).getEnvironment());
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
}
