package io.github.bytosphere.net;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Default implementation of WebSocketClient using java.net.http.WebSocket.
 * <p>
 * Async-first by design: connect/send/close all return CompletableFuture,
 * matching java.net.http.WebSocket's own non-blocking model. Calling
 * .join() anywhere is a blocking escape hatch for scripts/tests — the
 * intended usage is chaining (.thenCompose/.thenAccept) so the calling
 * thread is never held up waiting on I/O.
 */
public class DefaultWebSocketClient implements WebSocketClient {

    private final HttpClient httpClient;
    private final Duration connectTimeout;

    private Runnable onOpen;
    private Consumer<String> onText;
    private Consumer<ByteBuffer> onBinary;
    private BiConsumer<Integer, String> onClose;
    private Consumer<Throwable> onError;

    private WebSocket webSocket;

    /**
     * Creates a new DefaultWebSocketClient with the default connect timeout of 10 seconds.
     */
    public DefaultWebSocketClient() {
        this(Duration.ofSeconds(10));
    }

    /**
     * Creates a new DefaultWebSocketClient with the specified connect timeout.
     *
     * @param connectTimeout the maximum time to wait for the connection to be established
     */
    public DefaultWebSocketClient(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();
    }

    @Override
    public DefaultWebSocketClient onOpen(Runnable handler) {
        this.onOpen = handler;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultWebSocketClient onText(Consumer<String> handler) {
        this.onText = handler;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultWebSocketClient onBinary(Consumer<ByteBuffer> handler) {
        this.onBinary = handler;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultWebSocketClient onClose(BiConsumer<Integer, String> handler) {
        this.onClose = handler;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultWebSocketClient onError(Consumer<Throwable> handler) {
        this.onError = handler;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<WebSocketClient> connect(String url) {
        return httpClient.newWebSocketBuilder()
            .connectTimeout(connectTimeout)
            .buildAsync(URI.create(url), new InternalListener())
            .thenApply(ws -> {
                this.webSocket = ws;
                return this;
            });
    }

    /**
     * Sends a text frame. Returns the in-flight future — chain off it for
     * async use, or call .join() for sync use. Note: java.net.http.WebSocket
     * allows only one outstanding send at a time; sending again before this
     * future completes throws IllegalStateException.
     */
    @Override
    public CompletableFuture<WebSocket> sendText(String message) {
        requireConnected();
        return webSocket.sendText(message, true);
    }

    @Override
    public CompletableFuture<WebSocket> sendBinary(ByteBuffer data) {
        requireConnected();
        return webSocket.sendBinary(data, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<WebSocket> close() {
        return close(WebSocket.NORMAL_CLOSURE, "client closing");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<WebSocket> close(int statusCode, String reason) {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            return webSocket.sendClose(statusCode, reason);
        }
        return CompletableFuture.completedFuture(webSocket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return webSocket != null && !webSocket.isOutputClosed();
    }

    /**
     * Ensures that the WebSocket is connected before performing operations.
     *
     * @throws IllegalStateException if the WebSocket is not connected
     */
    private void requireConnected() {
        if (webSocket == null) {
            throw new IllegalStateException("WebSocketClient is not connected — call connect() first");
        }
    }

    /**
     * Bridges java.net.http.WebSocket.Listener callbacks to the plain
     * functional handlers registered above. Keeps flow-control (request(1))
     * delegated to the default implementations.
     */
    private class InternalListener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            if (onOpen != null) {
                onOpen.run();
            }
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public java.util.concurrent.CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            if (onText != null) {
                onText.accept(data.toString());
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public java.util.concurrent.CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            if (onBinary != null) {
                onBinary.accept(data);
            }
            return WebSocket.Listener.super.onBinary(webSocket, data, last);
        }

        @Override
        public java.util.concurrent.CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            if (onClose != null) {
                onClose.accept(statusCode, reason);
            }
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            if (onError != null) {
                onError.accept(error);
            }
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}