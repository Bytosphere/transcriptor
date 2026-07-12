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

    protected abstract void onMessage(String message);

    protected abstract void onBinaryMessage(ByteBuffer message);

    protected abstract void onClose(int statusCode, String reason);

    protected abstract void onError(Throwable error);
}
