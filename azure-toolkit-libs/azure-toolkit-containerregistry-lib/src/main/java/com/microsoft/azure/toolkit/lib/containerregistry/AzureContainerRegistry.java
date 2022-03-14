/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.lib.containerregistry;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.resources.models.Providers;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.AzureConfiguration;
import com.microsoft.azure.toolkit.lib.AzureService;
import com.microsoft.azure.toolkit.lib.auth.Account;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.AzureService.getUserAgentPolicy;

public class AzureContainerRegistry extends AbstractAzService<AzureContainerRegistryResourceManager, ContainerRegistryManager> {

    public AzureContainerRegistry() {
        super("Microsoft.ContainerRegistry");
    }

    @Override
    protected AzureContainerRegistryResourceManager newResource(@NotNull ContainerRegistryManager containerRegistryManager) {
        return new AzureContainerRegistryResourceManager(containerRegistryManager, this);
    }

    public AzureContainerRegistryModule registry(@Nonnull String subscriptionId) {
        final AzureContainerRegistryResourceManager rm = get(subscriptionId, null);
        assert rm != null;
        return rm.registry();
    }

    @Nullable
    @Override
    protected ContainerRegistryManager loadResourceFromAzure(@NotNull String subscriptionId, String resourceGroup) {
        final Account account = Azure.az(AzureAccount.class).account();
        final String tenantId = account.getSubscription(subscriptionId).getTenantId();
        final AzureConfiguration config = Azure.az().config();
        final String userAgent = config.getUserAgent();
        final HttpLogOptions logOptions = new HttpLogOptions();
        logOptions.setLogLevel(Optional.ofNullable(config.getLogLevel()).map(HttpLogDetailLevel::valueOf).orElse(HttpLogDetailLevel.NONE));
        final AzureProfile azureProfile = new AzureProfile(tenantId, subscriptionId, account.getEnvironment());
        // todo: migrate resource provider related codes to common library
        final Providers providers = ResourceManager.configure()
                .withHttpClient(AzureService.getDefaultHttpClient())
                .withPolicy(getUserAgentPolicy(userAgent))
                .authenticate(account.getTokenCredential(subscriptionId), azureProfile)
                .withSubscription(subscriptionId).providers();
        return ContainerRegistryManager
                .configure()
                .withHttpClient(AzureService.getDefaultHttpClient())
                .withLogOptions(logOptions)
                .withPolicy(getUserAgentPolicy(userAgent))
                .withPolicy(new ProviderRegistrationPolicy(providers)) // add policy to auto register resource providers
                .authenticate(account.getTokenCredential(subscriptionId), azureProfile);
    }

    @Override
    public String getResourceTypeName() {
        return "Container Registries";
    }
}
