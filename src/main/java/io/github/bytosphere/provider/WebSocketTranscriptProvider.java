package io.github.bytosphere.provider;

import io.github.bytosphere.net.DefaultWebSocketClient;
import io.github.bytosphere.net.WebSocketClient;

import java.nio.ByteBuffer;

/** An abstract class for transcript providers that use WebSockets. */
public abstract class WebSocketTranscriptProvider<T> implements TranscriptProvider<T> {

    private final WebSocketClient client;

    /** Creates a new WebSocket transcript provider using the default client. */
    public WebSocketTranscriptProvider() {
        this.client = new DefaultWebSocketClient()
            .onText(this::onMessage)
            .onBinary(this::onBinaryMessage)
            .onClose(this::onClose)
            .onError(this::onError);
    }

    /** Creates a new WebSocket transcript provider using the specified client implementation. */
    public WebSocketTranscriptProvider(WebSocketClient client) {
        this.client = client
            .onText(this::onMessage)
            .onBinary(this::onBinaryMessage)
            .onClose(this::onClose)
            .onError(this::onError);
    }

    public final WebSocketClient getClient() {
        return this.client;
    }

    /**
     * Called when a text message is received from the WebSocket.
     * Subclasses must implement this method to handle incoming text messages.
     *
     * @param message the text message received from the WebSocket
     */
    protected abstract void onMessage(String message);

    /**
     * Called when a binary message is received from the WebSocket.
     * Subclasses must implement this method to handle incoming binary messages.
     *
     * @param message the binary message received from the WebSocket
     */
    protected abstract void onBinaryMessage(ByteBuffer message);

    /**
     * Called when the WebSocket connection is closed.
     * Subclasses must implement this method to handle connection close events.
     *
     * @param statusCode the WebSocket close status code
     * @param reason the reason for closing
     */
    protected abstract void onClose(int statusCode, String reason);

    /**
     * Called when an error occurs on the WebSocket.
     * Subclasses must implement this method to handle error events.
     *
     * @param error the error that occurred
     */
    protected abstract void onError(Throwable error);
}
