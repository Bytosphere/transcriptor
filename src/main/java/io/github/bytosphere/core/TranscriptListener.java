package io.github.bytosphere.core;

/**
 * Defines callbacks for receiving transcription events from a {@link io.github.bytosphere.provider.TranscriptProvider}.
 * <p>
 * Implementations receive transcript values as they arrive, along with error and close events.
 * This is a push-based model where the provider calls these methods to notify the listener
 * of state changes in the transcription stream.
 *
 * @param <T> the type of transcript data received
 * @see io.github.bytosphere.core.TranscriptionSession
 * @see io.github.bytosphere.provider.TranscriptProvider
 */
public interface TranscriptListener<T> {

    /**
     * Called when a complete transcript message is received.
     *
     * @param value the transcript data, typically a String or a parsed object
     */
    void onTranscript(T value);

    /**
     * Called when an error occurs during transcription.
     * <p>
     * After an error, the session may still be active but no more transcripts
     * will be received unless explicitly reconnected.
     *
     * @param error the error that occurred
     */
    void onError(Throwable error);

    /**
     * Called when the transcription session is closed.
     * <p>
     * This is typically due to the client disconnecting, a server-initiated close,
     * or the session being cancelled. No more transcripts will be received after
     * this callback.
     *
     * @param statusCode the WebSocket close status code
     * @param reason the reason for closure, may be null
     */
    void onClose(int statusCode, String reason);
}
