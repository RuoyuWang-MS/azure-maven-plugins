package com.microsoft.azure.toolkit.lib.containerregistry.config;

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
public class ContainerRegistryConfig {
    private String subscriptionId;
    private String resourceGroup;
    private String registryName;
    private String region;
    private String sku;
}
