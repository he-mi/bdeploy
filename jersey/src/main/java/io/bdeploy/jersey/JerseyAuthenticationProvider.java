package io.bdeploy.jersey;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.annotation.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bdeploy.common.security.ApiAccessToken;
import io.bdeploy.common.security.SecurityHelper;
import io.bdeploy.jersey.errorpages.JerseyCustomErrorPages;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * A {@link ContainerRequestFilter} which enforces token based authentication
 * for all requests.
 * <p>
 * Requests using the {@link Unsecured} annotation are excluded from the
 * enforcement.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JerseyAuthenticationProvider implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String BDEPLOY_ALT_AUTH_HEADER = "X-BDeploy-Authorization";
    private static final String THREAD_ORIG_NAME = "THREAD_ORIG_NAME";
    private static final Logger log = LoggerFactory.getLogger(JerseyAuthenticationProvider.class);

    /**
     * Mark a single endpoint as "unsecure", allowing a call without valid token.
     */
    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    public @interface Unsecured {
    }

    /**
     * Allows authentication using a weak token. A weak token is used by applications
     * (e.g. the client launcher).
     */
    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    public @interface WeakTokenAllowed {
    }

    /**
     * {@link ContainerRequestFilter} which marks a request as authenticated for
     * endpoints with the {@link Unsecured} annotation.
     */
    @Unsecured
    @Provider
    @Priority(Priorities.AUTHENTICATION - 1) // before auth
    public static class JerseyAuthenticationUnprovider implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setProperty(NO_AUTH, NO_AUTH);
        }
    }

    @WeakTokenAllowed
    @Provider
    @Priority(Priorities.AUTHENTICATION - 2) // before auth and unauth
    public static class JerseyAuthenticationWeakenerProvider implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setProperty(WEAK_AUTH, WEAK_AUTH);
        }

    }

    public static final String AUTHENTICATION_SCHEME = "Bearer";
    private static final String REALM = "BDeploy";
    private static final String NO_AUTH = "unsecured";
    private static final String WEAK_AUTH = "weak";
    private final KeyStore store;
    private final UserValidator userValidator;

    public JerseyAuthenticationProvider(KeyStore store, UserValidator userValidator) {
        this.store = store;
        this.userValidator = userValidator;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        requestContext.setProperty(THREAD_ORIG_NAME, Thread.currentThread().getName());
        Thread.currentThread().setName(path);

        if (requestContext.getProperty(NO_AUTH) != null) {
            // target annotated with @Unsecured
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Authenticating {}", path);
        }

        // Get the Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(BDEPLOY_ALT_AUTH_HEADER);
        if (authorizationHeader == null) {
            authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        }

        // Check cookie.
        if (authorizationHeader == null && requestContext.getCookies().containsKey("st")) {
            authorizationHeader = AUTHENTICATION_SCHEME + " " + requestContext.getCookies().get("st").getValue();
        }

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        // Extract the token from the Authorization header
        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        try {

            // Validate the token
            ApiAccessToken api = validateToken(token, store);

            // check if weak is allowed
            if (api.isWeak() && requestContext.getProperty(WEAK_AUTH) == null) {
                abortWithUnauthorized(requestContext);
            }

            if (!api.isSystem() && userValidator != null && !userValidator.isValid(api.getIssuedTo())) {
                abortWithUnauthorized(requestContext);
            }

            // setup custom security context
            requestContext.setSecurityContext(
                    new JerseySecurityContext(api, requestContext.getHeaderString(JerseyOnBehalfOfFilter.ON_BEHALF_OF_HEADER)));

        } catch (Exception e) {
            log.error("Exception while parsing authorization: {}", e.toString());
            abortWithUnauthorized(requestContext);
        }

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String name = (String) requestContext.getProperty(THREAD_ORIG_NAME);
        if (name != null) {
            Thread.currentThread().setName(name);
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        // Check if the Authorization header is valid
        // It must not be null and must be prefixed with "Bearer" plus a whitespace
        // The authentication scheme comparison must be case-insensitive
        return authorizationHeader != null
                && authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        // Abort the filter chain with a 401 status code response
        // The WWW-Authenticate header is sent along with the response
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(JerseyCustomErrorPages.getErrorHtml(Response.Status.UNAUTHORIZED.getStatusCode(), "Not Authorized."))
                .header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"").build());
    }

    public static ApiAccessToken validateToken(String tokenValue, KeyStore ks) {
        try {
            ApiAccessToken token = SecurityHelper.getInstance().getVerifiedPayload(tokenValue, ApiAccessToken.class, ks);

            if (token != null && token.isValid()) {
                return token;
            } else {
                throw new IllegalStateException("Access token is null or no longer valid");
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot verify access token.", e);
        }
    }

    /**
     * A {@link UserValidator} will verify that a successfully authenticated user is still valid.
     * <p>
     * The validator should make sure that the user is still available, active, etc.
     */
    @FunctionalInterface
    public interface UserValidator {

        public boolean isValid(String user);
    }

}
