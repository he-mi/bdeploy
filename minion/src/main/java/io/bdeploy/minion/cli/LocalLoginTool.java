package io.bdeploy.minion.cli;

import java.util.Map;

import io.bdeploy.common.cfg.Configuration.EnvironmentFallback;
import io.bdeploy.common.cfg.Configuration.Help;
import io.bdeploy.common.cfg.Configuration.Validator;
import io.bdeploy.common.cfg.RemoteValidator;
import io.bdeploy.common.cli.ToolBase.CliTool.CliName;
import io.bdeploy.common.cli.ToolBase.ConfiguredCliTool;
import io.bdeploy.common.cli.ToolCategory;
import io.bdeploy.common.cli.data.DataResult;
import io.bdeploy.common.cli.data.DataTable;
import io.bdeploy.common.cli.data.DataTableColumn;
import io.bdeploy.common.cli.data.RenderableResult;
import io.bdeploy.common.security.RemoteService;
import io.bdeploy.interfaces.remote.ResourceProvider;
import io.bdeploy.jersey.cli.LocalLoginData;
import io.bdeploy.jersey.cli.LocalLoginManager;
import io.bdeploy.jersey.cli.LocalLoginServer;
import io.bdeploy.minion.cli.LocalLoginTool.LoginConfig;
import io.bdeploy.ui.api.AuthResource;
import io.bdeploy.ui.utils.BrowserHelper;
import jakarta.ws.rs.core.UriBuilder;

@Help("Manage local login sessions")
@ToolCategory("Local session and scripting commands")
@CliName("login")
public class LocalLoginTool extends ConfiguredCliTool<LoginConfig> {

    @Help("Configuration for remote access")
    public @interface LoginConfig {

        @Help("URI of remote Server")
        @Validator(RemoteValidator.class)
        String remote();

        @Help("Perform a login to the given remote and store the session locally using the given name")
        String add();

        @Help("User to login, read from console if not given")
        String user();

        @Help("Password to use when logging in, read from console if not given")
        String password();

        @Help("A full authentication pack which is used instead of user and password to authenticate")
        String token();

        @Help("Instructs the --add command to replace existing users instead of throwing an error upon duplicate name entry")
        boolean replace() default false;

        @Help("Checks if the specified login is valid")
        String check();

        @Help("Remove the given stored login session")
        String remove();

        @Help(value = "List all stored login sessions", arg = false)
        boolean list() default false;

        @Help("The name of the stored login session to switch to")
        String use();

        @Help("Path to the directory which contains the local login data")
        @EnvironmentFallback("BDEPLOY_LOGIN_STORAGE")
        String loginStorage();

        @Help(value = "The name of the stored login session to login on the default browser")
        String open();

        @Help(value = "The specific URL to open. Must start with /#/ followed by the rest of the path. Ignored if openSession is not specified.")
        String openUrl();
    }

    public LocalLoginTool() {
        super(LoginConfig.class);
    }

    @Override
    protected RenderableResult run(LoginConfig config) {
        LocalLoginManager llm = new LocalLoginManager(config.loginStorage());

        if (config.add() != null) {
            addUser(config, llm);
        } else if (config.remove() != null) {
            llm.remove(config.remove());
        } else if (config.use() != null) {
            llm.setCurrent(config.use());
        } else if (config.check() != null) {
            return check(config, llm);
        } else if (config.list()) {
            LocalLoginData data = llm.read();

            DataTable table = createDataTable();
            table.column(new DataTableColumn.Builder("Name").setMinWidth(10).build());
            table.column(new DataTableColumn.Builder("URI").setMinWidth(20).build());
            table.column(new DataTableColumn.Builder("User").setMinWidth(20).build());
            table.column(new DataTableColumn.Builder("Active").setMinWidth(6).build());
            for (Map.Entry<String, LocalLoginServer> entry : data.servers.entrySet()) {
                table.row().cell(entry.getKey()).cell(entry.getValue().url).cell(entry.getValue().user)
                        .cell((data.current != null && data.current.equals(entry.getKey())) ? "*" : "").build();
            }
            return table;
        } else if (config.open() != null) {
            String serverName = config.open();
            RemoteService service = llm.getNamedService(serverName);
            if (service == null) {
                return createResultWithErrorMessage("Unknown server: " + serverName);
            }
            return BrowserHelper.openUrl(service, config.openUrl())//
                    ? createResultWithSuccessMessage("Successfully opened the web UI")//
                    : createResultWithErrorMessage("Failed to open the web UI");
        } else {
            return createNoOp();
        }

        return createSuccess();
    }

    private void addUser(LoginConfig config, LocalLoginManager llm) {
        helpAndFailIfMissing(config.remote(), "Missing --remote");

        if (config.token() != null) {
            RemoteService s = new RemoteService(UriBuilder.fromUri(config.remote()).build(), config.token());
            llm.loginWithService(config.add(), s);
            return;
        }

        if (config.user() == null || config.password() == null) {
            out().println("Please specify user and password for " + config.remote());
        }

        String user;
        if (config.user() != null) {
            user = config.user();
        } else {
            out().print("User: ");
            user = System.console().readLine();
        }

        char[] pass;
        if (config.password() != null) {
            pass = config.password().toCharArray();
        } else {
            out().print("Password: ");
            pass = System.console().readPassword();
        }

        llm.login(config.replace(), config.add(), config.remote(), user, pass);
    }

    private DataResult check(LoginConfig config, LocalLoginManager llm) {
        String serverName = config.check();
        RemoteService service = llm.getNamedService(serverName);
        if (service == null) {
            return createResultWithErrorMessage("Unknown server: " + serverName);
        }

        try {
            getAuthRes(service).getCurrentUser();
        } catch (RuntimeException e) {
            return createResultWithErrorMessage("Failed to validate login " + serverName).setException(e);
        }
        return createSuccess();
    }

    private static AuthResource getAuthRes(RemoteService service) {
        return ResourceProvider.getResource(service, AuthResource.class, null);
    }
}
