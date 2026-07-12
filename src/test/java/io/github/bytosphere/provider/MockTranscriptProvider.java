package io.github.bytosphere.provider;

import io.github.bytosphere.core.TranscriptListener;
import io.github.bytosphere.core.TranscriptionSession;
import io.github.bytosphere.net.WebSocketClient;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * Mock implementation of a transcript provider for testing purposes.
 * <p>
 * This class extends WebSocketTranscriptProvider and provides a convenient
 * way to simulate transcript messages without requiring a real WebSocket connection.
 * It is primarily used in unit tests.
 *
 * @see WebSocketTranscriptProvider
 * @see TranscriptProvider
 */
public class MockTranscriptProvider extends WebSocketTranscriptProvider<String> {

    private TranscriptListener<String> listener;

    /**
     * Creates a new MockTranscriptProvider with the default WebSocket client.
     */
    public MockTranscriptProvider() {
        super();
    }

    /**
     * Creates a new MockTranscriptProvider with the specified WebSocket client.
     *
     * @param client the WebSocket client to use for connections
     */
    public MockTranscriptProvider(WebSocketClient client) {
        super(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TranscriptionSession listen(URI url, TranscriptListener<String> listener) {
        this.listener = listener;
        return new TranscriptionSession() {

            public void disconnect() {
                getClient().close().thenAccept((ws) -> { });
            }

            public boolean isActive() {
                return getClient().isConnected();
            }

            @Override
            public void close() {
                disconnect();
            }
        };
    }

    /**
     * Simulates receiving a transcript message from WebSocket.
     * This method is primarily used for testing purposes to simulate
     * incoming transcript messages without requiring a real WebSocket connection.
     *
     * @param message the transcript message to simulate
     * @throws IllegalStateException if no listener is registered
     */
    public void simulateTranscript(String message) {
        listener.onTranscript(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMessage(String message) {
        if (listener != null) {
            listener.onTranscript(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBinaryMessage(ByteBuffer message) { }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose(int statusCode, String reason) {
        if (listener != null) {
            listener.onClose();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onError(Throwable error) {
        if (listener != null) {
            listener.onError(error);
        }
    }
}