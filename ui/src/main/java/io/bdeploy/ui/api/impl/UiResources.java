package io.bdeploy.ui.api.impl;

import org.glassfish.grizzly.http.server.HttpHandlerRegistration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import io.bdeploy.jersey.JerseyCachingCLStaticHttpHandler;
import io.bdeploy.jersey.JerseyEagerServiceInitializer;
import io.bdeploy.jersey.RegistrationTarget;
import io.bdeploy.ui.ProductTransferService;
import io.bdeploy.ui.ProductUpdateService;
import io.bdeploy.ui.RemoteEntryStreamRequestService;
import jakarta.inject.Singleton;

public class UiResources {

    private UiResources() {
    }

    public static void register(RegistrationTarget server) {
        server.addHandler(new JerseyCachingCLStaticHttpHandler(UiResources.class.getClassLoader(), "/webapp/"),
                HttpHandlerRegistration.ROOT);
        server.register(AuthResourceImpl.class);
        server.register(HiveResourceImpl.class);
        server.register(BackendInfoResourceImpl.class);
        server.register(NodeManagementResourceImpl.class);

        server.register(InstanceGroupResourceImpl.class);
        server.register(SoftwareRepositoryResourceImpl.class);

        server.register(SoftwareUpdateResourceImpl.class);
        server.register(CleanupResourceImpl.class);
        server.register(DownloadServiceImpl.class);

        server.register(ManagedServersResourceImpl.class);
        server.register(ManagedServersAttachEventResourceImpl.class);

        server.register(PluginResourceImpl.class);
        server.register(LoggingAdminResourceImpl.class);

        server.register(PermissionRequestFilter.class);

        // force immediate initialization of the manifest spawn bridge so it can attach to the hive registry.
        server.register(new JerseyEagerServiceInitializer<>(ManifestSpawnToChangeEventBridge.class));

        server.register(new AbstractBinder() {

            @Override
            protected void configure() {
                bind(DownloadTokenCache.class).in(Singleton.class).to(DownloadTokenCache.class);
                bind(ProductTransferService.class).in(Singleton.class).to(ProductTransferService.class);
                bind(RemoteEntryStreamRequestService.class).in(Singleton.class).to(RemoteEntryStreamRequestService.class);
                bind(ChangeEventManager.class).in(Singleton.class).to(ChangeEventManager.class);
                bind(ProductUpdateService.class).in(Singleton.class).to(ProductUpdateService.class);
                bind(ManifestSpawnToChangeEventBridge.class).in(Singleton.class).to(ManifestSpawnToChangeEventBridge.class);
            }
        });
    }

}
