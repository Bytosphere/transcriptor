package io.github.bytosphere.provider;

import io.github.bytosphere.core.TranscriptListener;
import io.github.bytosphere.core.TranscriptionSession;

import java.util.function.Consumer;

/**
 * Defines an interface for external services that provide transcription capabilities.
 * <p>
 * A TranscriptProvider connects to a transcription service (e.g., speech-to-text API)
 * and delivers transcribed data through a {@link TranscriptListener} callback interface.
 * The provider manages the connection lifecycle and handles reconnection, error handling,
 * and resource cleanup.
 * <p>
 * Implementations should be async-first, returning {@link TranscriptionSession} objects
 * that allow callers to control the transcription session (cancel, check status, etc.).
 *
 * @param <T> the type of transcript data produced by this provider
 * @see TranscriptListener
 * @see TranscriptionSession
 */
public interface TranscriptProvider<T> {

    /**
     * Starts listening for transcripts and delivers them to the provided listener.
     * <p>
     * This method initiates a connection to the transcription service. The returned
     * {@link TranscriptionSession} can be used to control the session (cancel, check status).
     * The listener will be called asynchronously as transcriptions are received.
     *
     * @param listener the callback to receive transcript events
     * @return a TranscriptionSession representing the active connection
     */
    TranscriptionSession listen(TranscriptListener<T> listener);

    /**
     * Convenience method to listen for transcripts using a simple consumer callback.
     * <p>
     * This overload accepts a {@link Consumer} instead of a full {@link TranscriptListener},
     * which is useful when you only care about receiving transcript values and not
     * error or close events. Error and close events are silently ignored in this convenience
     * method.
     *
     * @param listener the consumer to receive transcript values
     * @return a TranscriptionSession representing the active connection
     */
    default TranscriptionSession listen(Consumer<T> listener) {
        return listen(new TranscriptListener<T>() {

            @Override
            public void onTranscript(T value) {
                listener.accept(value);
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public void onClose(int statusCode, String reason) {
            }
        });
    }
}
