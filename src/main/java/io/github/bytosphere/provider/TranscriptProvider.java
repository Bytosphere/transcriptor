package io.github.bytosphere.provider;

import io.github.bytosphere.core.TranscriptListener;
import io.github.bytosphere.core.TranscriptionSession;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Represents an external service that provides transcripts.
 */
public interface TranscriptProvider<T> {

    /**
     * Deserializes the transcript into the target type.
     */
    TranscriptionSession listen(URI url, TranscriptListener<T> listener);

    /**
     * Convenience method to listen for transcripts with a callback for transcript exclusively.
     */
    default TranscriptionSession listen(URI url, Consumer<T> listener) {
        return listen(url, new TranscriptListener<T>() {

            @Override
            public void onTranscript(T value) {
                listener.accept(value);
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public void onClose() {
            }
        });
    }
}
