package io.github.bytosphere.mock;

import io.github.bytosphere.core.TranscriptListener;
import io.github.bytosphere.core.TranscriptionSession;
import io.github.bytosphere.provider.TranscriptProvider;

import java.net.URI;

public class MockTranscriptProvider implements TranscriptProvider<String> {

    private TranscriptListener<String> listener;
    private MockWebSocketClient mockClient;

    /**
     * Creates a new MockTranscriptProvider.
     */
    public MockTranscriptProvider() {
    }

    /**
     * Creates a new MockTranscriptProvider with the given client.
     * <p>
     * This constructor exists for compatibility with existing tests that pass
     * a MockWebSocketClient.
     *
     * @param client the WebSocket client (must be MockWebSocketClient for test functionality)
     * @deprecated This constructor is for test compatibility only.
     */
    @Deprecated
    public MockTranscriptProvider(Object client) {
        if (client instanceof MockWebSocketClient) {
            this.mockClient = (MockWebSocketClient) client;
        }
    }

    /**
     * Convenience method for testing that accepts a URI and listener.
     * <p>
     * This method sets up the connection to the mock WebSocket client.
     *
     * @param uri the URI to connect to
     * @param listener the listener to receive transcript events
     * @return a TranscriptionSession
     */
    public TranscriptionSession listen(URI uri, TranscriptListener<String> listener) {
        // Set up handlers on the mock client if available
        if (mockClient != null) {
            this.listener = listener;

            // Set up text message handler
            mockClient.onText(message -> {
                if (this.listener != null) {
                    this.listener.onTranscript(message);
                }
            });

            // Set up error handler
            mockClient.onError(error -> {
                if (this.listener != null) {
                    this.listener.onError(error);
                }
            });

            // Set up close handler
            mockClient.onClose((statusCode, reason) -> {
                if (this.listener != null) {
                    this.listener.onClose(statusCode, reason);
                }
            });

            // Connect the mock client
            mockClient.connect(uri.toString());
        }

        return createSession();
    }

    /**
     * Convenience method for testing that accepts a URI and Consumer.
     * <p>
     * This method sets up the connection to the mock WebSocket client if available.
     *
     * @param uri the URI to connect to
     * @param listener the consumer to receive transcript events
     * @return a TranscriptionSession
     */
    public TranscriptionSession listen(URI uri, java.util.function.Consumer<String> listener) {
        // Create a TranscriptListener from the consumer
        TranscriptListener<String> transcriptListener = new TranscriptListener<String>() {
            @Override
            public void onTranscript(String value) {
                listener.accept(value);
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public void onClose(int statusCode, String reason) {
            }
        };

        // Use the URI + TranscriptListener version which sets up handlers
        return listen(uri, transcriptListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TranscriptionSession listen(TranscriptListener<String> listener) {
        this.listener = listener;
        return createSession();
    }

    private TranscriptionSession createSession() {
        return new TranscriptionSession() {

            @Override
            public boolean isActive() {
                return listener != null && (mockClient == null || mockClient.isConnected());
            }

            @Override
            public void close() {
                if (listener != null && mockClient != null) {
                    mockClient.simulateClose(1000, "Closed by client");
                } else if (listener != null) {
                    listener.onClose(1000, "Closed by client");
                }
            }
        };
    }

    /**
     * Simulates receiving a transcript message.
     * <p>
     * This method is used for testing purposes to simulate
     * incoming transcript messages without requiring a real WebSocket connection.
     *
     * @param message the transcript message to simulate
     * @throws IllegalStateException if no listener is registered
     */
    public void simulateTranscript(String message) {
        if (listener == null) {
            throw new IllegalStateException("No listener registered. Call listen() first.");
        }
        listener.onTranscript(message);
    }

    /**
     * Simulates an error occurring in the transcript provider.
     *
     * @param error the error to simulate
     * @throws IllegalStateException if no listener is registered
     */
    public void simulateError(Throwable error) {
        if (listener == null) {
            throw new IllegalStateException("No listener registered. Call listen() first.");
        }
        listener.onError(error);
    }

    /**
     * Simulates the connection being closed.
     *
     * @param statusCode the close status code
     * @param reason the close reason
     * @throws IllegalStateException if no listener is registered
     */
    public void simulateClose(int statusCode, String reason) {
        if (listener == null) {
            throw new IllegalStateException("No listener registered. Call listen() first.");
        }
        listener.onClose(statusCode, reason);
    }
}