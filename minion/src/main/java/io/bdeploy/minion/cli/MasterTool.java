package io.bdeploy.minion.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import io.bdeploy.bhive.remote.jersey.BHiveRegistry;
import io.bdeploy.common.ActivityReporter;
import io.bdeploy.common.cfg.Configuration.EnvironmentFallback;
import io.bdeploy.common.cfg.Configuration.Help;
import io.bdeploy.common.cfg.Configuration.Validator;
import io.bdeploy.common.cfg.MinionRootValidator;
import io.bdeploy.common.cli.ToolBase.CliTool.CliName;
import io.bdeploy.common.cli.ToolBase.ConfiguredCliTool;
import io.bdeploy.common.security.SecurityHelper;
import io.bdeploy.interfaces.remote.MasterRootResource;
import io.bdeploy.interfaces.remote.SlaveDeploymentResource;
import io.bdeploy.jersey.JerseyCorsFilter;
import io.bdeploy.jersey.JerseyServer;
import io.bdeploy.jersey.RegistrationTarget;
import io.bdeploy.jersey.audit.AuditRecord;
import io.bdeploy.jersey.audit.RollingFileAuditor;
import io.bdeploy.minion.MinionRoot;
import io.bdeploy.minion.MinionState;
import io.bdeploy.minion.cli.MasterTool.MasterConfig;
import io.bdeploy.minion.remote.jersey.CentralUpdateResourceImpl;
import io.bdeploy.minion.remote.jersey.CommonRootResourceImpl;
import io.bdeploy.minion.remote.jersey.JerseyAwareMinionUpdateManager;
import io.bdeploy.minion.remote.jersey.MasterRootResourceImpl;
import io.bdeploy.minion.remote.jersey.MasterSettingsResourceImpl;
import io.bdeploy.ui.api.MinionMode;
import io.bdeploy.ui.api.impl.UiResources;

/**
 * Starts a HTTP(S) server which accepts {@link MasterRootResource} and
 * {@link SlaveDeploymentResource} API calls.
 */
@Help("Start a Master Minion.")
@CliName("master")
public class MasterTool extends ConfiguredCliTool<MasterConfig> {

    public @interface MasterConfig {

        @Help("Root directory for the master minion. Must be initialized using the init command.")
        @EnvironmentFallback("BDEPLOY_ROOT")
        @Validator(MinionRootValidator.class)
        String root();

        @Help("Specify the directory where any incoming updates should be placed in.")
        String updateDir();

        @Help(value = "Publish the web application, defaults to true.", arg = false)
        boolean publishWebapp() default true;

        @Help(value = "Allow CORS, allows the web-app to run on a different port than the backend.", arg = false)
        boolean allowCors() default false;
    }

    public MasterTool() {
        super(MasterConfig.class);
    }

    @Override
    protected void run(MasterConfig config) {
        helpAndFailIfMissing(config.root(), "Missing --root");

        ActivityReporter.Delegating delegate = new ActivityReporter.Delegating();
        try (MinionRoot r = new MinionRoot(Paths.get(config.root()), delegate)) {
            out().println("Starting " + r.getMode() + " master...");
            r.getAuditor().audit(AuditRecord.Builder.fromSystem().addParameters(getRawConfiguration()).setWhat("master").build());

            if (config.updateDir() != null) {
                Path upd = Paths.get(config.updateDir());
                r.setUpdateDir(upd);
            }

            MinionState state = r.getState();

            SecurityHelper sh = SecurityHelper.getInstance();
            KeyStore ks = sh.loadPrivateKeyStore(state.keystorePath, state.keystorePass);
            try (JerseyServer srv = new JerseyServer(state.port, ks, state.keystorePass)) {
                srv.setAuditor(new RollingFileAuditor(r.getAuditLogDir()));
                r.setUpdateManager(new JerseyAwareMinionUpdateManager(srv));
                r.onStartup();

                delegate.setDelegate(srv.getSseActivityReporter());
                registerMasterResources(srv, config.publishWebapp(), config.allowCors(), r, srv.getSseActivityReporter());

                srv.start();
                srv.join();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot start master server", e);
        }
    }

    public static void registerMasterResources(RegistrationTarget srv, boolean webapp, boolean allowcors, MinionRoot minionRoot,
            ActivityReporter reporter) {
        BHiveRegistry reg = SlaveTool.registerCommonResources(srv, minionRoot, reporter);
        minionRoot.setupServerTasks(true, minionRoot.getMode());

        if (minionRoot.getMode() == MinionMode.CENTRAL) {
            srv.register(CentralUpdateResourceImpl.class);
        } else {
            srv.register(MasterRootResourceImpl.class);
        }

        srv.register(CommonRootResourceImpl.class);
        srv.register(MasterSettingsResourceImpl.class);
        srv.register(new AbstractBinder() {

            @Override
            protected void configure() {
                // required for SoftwareUpdateResourceImpl.
                bind(MasterRootResourceImpl.class).to(MasterRootResource.class);
            }
        });

        if (webapp) {
            UiResources.register(srv);
        }

        if (allowcors) {
            srv.register(JerseyCorsFilter.class);
        }

        // scan storage locations, register hives
        minionRoot.getStorageLocations().forEach(reg::scanLocation);
    }

}
