package io.github.bytosphere.net;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Generic WebSocket client interface. Provides simple callback hooks for
 * open/text/binary/close/error — no domain logic baked in.
 * <p>
 * Async-first by design: connect/send/close all return CompletableFuture.
 */
public interface WebSocketClient {

    /**
     * Sets a handler to be called when the WebSocket connection is opened.
     *
     * @param handler the Runnable to execute when the connection is opened
     * @return this WebSocketClient for method chaining
     */
    WebSocketClient onOpen(Runnable handler);

    /**
     * Sets a handler to be called when a text message is received.
     *
     * @param handler the Consumer to handle the text message
     * @return this WebSocketClient for method chaining
     */
    WebSocketClient onText(Consumer<String> handler);

    /**
     * Sets a handler to be called when a binary message is received.
     *
     * @param handler the Consumer to handle the binary message
     * @return this WebSocketClient for method chaining
     */
    WebSocketClient onBinary(Consumer<ByteBuffer> handler);

    /**
     * Sets a handler to be called when the WebSocket connection is closed.
     *
     * @param handler the BiConsumer to handle the close event, receiving status code and reason
     * @return this WebSocketClient for method chaining
     */
    WebSocketClient onClose(BiConsumer<Integer, String> handler);

    /**
     * Sets a handler to be called when an error occurs on the WebSocket.
     *
     * @param handler the Consumer to handle the error
     * @return this WebSocketClient for method chaining
     */
    WebSocketClient onError(Consumer<Throwable> handler);

    /**
     * Connects to the specified WebSocket URL.
     *
     * @param url the WebSocket URL to connect to
     * @return a CompletableFuture that completes with this WebSocketClient when connected
     */
    CompletableFuture<WebSocketClient> connect(String url);

    /**
     * Sends a text message over the WebSocket.
     *
     * @param message the text message to send
     * @return a CompletableFuture that completes when the message is sent
     */
    CompletableFuture<java.net.http.WebSocket> sendText(String message);

    /**
     * Sends a binary message over the WebSocket.
     *
     * @param data the binary data to send
     * @return a CompletableFuture that completes when the message is sent
     */
    CompletableFuture<java.net.http.WebSocket> sendBinary(ByteBuffer data);

    /**
     * Closes the WebSocket connection with the normal closure status code.
     *
     * @return a CompletableFuture that completes when the connection is closed
     */
    CompletableFuture<java.net.http.WebSocket> close();

    /**
     * Closes the WebSocket connection with the specified status code and reason.
     *
     * @param statusCode the WebSocket close status code
     * @param reason the reason for closing
     * @return a CompletableFuture that completes when the connection is closed
     */
    CompletableFuture<java.net.http.WebSocket> close(int statusCode, String reason);

    /**
     * Checks if the WebSocket is currently connected.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();
}