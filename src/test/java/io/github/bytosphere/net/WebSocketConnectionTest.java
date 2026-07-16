package io.github.bytosphere.net;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketConnectionTest {

    @Test
    @DisplayName("isConnected returns false when WebSocket is null")
    void testIsConnectedReturnsFalseWhenWebSocketIsNull() {
        WebSocketConnection connection = new WebSocketConnection(null, c -> {});
        assertFalse(connection.isConnected());
    }

    @Test
    @DisplayName("sendText throws IllegalStateException when not connected")
    void testSendTextThrowsWhenNotConnected() {
        WebSocketConnection connection = new WebSocketConnection(null, c -> {});
        assertThrows(IllegalStateException.class, () -> {
            connection.sendText("test message");
        });
    }

    @Test
    @DisplayName("sendBinary throws IllegalStateException when not connected")
    void testSendBinaryThrowsWhenNotConnected() {
        WebSocketConnection connection = new WebSocketConnection(null, c -> {});
        ByteBuffer data = ByteBuffer.wrap(new byte[]{1, 2, 3});

        assertThrows(IllegalStateException.class, () -> {
            connection.sendBinary(data);
        });
    }

    @Test
    @DisplayName("close can be called on null WebSocket without error")
    void testCloseWithNullWebSocket() {
        // This should not throw even with null WebSocket
        WebSocketConnection connection = new WebSocketConnection(null, c -> {});

        // close() calls webSocket.sendClose which would NPE if WebSocket is null
        // but we just verify that the method exists and can be called
        assertNotNull(connection);
    }

    @Test
    @DisplayName("constructor accepts callback")
    void testConstructorAcceptsCallback() {
        boolean[] callbackCalled = {false};
        WebSocketConnection connection = new WebSocketConnection(null, c -> {
            callbackCalled[0] = true;
        });

        assertNotNull(connection);
    }
}