package io.bdeploy.messaging;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.angus.mail.util.MailConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Service;
import jakarta.mail.Session;
import jakarta.mail.URLName;

/**
 * Handles the opening and closing of a connection to a {@link Service}.
 *
 * @param <S> The type of {@link Service} to handle the connection to
 */
public abstract class ServiceConnectionHandler<S extends Service> implements ConnectionHandler {

    private static final Logger log = LoggerFactory.getLogger(ServiceConnectionHandler.class);

    private final ExecutorService connectionThreadExecutor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat(getClass().getSimpleName() + "ConnectionThread-%d").build());

    private String protocol;
    private Session session;
    private S service;

    @Override
    public CompletableFuture<Void> connect(URLName url, boolean testMode) {
        return CompletableFuture.runAsync(() -> doConnect(url, testMode), connectionThreadExecutor);
    }

    /**
     * Disconnects the {@link ServiceConnectionHandler} by {@link Service#close() closing} the internal {@link Service}.
     *
     * @see Service#close()
     */
    @Override
    public void disconnect() {
        if (service != null && service.isConnected()) {
            try {
                service.close();
                service = null;
            } catch (MessagingException e) {
                log.error("Exception while disconnecting service " + service.getURLName(), e);
            }
        }
    }

    @Override
    public void close() {
        connectionThreadExecutor.close();
        disconnect();
    }

    protected String getProtocol() {
        return protocol;
    }

    protected Session getSession() {
        return session;
    }

    protected S getService() {
        return service;
    }

    /**
     * Called after the connecting of the internal {@link Service} was completed successfully. The provided {@link URLName} does
     * not contain the password.
     * <p>
     * This method will be executed as part of the {@link CompletableFuture} of {@link #connect(URLName)}.
     *
     * @param url The {@link URLName} to which the connection was established
     * @param testMode If <code>true</code>, the implementation should start in test mode
     */
    protected void afterConnect(URLName url, boolean testMode) {
        // Only a hook for subclasses - does nothing by default
    }

    protected static NoSuchProviderException getNoSuchProviderException(String protocol) {
        return new NoSuchProviderException("Protocol " + protocol + " is not supported.");
    }

    protected abstract void modifyProperties(Properties properties);

    protected abstract Session createSession(Properties properties) throws NoSuchProviderException;

    protected abstract S createService(URLName url) throws NoSuchProviderException;

    private void doConnect(URLName url, boolean testMode) {
        if (url == null) {
            throw new IllegalArgumentException("Connection handling called with url being null.");
        }
        try {
            disconnect();

            URLName urlWithoutPassword = url.getPassword() == null ? url
                    : new URLName(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), url.getUsername(), null);

            if (log.isTraceEnabled()) {
                log.trace("Attempting connection to {}", urlWithoutPassword);
            }

            protocol = urlWithoutPassword.getProtocol();

            if (protocol == null) {
                throw getNoSuchProviderException(null);
            }
            protocol = protocol.trim().toLowerCase();

            Properties properties = new Properties();
            modifyProperties(properties);

            if (log.isTraceEnabled()) {
                log.trace("Properties for {}: {}", protocol, properties);
            }

            session = createSession(properties);
            service = createService(url);
            service.addConnectionListener(
                    (LoggingConnectionListener<S>) (source, info) -> log.info("{}{}", info, source.getURLName()));
            service.connect();
            afterConnect(urlWithoutPassword, testMode);
        } catch (RuntimeException | MessagingException e) {
            if (testMode) {
                throw new IllegalStateException("Connection test failed", e);
            } else if (e instanceof MailConnectException) {
                log.warn("Failed to connect.", e);
            } else {
                log.error("Unexpected exception during connection handling.", e);
            }
        }
    }
}
