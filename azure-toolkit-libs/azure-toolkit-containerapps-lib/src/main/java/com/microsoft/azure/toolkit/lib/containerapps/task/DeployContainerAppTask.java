package com.microsoft.azure.toolkit.lib.containerapps.task;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.containerapps.AzureContainerApps;
import com.microsoft.azure.toolkit.lib.containerapps.config.ContainerAppConfig;
import com.microsoft.azure.toolkit.lib.containerapps.config.ContainerAppsEnvironmentConfig;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerApp;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import com.microsoft.azure.toolkit.lib.containerapps.environment.ContainerAppsEnvironment;
import com.microsoft.azure.toolkit.lib.containerapps.environment.ContainerAppsEnvironmentDraft;
import com.microsoft.azure.toolkit.lib.resource.AzureResources;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.azure.toolkit.lib.resource.task.CreateResourceGroupTask;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class DeployContainerAppTask extends AzureTask<ContainerApp> {
    private static final String EXPANDABLE_REGION_WARNING = "'%s' may not be a valid region, " +
        "please refer to https://aka.ms/maven_webapp_runtime#region for valid values";

    private final ContainerAppConfig config;
    private ContainerApp containerApp;
    @Nonnull
    private final List<AzureTask<?>> subTasks;
    public DeployContainerAppTask(ContainerAppConfig config) {
        this.config = config;
        this.subTasks = new ArrayList<>();
        this.initTasks();
    }
    private void initTasks() {
        final ContainerAppsEnvironmentConfig environmentConfig = config.getEnvironment();
        final String subscriptionId = Optional.ofNullable(environmentConfig).map(ContainerAppsEnvironmentConfig::getSubscriptionId).filter(StringUtils::isNotBlank).orElseThrow(() -> new AzureToolkitRuntimeException("'subscriptionId' is required"));
        final String environmentName = Optional.ofNullable(environmentConfig).map(ContainerAppsEnvironmentConfig::getAppEnvironmentName).filter(StringUtils::isNotBlank).orElseThrow(() -> new AzureToolkitRuntimeException("'environmentName' is required"));
        final String appName = Optional.ofNullable(config).map(ContainerAppConfig::getAppName).filter(StringUtils::isNotBlank).orElseThrow(() -> new AzureToolkitRuntimeException("'appName' is required"));
        final String resourceGroupName = Optional.ofNullable(environmentConfig).map(ContainerAppsEnvironmentConfig::getResourceGroup).filter(StringUtils::isNotBlank).orElseThrow(() -> new AzureToolkitRuntimeException("'resourceGroup' is required"));

        addCreateResourceGroupTaskIfNecessary(environmentConfig);
        addCreateAppEnvironmentTaskIfNecessary(environmentConfig);
        addCreateOrUpdateContainerAppTask();
    }

    private void addCreateResourceGroupTaskIfNecessary(@Nonnull final ContainerAppsEnvironmentConfig config) {
        final ResourceGroup resourceGroup = Azure.az(AzureResources.class).groups(config.getSubscriptionId())
            .getOrDraft(config.getResourceGroup(), config.getResourceGroup());

        if (resourceGroup.isDraftForCreating() && !resourceGroup.exists()) {
            final String region = Optional.ofNullable(config.getRegion()).filter(StringUtils::isNotBlank).orElseThrow(() -> new AzureToolkitRuntimeException("'region' is required"));
            this.subTasks.add(new CreateResourceGroupTask(config.getSubscriptionId(), config.getResourceGroup(), Region.fromName(region)));
        }
    }

    private void addCreateAppEnvironmentTaskIfNecessary(@Nonnull final ContainerAppsEnvironmentConfig config) {
        final ContainerAppsEnvironment environment = Azure.az(AzureContainerApps.class).environments(config.getSubscriptionId())
            .getOrDraft(config.getAppEnvironmentName(), config.getResourceGroup());
        if (environment.isDraftForCreating() && !environment.exists()) {
            final AzureString title = AzureString.format("Create new Container Apps Environment({0})", environment.getName());
            this.subTasks.add(new AzureTask<Void>(title, () -> {
                final ResourceGroup resourceGroup = Azure.az(AzureResources.class).groups(config.getSubscriptionId())
                    .get(config.getResourceGroup(), config.getResourceGroup());
                final ContainerAppsEnvironmentDraft draft = (ContainerAppsEnvironmentDraft) environment;
                final ContainerAppsEnvironmentDraft.Config draftConfig = new ContainerAppsEnvironmentDraft.Config();
                draftConfig.setName(config.getAppEnvironmentName());
                draftConfig.setResourceGroup(resourceGroup);
                draftConfig.setRegion(Region.fromName(config.getRegion()));
                draft.setConfig(draftConfig);
                draft.commit();
            }));
        }
    }

    private void addCreateOrUpdateContainerAppTask() {
        final ContainerAppDraft containerAppDraft = Azure.az(AzureContainerApps.class).containerApps(config.getEnvironment().getSubscriptionId())
            .updateOrCreate(config.getAppName(), config.getEnvironment().getResourceGroup());
        final AzureString title = AzureString.format("Create or update Container App({0})", config.getAppName());
        this.subTasks.add(new AzureTask<Void>(title, () -> {
            containerAppDraft.setConfig(toContainerAppDraftConfig());
            containerApp = containerAppDraft.commit();
        }));
    }

    private ContainerAppDraft.Config toContainerAppDraftConfig() {
        final ContainerAppDraft.Config draftConfig = new ContainerAppDraft.Config();
        final ContainerAppsEnvironmentConfig environmentConfig = config.getEnvironment();
        final ContainerAppsEnvironment environment = Azure.az(AzureContainerApps.class).environments(environmentConfig.getSubscriptionId())
            .get(environmentConfig.getAppEnvironmentName(), environmentConfig.getResourceGroup());
        draftConfig.setEnvironment(environment);
        draftConfig.setSubscription(environment.getSubscription());
        draftConfig.setResourceGroup(environment.getResourceGroup());
        draftConfig.setName(config.getAppName());
        draftConfig.setImageConfig(config.getImageConfig());
        draftConfig.setIngressConfig(config.getIngressConfig());
        draftConfig.setResourceConfiguration(config.getResourceConfiguration());
        draftConfig.setScaleConfig(config.getScaleConfig());

        return draftConfig;
    }

    @Override
    @AzureOperation(name = "internal/containerapps.create_update_app.app", params = {"config.getAppName()"})
    public ContainerApp doExecute()  throws Exception {
        for (final AzureTask<?> t : this.subTasks) {
            t.getBody().call();
        }
        return containerApp;
    }
}