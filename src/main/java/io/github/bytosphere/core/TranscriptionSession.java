package io.github.bytosphere.core;

/**
 * Represents a session for transcription from some `TranscriptProvider`.
 */
public interface TranscriptionSession extends AutoCloseable {

    /**
     * A user-requested cancellation of the transcription session.
     */
    void cancel();

    /**
     * Returns whether the session is active.
     */
    boolean isActive();

    @Override
    default void close() {
        cancel();
    }
}
