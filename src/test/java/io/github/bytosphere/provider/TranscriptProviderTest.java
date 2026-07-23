package io.github.bytosphere.provider;

import io.github.bytosphere.core.TranscriptListener;
import io.github.bytosphere.core.TranscriptionSession;
import io.github.bytosphere.mock.MockTranscriptProvider;
import io.github.bytosphere.mock.MockWebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TranscriptProviderTest {

    private MockWebSocketClient mockWebSocketClient;
    private MockTranscriptProvider transcriptProvider;

    @BeforeEach
    void setUp() {
        mockWebSocketClient = new MockWebSocketClient();
        transcriptProvider = new MockTranscriptProvider(mockWebSocketClient);
    }

    @Test
    @DisplayName("Test connecting to a WebSocket and receiving transcript")
    void testConnectAndReceiveTranscript() {
        // Arrange
        List<String> transcripts = new ArrayList<>();
        AtomicBoolean connected = new AtomicBoolean(false);

        // Act
        URI uri = URI.create("ws://localhost:8080/transcript");
        TranscriptionSession session = transcriptProvider.listen(uri, new TranscriptListener<String>() {
            @Override
            public void onTranscript(String value) {
                transcripts.add(value);
            }

            @Override
            public void onError(Throwable error) {
                fail("Unexpected error: " + error.getMessage());
            }

            @Override
            public void onClose(int statusCode, String reason) {
            }
        });

        // Connect and simulate messages
        mockWebSocketClient.connect(uri.toString());
        mockWebSocketClient.simulateTextMessage("Hello, this is a test transcript.");
        mockWebSocketClient.simulateTextMessage("This is a second message.");

        // Assert
        assertEquals(2, transcripts.size());
        assertEquals("Hello, this is a test transcript.", transcripts.get(0));
        assertEquals("This is a second message.", transcripts.get(1));
    }

    @Test
    @DisplayName("Test handling WebSocket error")
    void testHandleError() {
        // Arrange
        AtomicBoolean errorReceived = new AtomicBoolean(false);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Act
        URI uri = URI.create("ws://localhost:8080/transcript");
        TranscriptionSession session = transcriptProvider.listen(uri, new TranscriptListener<String>() {
            @Override
            public void onTranscript(String value) {
                fail("Unexpected transcript: " + value);
            }

            @Override
            public void onError(Throwable error) {
                errorReceived.set(true);
                errorCount.incrementAndGet();
            }

            @Override
            public void onClose(int statusCode, String reason) {
            }
        });

        // Connect and simulate error
        mockWebSocketClient.connect(uri.toString());
        mockWebSocketClient.simulateError(new RuntimeException("Connection lost"));

        // Assert
        assertTrue(errorReceived.get());
        assertEquals(1, errorCount.get());
    }

    @Test
    @DisplayName("Test disconnecting from WebSocket")
    void testDisconnect() {
        // Arrange
        AtomicBoolean closeReceived = new AtomicBoolean(false);
        AtomicInteger closeCount = new AtomicInteger(0);

        // Act
        URI uri = URI.create("ws://localhost:8080/transcript");
        TranscriptionSession session = transcriptProvider.listen(uri, new TranscriptListener<String>() {
            @Override
            public void onTranscript(String value) {
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public void onClose(int statusCode, String reason) {
                closeReceived.set(true);
                closeCount.incrementAndGet();
            }
        });

        // Connect then disconnect
        mockWebSocketClient.connect(uri.toString());
        assertTrue(mockWebSocketClient.isConnected());

        session.close();

        // Assert
        assertFalse(mockWebSocketClient.isConnected());
        assertTrue(closeReceived.get());
        assertEquals(1, closeCount.get());
    }

    @Test
    @DisplayName("Test session is active when connected")
    void testSessionIsActive() {
        // Arrange & Act
        URI uri = URI.create("ws://localhost:8080/transcript");
        TranscriptionSession session = transcriptProvider.listen(uri, new TranscriptListener<String>() {
            @Override
            public void onTranscript(String value) {
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public void onClose(int statusCode, String reason) {
            }
        });

        // Connect
        mockWebSocketClient.connect(uri.toString());

        // Assert
        assertTrue(session.isActive());
    }

    @Test
    @DisplayName("Test session is not active after disconnect")
    void testSessionNotActiveAfterDisconnect() {
        // Arrange
        URI uri = URI.create("ws://localhost:8080/transcript");
        TranscriptionSession session = transcriptProvider.listen(uri, new TranscriptListener<String>() {
            @Override
            public void onTranscript(String value) {
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public void onClose(int statusCode, String reason) {
            }
        });

        // Connect then disconnect
        mockWebSocketClient.connect(uri.toString());
        assertTrue(session.isActive());

        session.close();

        // Assert
        assertFalse(session.isActive());
    }

    @Test
    @DisplayName("Test using convenience method with Consumer")
    void testListenWithConsumer() {
        // Arrange
        List<String> transcripts = new ArrayList<>();

        // Act
        URI uri = URI.create("ws://localhost:8080/transcript");
        TranscriptionSession session = transcriptProvider.listen(uri, transcripts::add);

        // Connect and simulate messages
        mockWebSocketClient.connect(uri.toString());
        mockWebSocketClient.simulateTextMessage("First message");
        mockWebSocketClient.simulateTextMessage("Second message");

        session.close();

        // Assert
        assertEquals(2, transcripts.size());
        assertEquals("First message", transcripts.get(0));
        assertEquals("Second message", transcripts.get(1));
    }

    @Test
    @DisplayName("Test multiple sessions can be created")
    void testMultipleSessions() {
        // Arrange
        List<String> session1Transcripts = new ArrayList<>();
        List<String> session2Transcripts = new ArrayList<>();

        // Act
        URI uri1 = URI.create("ws://localhost:8080/transcript1");
        URI uri2 = URI.create("ws://localhost:8080/transcript2");

        // Create mock clients for each session
        MockWebSocketClient mockClient1 = new MockWebSocketClient();
        MockWebSocketClient mockClient2 = new MockWebSocketClient();

        MockTranscriptProvider provider1 = new MockTranscriptProvider(mockClient1);
        MockTranscriptProvider provider2 = new MockTranscriptProvider(mockClient2);

        TranscriptionSession session1 = provider1.listen(uri1, session1Transcripts::add);
        TranscriptionSession session2 = provider2.listen(uri2, session2Transcripts::add);

        // Simulate messages
        mockClient1.connect(uri1.toString());
        mockClient1.simulateTextMessage("Session 1 message");

        mockClient2.connect(uri2.toString());
        mockClient2.simulateTextMessage("Session 2 message");

        session1.close();
        session2.close();

        // Assert
        assertEquals(1, session1Transcripts.size());
        assertEquals("Session 1 message", session1Transcripts.getFirst());
        assertEquals(1, session2Transcripts.size());
        assertEquals("Session 2 message", session2Transcripts.getFirst());
    }

    @Test
    @DisplayName("Test close method calls disconnect")
    void testCloseCallsDisconnect() {
        // Arrange
        AtomicBoolean closed = new AtomicBoolean(false);

        // Act
        URI uri = URI.create("ws://localhost:8080/transcript");
        TranscriptionSession session = transcriptProvider.listen(uri, new TranscriptListener<String>() {
            @Override
            public void onTranscript(String value) {
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public void onClose(int statusCode, String reason) {
                closed.set(true);
            }
        });

        mockWebSocketClient.connect(uri.toString());
        assertTrue(mockWebSocketClient.isConnected());

        session.close();

        // Assert
        assertFalse(mockWebSocketClient.isConnected());
        assertTrue(closed.get());
    }

    @Test
    @DisplayName("Test simulateTranscript method for testing")
    void testSimulateTranscript() {
        // Arrange
        List<String> transcripts = new ArrayList<>();
        URI uri = URI.create("ws://localhost:8080/transcript");

        // Act - Use listen with Consumer to register handler
        TranscriptionSession session = transcriptProvider.listen(uri, transcripts::add);
        transcriptProvider.simulateTranscript("Simulated message 1");
        transcriptProvider.simulateTranscript("Simulated message 2");

        session.close();

        // Assert
        assertEquals(2, transcripts.size());
        assertEquals("Simulated message 1", transcripts.get(0));
        assertEquals("Simulated message 2", transcripts.get(1));
    }
}