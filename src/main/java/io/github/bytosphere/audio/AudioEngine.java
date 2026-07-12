package io.github.bytosphere.audio;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * A processor that prepares audio for transmission.
 */
public class AudioEngine {

    // TODO: Temporary constant, could be removed later.
    private final int CHUNK_SIZE_BYTES = 4096;

    private final AudioFormat audioFormat;

    private final ByteBuffer buffer;

    public AudioEngine(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        this.buffer = ByteBuffer.allocateDirect(CHUNK_SIZE_BYTES * audioFormat.channels());
    }

    /** Consumes an audio frame and prepares it for transmission. */
    public void consume(AudioFrame audioFrame) {
        byte[] data = audioFrame.data();
        int remaining = buffer.remaining();

        if (data.length <= remaining) {
            buffer.put(data);
        } else {
            // Only write what fits, ignore excess
            buffer.put(data, 0, remaining);
        }
    }

    /** Produces an audio chunk if there is enough data in the buffer. */
    public Optional<AudioChunk> produce() {
        if (buffer.position() >= CHUNK_SIZE_BYTES) {
            AudioChunk chunk = new AudioChunk(CHUNK_SIZE_BYTES);
            buffer.flip();
            buffer.get(chunk.getData());
            buffer.compact();
            return Optional.of(chunk);
        }
        return Optional.empty();
    }

    public int getCurrentBufferSize() {
        return buffer.position();
    }
}
