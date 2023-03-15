package com.microsoft.azure.toolkit.lib.servicebus;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.model.Deletable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ServiceBusNamespace extends AbstractAzResource<ServiceBusNamespace, ServiceBusNamespaceSubscription, com.azure.resourcemanager.servicebus.models.ServiceBusNamespace> implements Deletable {
    @Nonnull
    private final ServiceBusQueueModule queueModule;
    @Nonnull
    private final ServiceBusTopicModule topicModule;
    protected ServiceBusNamespace(@Nonnull String name, @Nonnull String resourceGroupName, @Nonnull ServiceBusNamespaceModule module) {
        super(name, resourceGroupName, module);
        this.queueModule = new ServiceBusQueueModule(this);
        this.topicModule = new ServiceBusTopicModule(this);
    }

    protected ServiceBusNamespace(@Nonnull ServiceBusNamespace origin) {
        super(origin);
        this.queueModule = origin.queueModule;
        this.topicModule = origin.topicModule;
    }

    protected ServiceBusNamespace(@Nonnull com.azure.resourcemanager.servicebus.models.ServiceBusNamespace remote, @Nonnull ServiceBusNamespaceModule module) {
        super(remote.name(), ResourceId.fromString(remote.id()).resourceGroupName(), module);
        this.queueModule = new ServiceBusQueueModule(this);
        this.topicModule = new ServiceBusTopicModule(this);
    }

    @Nonnull
    @Override
    public List<AbstractAzResourceModule<?, ?, ?>> getSubModules() {
        return Arrays.asList(queueModule, topicModule);
    }

    @Nonnull
    @Override
    public String loadStatus(@Nonnull com.azure.resourcemanager.servicebus.models.ServiceBusNamespace remote) {
        return remote.innerModel().status();
    }
}
