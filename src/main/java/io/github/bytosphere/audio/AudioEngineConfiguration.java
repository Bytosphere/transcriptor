package io.github.bytosphere.audio;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

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
 * AudioEngineConfiguration config = AudioEngineConfiguration.builder().build();
 *
 * // Use custom configuration
 * AudioEngineConfiguration config = AudioEngineConfiguration.builder()
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
public class AudioEngineConfiguration {

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