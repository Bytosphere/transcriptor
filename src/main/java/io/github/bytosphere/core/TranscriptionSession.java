package io.github.bytosphere.core;

/**
 * Represents a session for transcription from some `TranscriptProvider`.
 */
public interface TranscriptionSession extends AutoCloseable {

    /**
     * Disconnects the current session.
     */
    void disconnect();

    /**
     * Returns whether the session is active.
     */
    boolean isActive();

    @Override
    default void close() {
        disconnect();
    }
}
