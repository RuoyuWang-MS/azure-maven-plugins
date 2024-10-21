package com.microsoft.azure.toolkit.lib.containerapps.config;

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
public class ContainerAppsEnvironmentConfig {
    private String subscriptionId;
    private String resourceGroup;
    private String appEnvironmentName;
    private String region;
}
