package io.github.bytosphere.audio;

/**
 * Defines the format of an audio stream.
 * <p>
 * This record encapsulates the three fundamental properties of an audio format:
 * sample rate, number of channels, and bit depth. These properties are used by
 * the {@link AudioEngine} to properly process audio data.
 *
 * @see AudioEngine
 * @see AudioFrame
 * @see AudioChunk
 */
public record AudioFormat(int sampleRate, int channels, int bitDepth) {

    /**
     * Returns a canonical audio format suitable for speech recognition.
     * <p>
     * The canonical format is:
     * <ul>
     *   <li>Sample rate: 16000 Hz (16 kHz)</li>
     *   <li>Channels: 1 (mono)</li>
     *   <li>Bit depth: 16 bits</li>
     * </ul>
     *
     * @return the canonical audio format
     */
    public static AudioFormat canonical() {
        return new AudioFormat(16000, 1, 16);
    }
}
