package io.github.bytosphere.core;

/**
 * Interface that defines listeners for transcript events.
 */
public interface TranscriptListener<T> {

    void onTranscript(T value);

    void onError(Throwable error);

    void onClose();
}
