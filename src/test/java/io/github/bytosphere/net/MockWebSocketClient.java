package io.github.bytosphere.net;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Mock implementation of WebSocketClient for testing.
 * Allows simulating WebSocket events without actual network connections.
 */
public class MockWebSocketClient implements WebSocketClient {

    private Runnable onOpenHandler;
    private Consumer<String> onTextHandler;
    private Consumer<ByteBuffer> onBinaryHandler;
    private BiConsumer<Integer, String> onCloseHandler;
    private Consumer<Throwable> onErrorHandler;

    private boolean connected = false;

    @Override
    public MockWebSocketClient onOpen(Runnable handler) {
        this.onOpenHandler = handler;
        return this;
    }

    @Override
    public MockWebSocketClient onText(Consumer<String> handler) {
        this.onTextHandler = handler;
        return this;
    }

    @Override
    public MockWebSocketClient onBinary(Consumer<ByteBuffer> handler) {
        this.onBinaryHandler = handler;
        return this;
    }

    @Override
    public MockWebSocketClient onClose(BiConsumer<Integer, String> handler) {
        this.onCloseHandler = handler;
        return this;
    }

    @Override
    public MockWebSocketClient onError(Consumer<Throwable> handler) {
        this.onErrorHandler = handler;
        return this;
    }

    @Override
    public CompletableFuture<WebSocketClient> connect(String url) {
        connected = true;
        if (onOpenHandler != null) {
            onOpenHandler.run();
        }
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public CompletableFuture<java.net.http.WebSocket> sendText(String message) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<java.net.http.WebSocket> sendBinary(ByteBuffer data) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<java.net.http.WebSocket> close() {
        return close(java.net.http.WebSocket.NORMAL_CLOSURE, "client closing");
    }

    @Override
    public CompletableFuture<java.net.http.WebSocket> close(int statusCode, String reason) {
        connected = false;
        if (onCloseHandler != null) {
            onCloseHandler.accept(statusCode, reason);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    // Helper methods to simulate events for testing

    /**
     * Simulates receiving a text message from the WebSocket server.
     */
    public void simulateTextMessage(String message) {
        if (onTextHandler != null) {
            onTextHandler.accept(message);
        }
    }

    /**
     * Simulates receiving a binary message from the WebSocket server.
     */
    public void simulateBinaryMessage(ByteBuffer message) {
        if (onBinaryHandler != null) {
            onBinaryHandler.accept(message);
        }
    }

    /**
     * Simulates the WebSocket connection being closed by the server.
     */
    public void simulateClose(int statusCode, String reason) {
        connected = false;
        if (onCloseHandler != null) {
            onCloseHandler.accept(statusCode, reason);
        }
    }

    /**
     * Simulates a WebSocket error.
     */
    public void simulateError(Throwable error) {
        if (onErrorHandler != null) {
            onErrorHandler.accept(error);
        }
    }

    /**
     * Simulates the WebSocket connection being opened.
     */
    public void simulateOpen() {
        connected = true;
        if (onOpenHandler != null) {
            onOpenHandler.run();
        }
    }
}