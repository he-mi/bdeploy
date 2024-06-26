package io.bdeploy.jersey.security;

import io.bdeploy.jersey.JerseyAuthenticationProvider.Unsecured;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/security")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public interface SecurityTestResource {

    @GET
    @Unsecured
    @Path("/unsecured")
    public String testUnsecured();

    @GET
    @Path("/secured")
    public String testSecured();

}
