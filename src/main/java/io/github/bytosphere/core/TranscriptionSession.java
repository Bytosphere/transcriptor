package io.github.bytosphere.core;

/**
 * Represents a session for transcription from some `TranscriptProvider`.
 */
public interface TranscriptionSession extends AutoCloseable {

    /**
     * Returns whether the session is active.
     */
    boolean isActive();

    /**
     * Closes this transcription session and releases any resources.
     * <p>
     * This is a default implementation that does nothing.
     * Implementations should override this method to perform cleanup.
     */
    @Override
    default void close() {
        // Default implementation, no-op.
    }
}
