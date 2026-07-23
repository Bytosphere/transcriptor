package io.github.bytosphere.core;

import io.github.bytosphere.net.WebSocketConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketTranscriptionSessionTest {

    private TestWebSocketConnection mockConnection;
    private WebSocketTranscriptionSession session;

    @BeforeEach
    void setUp() {
        mockConnection = new TestWebSocketConnection();
        session = new WebSocketTranscriptionSession(mockConnection);
    }

    @Test
    @DisplayName("constructor accepts null connection")
    void testConstructorAcceptsNull() {
        WebSocketTranscriptionSession nullSession = new WebSocketTranscriptionSession(null);
        assertNotNull(nullSession);
    }

    @Test
    @DisplayName("isActive returns true when connection is connected")
    void testIsActiveReturnsTrueWhenConnected() {
        mockConnection.connected = true;
        assertTrue(session.isActive());
    }

    @Test
    @DisplayName("isActive returns false when connection is not connected")
    void testIsActiveReturnsFalseWhenNotConnected() {
        mockConnection.connected = false;
        assertFalse(session.isActive());
    }

    @Test
    @DisplayName("sendText delegates to connection")
    void testSendTextDelegatesToConnection() {
        session.sendText("Hello, World!");

        assertEquals("Hello, World!", mockConnection.lastTextMessage);
    }

    @Test
    @DisplayName("sendBinary byte array delegates to connection")
    void testSendBinaryByteArrayDelegatesToConnection() {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        session.sendBinary(data);

        assertNotNull(mockConnection.lastBinaryData);
    }

    @Test
    @DisplayName("sendBinary ByteBuffer delegates to connection")
    void testSendBinaryByteBufferDelegatesToConnection() {
        ByteBuffer data = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        session.sendBinary(data);

        assertNotNull(mockConnection.lastBinaryData);
    }

    @Test
    @DisplayName("close closes connection with normal closure and empty reason")
    void testCloseClosesConnectionWithNormalClosureAndEmptyReason() {
        session.close();

        assertEquals(WebSocket.NORMAL_CLOSURE, mockConnection.lastCloseStatusCode);
        assertEquals("", mockConnection.lastCloseReason);
    }

    @Test
    @DisplayName("close makes session inactive")
    void testCloseMakesSessionInactive() {
        mockConnection.connected = true;
        assertTrue(session.isActive());

        session.close();

        assertFalse(session.isActive());
    }

    @Test
    @DisplayName("multiple close calls are idempotent")
    void testMultipleCloseCallsAreIdempotent() {
        session.close();
        session.close();
        session.close();

        // Should only call close once (the connection is closed on first call)
        // In this test implementation, we just verify it doesn't throw
        assertFalse(mockConnection.connected);
    }

    /**
     * Test implementation of WebSocketConnection for testing.
     */
    private static class TestWebSocketConnection extends WebSocketConnection {

        boolean connected = true;
        String lastTextMessage = null;
        ByteBuffer lastBinaryData = null;
        int lastCloseStatusCode = -1;
        String lastCloseReason = null;

        TestWebSocketConnection() {
            super(null, c -> {});
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public void sendText(String message) {
            if (!isConnected()) {
                throw new IllegalStateException("WebSocket connection is not open");
            }
            lastTextMessage = message;
        }

        @Override
        public void sendBinary(ByteBuffer data) {
            if (!isConnected()) {
                throw new IllegalStateException("WebSocket connection is not open");
            }
            lastBinaryData = data.duplicate();
        }

        @Override
        public void close(int statusCode, String reason) {
            lastCloseStatusCode = statusCode;
            lastCloseReason = reason;
            connected = false;
        }
    }
}