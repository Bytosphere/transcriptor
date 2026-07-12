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

    WebSocketClient onOpen(Runnable handler);

    WebSocketClient onText(Consumer<String> handler);

    WebSocketClient onBinary(Consumer<ByteBuffer> handler);

    WebSocketClient onClose(BiConsumer<Integer, String> handler);

    WebSocketClient onError(Consumer<Throwable> handler);

    CompletableFuture<WebSocketClient> connect(String url);

    CompletableFuture<java.net.http.WebSocket> sendText(String message);

    CompletableFuture<java.net.http.WebSocket> sendBinary(ByteBuffer data);

    CompletableFuture<java.net.http.WebSocket> close();

    CompletableFuture<java.net.http.WebSocket> close(int statusCode, String reason);

    boolean isConnected();
}