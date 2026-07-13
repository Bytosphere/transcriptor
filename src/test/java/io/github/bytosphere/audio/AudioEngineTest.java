package io.github.bytosphere.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AudioEngineTest {

    private AudioEngine audioEngine;

    @BeforeEach
    void setUp() {
        AudioEngine.Configuration config = AudioEngine.Configuration.builder().build();
        audioEngine = new AudioEngine(config);
    }

    @Test
    @DisplayName("Test creating AudioEngine with default configuration")
    void testCreateAudioEngineWithDefaultFormat() {
        // Act
        AudioEngine.Configuration config = AudioEngine.Configuration.builder().build();
        AudioEngine engine = new AudioEngine(config);

        // Assert
        assertNotNull(engine);
        assertFalse(engine.hasNextChunk());
    }

    @Test
    @DisplayName("Test creating AudioEngine with stereo format")
    void testCreateAudioEngineWithStereoFormat() {
        // Arrange
        AudioFormat stereoFormat = new AudioFormat(44100, 2, 16);
        AudioEngine.Configuration config = AudioEngine.Configuration.builder()
            .audioFormat(stereoFormat)
            .build();

        // Act
        AudioEngine engine = new AudioEngine(config);

        // Assert
        assertNotNull(engine);
        assertFalse(engine.hasNextChunk());
    }

    @Test
    @DisplayName("Test consume increases buffer size")
    void testConsumeIncreasesBufferSize() {
        // Arrange
        byte[] frameData = new byte[1024];
        AudioFrame frame = new AudioFrame(frameData);

        // Act
        audioEngine.consume(frame);

        // Assert
        assertEquals(1024, audioEngine.getCurrentBufferSize());
    }

    @Test
    @DisplayName("Test multiple consume calls accumulate data")
    void testMultipleConsumeCallsAccumulateData() {
        // Arrange
        byte[] frameData1 = new byte[512];
        byte[] frameData2 = new byte[512];
        byte[] frameData3 = new byte[256];

        // Act
        audioEngine.consume(new AudioFrame(frameData1));
        audioEngine.consume(new AudioFrame(frameData2));
        audioEngine.consume(new AudioFrame(frameData3));

        // Assert
        assertEquals(1280, audioEngine.getCurrentBufferSize());
    }

    @Test
    @DisplayName("Test hasNextChunk returns false on empty buffer")
    void testHasNextChunkReturnsFalseOnEmptyBuffer() {
        // Act
        boolean hasChunk = audioEngine.hasNextChunk();

        // Assert
        assertFalse(hasChunk);
    }

    @Test
    @DisplayName("Test nextChunk returns empty when no chunks available")
    void testNextChunkReturnsEmptyWhenNoChunksAvailable() {
        // Arrange
        byte[] frameData = new byte[1024];
        AudioFrame frame = new AudioFrame(frameData);
        audioEngine.consume(frame);

        // Act
        Optional<AudioChunk> result = audioEngine.nextChunk();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test fromFile throws IllegalArgumentException for non-WAV file")
    void testFromFileThrowsExceptionForNonWavFile() {
        // Arrange
        AudioEngine.Configuration config = AudioEngine.Configuration.builder().build();
        Path mp3Path = Path.of("test.mp3");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            AudioEngine.fromFile(mp3Path, config);
        });
    }

    @Test
    @DisplayName("Test fromFile throws exception for non-existent WAV file")
    void testFromFileThrowsExceptionForNonExistentFile() {
        // Arrange
        AudioEngine.Configuration config = AudioEngine.Configuration.builder().build();
        Path nonExistentPath = Path.of("test_resources", "non_existent_file.wav");

        // Act & Assert
        // First checks extension, then tries to read file
        Exception exception = assertThrows(Exception.class, () -> {
            AudioEngine.fromFile(nonExistentPath, config);
        });
        // Either IllegalArgumentException (extension check) or IOException (file read)
        assertTrue(exception instanceof IllegalArgumentException ||
                    exception instanceof IOException);
    }
}