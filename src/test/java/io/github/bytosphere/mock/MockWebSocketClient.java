package io.github.bytosphere.mock;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MockWebSocketClient {

    private Runnable onOpenHandler;
    private Consumer<String> onTextHandler;
    private Consumer<ByteBuffer> onBinaryHandler;
    private BiConsumer<Integer, String> onCloseHandler;
    private Consumer<Throwable> onErrorHandler;

    private boolean connected = false;

    /**
     * Sets a handler to be called when the WebSocket connection is opened.
     *
     * @param handler the Runnable to execute when the connection is opened
     * @return this MockWebSocketClient for method chaining
     */
    public MockWebSocketClient onOpen(Runnable handler) {
        this.onOpenHandler = handler;
        return this;
    }

    /**
     * Sets a handler to be called when a text message is received.
     *
     * @param handler the Consumer to handle the text message
     * @return this MockWebSocketClient for method chaining
     */
    public MockWebSocketClient onText(Consumer<String> handler) {
        this.onTextHandler = handler;
        return this;
    }

    /**
     * Sets a handler to be called when a binary message is received.
     *
     * @param handler the Consumer to handle the binary message
     * @return this MockWebSocketClient for method chaining
     */
    public MockWebSocketClient onBinary(Consumer<ByteBuffer> handler) {
        this.onBinaryHandler = handler;
        return this;
    }

    /**
     * Sets a handler to be called when the WebSocket connection is closed.
     *
     * @param handler the BiConsumer to handle the close event, receiving status code and reason
     * @return this MockWebSocketClient for method chaining
     */
    public MockWebSocketClient onClose(BiConsumer<Integer, String> handler) {
        this.onCloseHandler = handler;
        return this;
    }

    /**
     * Sets a handler to be called when an error occurs on the WebSocket.
     *
     * @param handler the Consumer to handle the error
     * @return this MockWebSocketClient for method chaining
     */
    public MockWebSocketClient onError(Consumer<Throwable> handler) {
        this.onErrorHandler = handler;
        return this;
    }

    /**
     * Simulates connecting to a WebSocket server.
     *
     * @return a CompletableFuture that completes with this MockWebSocketClient when connected
     */
    public CompletableFuture<MockWebSocketClient> connect(String url) {
        connected = true;
        if (onOpenHandler != null) {
            onOpenHandler.run();
        }
        return CompletableFuture.completedFuture(this);
    }

    /**
     * Simulates sending a text message over the WebSocket.
     *
     * @param message the text message to send
     * @return a CompletableFuture that completes when the message is sent
     */
    public CompletableFuture<java.net.http.WebSocket> sendText(String message) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Simulates sending a binary message over the WebSocket.
     *
     * @param data the binary data to send
     * @return a CompletableFuture that completes when the message is sent
     */
    public CompletableFuture<java.net.http.WebSocket> sendBinary(ByteBuffer data) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Simulates closing the WebSocket connection.
     *
     * @return a CompletableFuture that completes when the connection is closed
     */
    public CompletableFuture<java.net.http.WebSocket> close() {
        return close(java.net.http.WebSocket.NORMAL_CLOSURE, "client closing");
    }

    /**
     * Simulates closing the WebSocket connection with the specified status code and reason.
     *
     * @param statusCode the WebSocket close status code
     * @param reason the reason for closing
     * @return a CompletableFuture that completes when the connection is closed
     */
    public CompletableFuture<java.net.http.WebSocket> close(int statusCode, String reason) {
        connected = false;
        if (onCloseHandler != null) {
            onCloseHandler.accept(statusCode, reason);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Checks if the WebSocket is currently connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    // Helper methods to simulate events for testing

    /**
     * Simulates receiving a text message from the WebSocket server.
     *
     * @param message the text message received
     */
    public void simulateTextMessage(String message) {
        if (onTextHandler != null) {
            onTextHandler.accept(message);
        }
    }

    /**
     * Simulates receiving a binary message from the WebSocket server.
     *
     * @param message the binary message received
     */
    public void simulateBinaryMessage(ByteBuffer message) {
        if (onBinaryHandler != null) {
            onBinaryHandler.accept(message);
        }
    }

    /**
     * Simulates the WebSocket connection being closed by the server.
     *
     * @param statusCode the close status code
     * @param reason the close reason
     */
    public void simulateClose(int statusCode, String reason) {
        connected = false;
        if (onCloseHandler != null) {
            onCloseHandler.accept(statusCode, reason);
        }
    }

    /**
     * Simulates a WebSocket error.
     *
     * @param error the error that occurred
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