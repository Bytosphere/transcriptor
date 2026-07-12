package io.github.bytosphere.provider;

import io.github.bytosphere.core.TranscriptListener;
import io.github.bytosphere.core.TranscriptionSession;
import io.github.bytosphere.net.WebSocketClient;

import java.net.URI;
import java.nio.ByteBuffer;

public class DefaultTranscriptProvider extends WebSocketTranscriptProvider<String> {

    private TranscriptListener<String> listener;

    public DefaultTranscriptProvider() {
        super();
    }

    public DefaultTranscriptProvider(WebSocketClient client) {
        super(client);
    }

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
                System.out.println("Closing session...");
                disconnect();
            }
        };
    }

    /**
     * Simulates receiving a transcript message from WebSocket.
     * Called from Main to test transcript handling.
     */
    public void simulateTranscript(String message) {
        listener.onTranscript(message);
    }

    @Override
    protected void onMessage(String message) {
        listener.onTranscript(message);
    }

    @Override
    protected void onBinaryMessage(ByteBuffer message) { }

    @Override
    protected void onClose(int statusCode, String reason) {
        if (listener != null) {
            listener.onClose();
        }
    }

    @Override
    protected void onError(Throwable error) {
        if (listener != null) {
            listener.onError(error);
        }
    }
}
