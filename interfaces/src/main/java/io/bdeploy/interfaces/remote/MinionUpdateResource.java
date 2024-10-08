package io.bdeploy.interfaces.remote;

import io.bdeploy.bhive.model.Manifest;
import io.bdeploy.bhive.model.Manifest.Key;
import io.bdeploy.common.security.RequiredPermission;
import io.bdeploy.common.security.ScopedPermission;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Perform a remote update of a minion.
 */
@Path("/update")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredPermission(permission = ScopedPermission.Permission.ADMIN)
public interface MinionUpdateResource {

    /**
     * Queue an update. On success, the call returns without error and the minion is restarted and updated afterwards.
     * <p>
     * An update has to be prepared using {@link #prepare(Key, boolean)} before {@link #update(Key)} to succeed.
     *
     * @param key the {@link Key} to update to. The {@link Key} must have been pushed to the default hive before.
     */
    @PUT
    public void update(Manifest.Key key);

    /**
     * Perform preparation of an update. This involves checking free disc space, placing the update in the correct location for
     * the start script to pick it up, etc.
     *
     * @param key the {@link Key} to update to.
     * @param clean whether to clean up old versions.
     */
    @PUT
    @Path("/prepare")
    public void prepare(Manifest.Key key, @QueryParam("clean") boolean clean);

    /**
     * Converts the running server to type NODE and restarts. This operation cannot be reversed!
     */
    @GET
    @Path("/convertToNode")
    public void convertToNode();

    /**
     * Restart the current server.
     */
    @GET
    @Path("/restart")
    public void restart();

    /**
     * Shuts down the current server remotely.
     */
    @GET
    @Path("/shutdown")
    public void shutdown();
}
