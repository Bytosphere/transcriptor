package io.github.bytosphere.net;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of WebSocketClient using java.net.http.WebSocket.
 * <p>
 * Async-first by design: connect/send/close all return CompletableFuture,
 * matching java.net.http.WebSocket's own non-blocking model. Calling
 * .join() anywhere is a blocking escape hatch for scripts/tests — the
 * intended usage is chaining (.thenCompose/.thenAccept) so the calling
 * thread is never held up waiting on I/O.
 */
public class WebSocketClient {

    private final HttpClient httpClient;

    /**
     * A list of active WebSocket connections tracked by the client. This is to ensure proper cleanup.
     */
    private final Set<WebSocketConnection> connections = ConcurrentHashMap.newKeySet();

    /**
     * Creates a new DefaultWebSocketClient with the default connect timeout of 10 seconds.
     */
    public WebSocketClient() {
        this(Duration.ofSeconds(10));
    }

    /**
     * Creates a new DefaultWebSocketClient with the specified connect timeout.
     *
     * @param connectTimeout the maximum time to wait for the connection to be established
     */
    public WebSocketClient(Duration connectTimeout) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();
    }

    public <L extends AbstractWebSocketListener> CompletableFuture<WebSocketConnection> connect(URI uri, L listener) {
        return httpClient.newWebSocketBuilder()
            .buildAsync(uri, listener.asWebSocketListener())
            .thenApply(webSocket -> new WebSocketConnection(webSocket, this::onConnectionClosed));
    }

    private void onConnectionClosed(WebSocketConnection connection) {
        connections.remove(connection);
    }

    public void closeAll() {
        connections.forEach(
            w -> w.close(WebSocket.NORMAL_CLOSURE, "closed by client")
        );
    }
}