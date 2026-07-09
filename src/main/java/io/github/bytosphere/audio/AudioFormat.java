package io.github.bytosphere.audio;

/**
 * Defines the format of an audio stream.
 */
public record AudioFormat(int sampleRate, int channels, int bitDepth) {

    public static AudioFormat canonical() {
        return new AudioFormat(16000, 1, 16);
    }
}
