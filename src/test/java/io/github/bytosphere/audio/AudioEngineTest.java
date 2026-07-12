package io.github.bytosphere.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AudioEngineTest {

    private AudioEngine audioEngine;

    @BeforeEach
    void setUp() {
        AudioEngineConfiguration config = AudioEngineConfiguration.builder().build();
        audioEngine = new AudioEngine(config);
    }

    @Test
    @DisplayName("Test creating AudioEngine with default configuration")
    void testCreateAudioEngineWithDefaultFormat() {
        // Act
        AudioEngineConfiguration config = AudioEngineConfiguration.builder().build();
        AudioEngine engine = new AudioEngine(config);

        // Assert
        assertNotNull(engine);
        assertEquals(0, engine.getCurrentBufferSize());
    }

    @Test
    @DisplayName("Test creating AudioEngine with stereo format")
    void testCreateAudioEngineWithStereoFormat() {
        // Arrange
        AudioFormat stereoFormat = new AudioFormat(44100, 2, 16);
        AudioEngineConfiguration config = AudioEngineConfiguration.builder()
            .audioFormat(stereoFormat)
            .build();

        // Act
        AudioEngine engine = new AudioEngine(config);

        // Assert
        assertNotNull(engine);
        assertEquals(0, engine.getCurrentBufferSize());
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
    @DisplayName("Test produce returns empty when buffer is not full")
    void testProduceReturnsEmptyWhenBufferNotFull() {
        // Arrange
        byte[] frameData = new byte[1024];
        AudioFrame frame = new AudioFrame(frameData);
        audioEngine.consume(frame);

        // Act
        Optional<AudioChunk> result = audioEngine.produce();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test produce returns chunk when buffer is exactly full")
    void testProduceReturnsChunkWhenBufferExactlyFull() {
        // Arrange - CHUNK_SIZE_BYTES is 4096
        byte[] frameData = new byte[4096];
        AudioFrame frame = new AudioFrame(frameData);
        audioEngine.consume(frame);

        // Act
        Optional<AudioChunk> result = audioEngine.produce();

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getData());
        assertEquals(4096, result.get().getData().length);
    }

    @Test
    @DisplayName("Test produce returns chunk when buffer exactly matches chunk size")
    void testProduceReturnsChunkWhenBufferExactlyMatchesChunkSize() {
        // Arrange - CHUNK_SIZE_BYTES is 4096
        byte[] frameData = new byte[4096];
        AudioFrame frame = new AudioFrame(frameData);
        audioEngine.consume(frame);

        // Act
        Optional<AudioChunk> result = audioEngine.produce();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(4096, result.get().getData().length);
    }

    @Test
    @DisplayName("Test produce resets buffer after producing chunk")
    void testProduceResetsBufferAfterProducingChunk() {
        // Arrange
        byte[] frameData = new byte[4096];
        AudioFrame frame = new AudioFrame(frameData);
        audioEngine.consume(frame);

        // Verify initial state
        assertEquals(4096, audioEngine.getCurrentBufferSize());

        // Act
        audioEngine.produce();

        // Assert
        assertEquals(0, audioEngine.getCurrentBufferSize());
    }

    @Test
    @DisplayName("Test data exceeding buffer capacity is dropped")
    void testDataExceedingBufferCapacityIsDropped() {
        // Arrange - 5000 bytes, but buffer can only hold 4096
        byte[] frameData = new byte[5000];
        AudioFrame frame = new AudioFrame(frameData);
        audioEngine.consume(frame);

        // Verify initial state - only 4096 bytes fit
        assertEquals(4096, audioEngine.getCurrentBufferSize());

        // Act
        Optional<AudioChunk> result = audioEngine.produce();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(4096, result.get().getData().length);
        // Buffer should be empty after produce
        assertEquals(0, audioEngine.getCurrentBufferSize());
    }

    @Test
    @DisplayName("Test multiple produce calls with accumulated data")
    void testMultipleProduceCallsWithAccumulatedData() {
        // Arrange - consume 2 chunks worth of data (8192 bytes), but buffer can only hold 4096
        byte[] frameData = new byte[8192];
        AudioFrame frame = new AudioFrame(frameData);
        audioEngine.consume(frame);

        // Verify initial state - only 4096 bytes fit
        assertEquals(4096, audioEngine.getCurrentBufferSize());

        // Act - produce first chunk
        Optional<AudioChunk> result1 = audioEngine.produce();

        // Assert first produce
        assertTrue(result1.isPresent());
        assertEquals(4096, result1.get().getData().length);
        assertEquals(0, audioEngine.getCurrentBufferSize());

        // Act - produce second chunk (should be empty since we dropped excess)
        Optional<AudioChunk> result2 = audioEngine.produce();

        // Assert second produce
        assertTrue(result2.isEmpty());
    }

    @Test
    @DisplayName("Test produce after consuming more data")
    void testProduceAfterConsumingMoreData() {
        // Arrange
        byte[] frameData1 = new byte[2048];
        byte[] frameData2 = new byte[2048];
        // Total: 4096, should trigger produce

        // Act
        audioEngine.consume(new AudioFrame(frameData1));
        Optional<AudioChunk> result1 = audioEngine.produce();

        audioEngine.consume(new AudioFrame(frameData2));
        Optional<AudioChunk> result2 = audioEngine.produce();

        // Assert
        assertTrue(result1.isEmpty());
        assertTrue(result2.isPresent());
        assertEquals(4096, result2.get().getData().length);
    }

    @Test
    @DisplayName("Test consume and produce workflow")
    void testConsumeAndProduceWorkflow() {
        // Arrange - simulate a typical workflow
        byte[] smallFrame = new byte[512];

        // Act & Assert - consume 8 small frames to fill buffer
        for (int i = 0; i < 7; i++) {
            audioEngine.consume(new AudioFrame(smallFrame));
            Optional<AudioChunk> result = audioEngine.produce();
            assertTrue(result.isEmpty(), "Should not produce after frame " + (i + 1));
        }

        // After 8th frame, we should have 4096 bytes
        audioEngine.consume(new AudioFrame(smallFrame));
        Optional<AudioChunk> result = audioEngine.produce();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(4096, result.get().getData().length);
        assertEquals(0, audioEngine.getCurrentBufferSize());
    }

    @Test
    @DisplayName("Test produce on empty buffer returns empty")
    void testProduceOnEmptyBufferReturnsEmpty() {
        // Act
        Optional<AudioChunk> result = audioEngine.produce();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test buffer size with different channel configurations")
    void testBufferSizeWithDifferentChannelConfigurations() {
        // Arrange - stereo format (2 channels)
        AudioFormat stereoFormat = new AudioFormat(44100, 2, 16);
        AudioEngineConfiguration config = AudioEngineConfiguration.builder()
            .audioFormat(stereoFormat)
            .build();
        AudioEngine stereoEngine = new AudioEngine(config);

        // The buffer size should be CHUNK_SIZE_BYTES * channels = 4096 * 2 = 8192
        // But the CHUNK_SIZE_BYTES is hardcoded to 4096, not multiplied by channels

        // Let me verify the current behavior - the buffer is allocated as CHUNK_SIZE_BYTES * channels
        byte[] frameData = new byte[4096];

        // Act
        stereoEngine.consume(new AudioFrame(frameData));

        // Assert - The buffer should have 4096 bytes
        assertEquals(4096, stereoEngine.getCurrentBufferSize());

        // And produce should work with CHUNK_SIZE_BYTES (4096), not CHUNK_SIZE_BYTES * channels
        Optional<AudioChunk> result = stereoEngine.produce();

        // This test documents the current behavior
        assertTrue(result.isPresent());
        assertEquals(4096, result.get().getData().length);
    }
}