package io.github.bytosphere.audio;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * A processor that prepares audio for transmission.
 * <p>
 * The AudioEngine acts as a buffer between audio frame input and chunk output.
 * It accumulates incoming {@link AudioFrame} data until there is enough to
 * produce a complete {@link AudioChunk} for transmission.
 * <p>
 * Usage pattern:
 * <pre>{@code
 * // Create a configuration with default settings
 * AudioEngineConfiguration config = AudioEngineConfiguration.builder().build();
 *
 * // Or with custom settings
 * AudioEngineConfiguration config = AudioEngineConfiguration.builder()
 *     .audioFormat(new AudioFormat(16000, 1, 16))
 *     .chunkSize(4096)
 *     .build();
 *
 * AudioEngine engine = new AudioEngine(config);
 *
 * // Feed audio frames to the engine
 * engine.consume(new AudioFrame(audioData));
 *
 * // Try to produce a chunk when enough data is accumulated
 * Optional<AudioChunk> chunk = engine.produce();
 * if (chunk.isPresent()) {
 *     // Send chunk over the network
 * }
 * }</pre>
 *
 * @see AudioFormat
 * @see AudioFrame
 * @see AudioChunk
 * @see AudioEngineConfiguration
 */
public class AudioEngine {

    private final AudioFormat audioFormat;

    /**
     * The size of each audio chunk in bytes.
     * This is the amount of audio data required to produce one chunk.
     */
    private final int chunkSize;

    private final ByteBuffer buffer;

    /**
     * Creates a new AudioEngine with the specified configuration.
     *
     * @param configuration the audio engine configuration containing format and chunk size
     */
    public AudioEngine(AudioEngineConfiguration configuration) {
        this.audioFormat = configuration.getAudioFormat();
        this.chunkSize = configuration.getChunkSize();
        this.buffer = ByteBuffer.allocateDirect(chunkSize * audioFormat.channels());
    }

    /**
     * Consumes an audio frame and prepares it for transmission.
     * <p>
     * The frame data is added to the internal buffer. If the frame data exceeds
     * the available buffer space, only the portion that fits is written and the
     * excess data is dropped.
     *
     * @param audioFrame the audio frame to consume
     */
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

    /**
     * Produces an audio chunk if there is enough data in the buffer.
     * <p>
     * This method checks if the buffer contains at least the configured chunk size of data.
     * If so, it creates an AudioChunk and returns it wrapped in an Optional.
     * If not, it returns an empty Optional.
     * <p>
     * After producing a chunk, the buffer is compacted to remove the consumed data,
     * leaving any remaining data for the next chunk.
     *
     * @return an Optional containing the AudioChunk if enough data is available,
     *         or an empty Optional if more data needs to be accumulated
     */
    public Optional<AudioChunk> produce() {
        if (buffer.position() >= chunkSize) {
            AudioChunk chunk = new AudioChunk(chunkSize);
            buffer.flip();
            buffer.get(chunk.getData());
            buffer.compact();
            return Optional.of(chunk);
        }
        return Optional.empty();
    }

    /**
     * Returns the current amount of data in the buffer.
     *
     * @return the number of bytes currently in the buffer
     */
    public int getCurrentBufferSize() {
        return buffer.position();
    }
}
