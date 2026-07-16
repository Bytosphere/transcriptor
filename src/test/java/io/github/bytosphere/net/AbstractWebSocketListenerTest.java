package io.github.bytosphere.net;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractWebSocketListenerTest {

    private TestWebSocketListener listener;

    @BeforeEach
    void setUp() {
        listener = new TestWebSocketListener();
    }

    @Test
    @DisplayName("asWebSocketListener returns a non-null listener")
    void testAsWebSocketListenerReturnsNonNull() {
        assertNotNull(listener.asWebSocketListener());
    }

    @Test
    @DisplayName("asWebSocketListener returns the same instance on multiple calls")
    void testAsWebSocketListenerReturnsSameInstance() {
        var l1 = listener.asWebSocketListener();
        var l2 = listener.asWebSocketListener();
        assertSame(l1, l2);
    }

    @Test
    @DisplayName("onMessage can be called multiple times with different data")
    void testOnMessageCanBeCalledMultipleTimes() {
        listener.onMessage("First");
        assertEquals("First", listener.lastMessage);

        listener.onMessage("Second");
        assertEquals("Second", listener.lastMessage);
    }

    @Test
    @DisplayName("onBinaryMessage can be called")
    void testOnBinaryMessageCanBeCalled() {
        ByteBuffer data = ByteBuffer.wrap(new byte[]{1, 2, 3});
        listener.onBinaryMessage(data);

        assertNotNull(listener.lastBinaryMessage);
    }

    @Test
    @DisplayName("onOpen can be called")
    void testOnOpenCanBeCalled() {
        assertFalse(listener.onOpenInvoked);
        listener.onOpen(null);
        assertTrue(listener.onOpenInvoked);
    }

    @Test
    @DisplayName("onClose can be called with parameters")
    void testOnCloseCanBeCalledWithParameters() {
        listener.onClose(1000, "Normal closure");

        assertEquals(1000, listener.lastCloseStatusCode);
        assertEquals("Normal closure", listener.lastCloseReason);
    }

    @Test
    @DisplayName("onError can be called with throwable")
    void testOnErrorCanBeCalledWithThrowable() {
        RuntimeException error = new RuntimeException("Test error");
        listener.onError(error);

        assertSame(error, listener.lastError);
    }

    /**
     * Test implementation of AbstractWebSocketListener for testing.
     */
    private static class TestWebSocketListener extends AbstractWebSocketListener {

        boolean onOpenInvoked = false;
        String lastMessage = null;
        ByteBuffer lastBinaryMessage = null;
        int lastCloseStatusCode = -1;
        String lastCloseReason = null;
        Throwable lastError = null;

        @Override
        protected void onMessage(String message) {
            this.lastMessage = message;
        }

        @Override
        protected void onBinaryMessage(ByteBuffer message) {
            this.lastBinaryMessage = message;
        }

        @Override
        protected void onOpen(java.net.http.WebSocket webSocket) {
            this.onOpenInvoked = true;
        }

        @Override
        protected void onClose(int statusCode, String reason) {
            this.lastCloseStatusCode = statusCode;
            this.lastCloseReason = reason;
        }

        @Override
        protected void onError(Throwable error) {
            this.lastError = error;
        }
    }
}