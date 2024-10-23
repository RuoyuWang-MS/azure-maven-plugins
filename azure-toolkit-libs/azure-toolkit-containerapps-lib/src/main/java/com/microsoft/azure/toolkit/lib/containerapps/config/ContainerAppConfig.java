package com.microsoft.azure.toolkit.lib.containerapps.config;

import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import com.microsoft.azure.toolkit.lib.containerapps.model.IngressConfig;
import com.microsoft.azure.toolkit.lib.containerapps.model.ResourceConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContainerAppConfig {
    private ContainerAppsEnvironmentConfig environment;
    private String appName;
    private IngressConfig ingressConfig;
    //todo(ruoyuwang): change imageConfig && resourceConfiguration to containerConfig and registry;
    private ContainerAppDraft.ImageConfig imageConfig;
    private ResourceConfiguration resourceConfiguration;
    private ContainerAppDraft.ScaleConfig scaleConfig;
}
