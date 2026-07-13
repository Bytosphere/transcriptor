package io.github.bytosphere.audio;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

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
 * AudioEngine.Configuration config = AudioEngine.Configuration.builder().build();
 *
 * // Or with custom settings
 * AudioEngine.Configuration config = AudioEngine.Configuration.builder()
 *     .audioFormat(new AudioFormat(16000, 1, 16))
 *     .chunkSize(4096)
 *     .build();
 *
 * AudioEngine engine = new AudioEngine(config);
 *
 * // Feed audio frames to the engine
 * engine.consume(new AudioFrame(audioData));
 *
 * // Chunks are automatically produced when the buffer fills
 * // Retrieve available chunks
 * while (engine.hasNextChunk()) {
 *     AudioChunk chunk = engine.nextChunk().get();
 *     // Send chunk over the network
 * }
 * }</pre>
 *
 * @see AudioFormat
 * @see AudioFrame
 * @see AudioChunk
 * @see AudioEngine.Configuration
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
     * A queue of produced chunks.
     */
    private final Queue<AudioChunk> chunks = new ArrayDeque<>();

    /**
     * Creates a new AudioEngine with the specified configuration.
     *
     * @param configuration the audio engine configuration containing format and chunk size
     * @see Configuration
     */
    public AudioEngine(Configuration configuration) {
        this.audioFormat = configuration.getAudioFormat();
        this.chunkSize = configuration.getChunkSize();
        this.buffer = ByteBuffer.allocateDirect(chunkSize * audioFormat.channels());
    }

    /**
     * Creates an AudioEngine and loads audio data from a file.
     * <p>
     * This convenience method creates an AudioEngine with the given configuration,
     * reads all audio data from the specified WAV file, and feeds it into the engine.
     * The data is automatically consumed and any complete chunks are produced.
     *
     * @param path         the path to the WAV audio file
     * @param configuration the audio engine configuration
     * @return a new AudioEngine with the file data loaded
     * @throws IOException          if an I/O error occurs reading the file
     * @throws IllegalArgumentException if the file is not a WAV file
     */
    public static AudioEngine fromFile(Path path, Configuration configuration) throws IOException {
        AudioEngine self = new AudioEngine(configuration);

        // Validate the path is a supported audio format.
        if (!path.endsWith(".wav"))
            throw new IllegalArgumentException("Unsupported audio format: " + path);

        byte[] data = Files.readAllBytes(path);

        // Consume the data into chunks.
        self.consume(new AudioFrame(data));

        return self;
    }

    /**
     * Consumes an audio frame and prepares it for transmission.
     * <p>
     * The frame data is added to the internal buffer. Whenever the buffer becomes
     * full, a chunk is produced and the remaining frame data continues filling
     * the buffer until all data has been consumed.
     *
     * @param audioFrame the audio frame to consume
     */
    public void consume(AudioFrame audioFrame) {
        byte[] data = audioFrame.data();
        int offset = 0;

        while (offset < data.length) {
            int bytesToWrite = Math.min(buffer.remaining(), data.length - offset);

            buffer.put(data, offset, bytesToWrite);
            offset += bytesToWrite;

            if (!buffer.hasRemaining()) {
                produce();
                buffer.clear();
            }
        }
    }

    /**
     * Retrieves and removes the next available audio chunk.
     * <p>
     * Chunks are automatically produced when the internal buffer fills up during
     * calls to {@link #consume(AudioFrame)}. This method provides access to those
     * produced chunks.
     *
     * @return an Optional containing the next AudioChunk, or empty if no chunks are available
     * @see #hasNextChunk()
     */
    public Optional<AudioChunk> nextChunk() {
        if (chunks.isEmpty())
            return Optional.empty();
        return Optional.of(chunks.remove());
    }

    /**
     * Checks if there are any produced chunks available to retrieve.
     *
     * @return true if there are chunks available, false otherwise
     * @see #nextChunk()
     */
    public boolean hasNextChunk() {
        return !chunks.isEmpty();
    }

    /**
     * Produces an audio chunk if there is enough data in the buffer.
     * <p>
     * This method is called automatically when the buffer becomes full during
     * {@link #consume(AudioFrame)}. It checks if the buffer contains at least the
     * configured chunk size of data. If so, it creates an AudioChunk and adds it
     * to the chunks queue.
     * <p>
     * After producing a chunk, the buffer is compacted to remove the consumed data,
     * leaving any remaining data for the next chunk.
     */
    private void produce() {
        if (buffer.position() >= chunkSize)
            return;
        AudioChunk chunk = new AudioChunk(chunkSize);
        buffer.flip();
        buffer.get(chunk.getData());
        buffer.compact();
        chunks.add(chunk);
    }

    /**
     * Returns the current amount of data in the buffer.
     *
     * @return the number of bytes currently in the buffer
     */
    public int getCurrentBufferSize() {
        return buffer.position();
    }

    /**
     * Configuration for the AudioEngine.
     * <p>
     * This class uses a fluent builder API to configure the AudioEngine settings.
     * All fields have sensible defaults, so you only need to specify the settings
     * you want to override.
     * <p>
     * Usage example:
     * <pre>{@code
     * // Use default configuration
     * AudioEngine.Configuration config = AudioEngine.Configuration.builder().build();
     *
     * // Use custom configuration
     * AudioEngine.Configuration config = AudioEngine.Configuration.builder()
     *     .audioFormat(new AudioFormat(44100, 2, 16))
     *     .chunkSize(8192)
     *     .build();
     *
     * AudioEngine engine = new AudioEngine(config);
     * }</pre>
     *
     * @see AudioEngine
     * @see AudioFormat
     */
    @Getter
    @Builder
    public static class Configuration {

        /**
         * The audio format used for processing.
         * Defaults to {@link AudioFormat#canonical()} (16kHz, mono, 16-bit).
         */
        @NonNull
        @Builder.Default
        private final AudioFormat audioFormat = AudioFormat.canonical();

        /**
         * The size of each audio chunk in bytes.
         * Defaults to 4096 bytes.
         */
        @Builder.Default
        private final int chunkSize = 4096;
    }
}
