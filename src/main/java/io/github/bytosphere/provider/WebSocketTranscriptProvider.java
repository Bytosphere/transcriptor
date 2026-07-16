package io.github.bytosphere.provider;

import io.github.bytosphere.net.AbstractWebSocketListener;
import io.github.bytosphere.net.WebSocketClient;

/**
 * An abstract base class for transcript providers that use WebSockets for communication.
 * <p>
 * This class provides a foundation for implementing transcript providers that connect
 * to speech-to-text services over WebSocket. It holds a {@link WebSocketClient} that
 * manages the WebSocket connections. Subclasses typically provide specific implementations
 * that handle the message format and processing logic.
 *
 * @param <T> the type of transcript data produced by this provider
 * @see TranscriptProvider
 * @see AbstractWebSocketListener
 * @see WebSocketClient
 */
public abstract class WebSocketTranscriptProvider<T> implements TranscriptProvider<T> {

    protected final WebSocketClient client;

    /**
     * Creates a new WebSocket transcript provider using the default WebSocket client.
     * <p>
     * The default client uses a 10-second connect timeout.
     */
    public WebSocketTranscriptProvider() {
        this.client = new WebSocketClient();
    }

    /**
     * Creates a new WebSocket transcript provider using the specified WebSocket client.
     * <p>
     * This constructor allows dependency injection of a custom WebSocket client,
     * which is useful for testing or when custom client configuration is needed.
     *
     * @param client the WebSocket client to use for connections
     * @throws NullPointerException if client is null
     */
    public WebSocketTranscriptProvider(WebSocketClient client) {
        this.client = client;
    }
}
