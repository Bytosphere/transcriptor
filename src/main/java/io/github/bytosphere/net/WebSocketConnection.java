package io.github.bytosphere.net;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * Represents a single active WebSocket connection.
 * <p>
 * This class wraps a {@link WebSocket} from the java.net.http package and
 * provides a simplified interface for sending messages and managing the
 * connection lifecycle. It tracks the connection state and provides callbacks
 * for close events.
 * <p>
 * Instances are typically created by {@link WebSocketClient} when connecting
 * to a server. The onClose callback is invoked when the connection is closed
 * to allow cleanup of resources.
 *
 * @see WebSocketClient
 * @see io.github.bytosphere.core.WebSocketTranscriptionSession
 */
public class WebSocketConnection {

    private final WebSocket webSocket;
    private final Consumer<WebSocketConnection> onClose;

    /**
     * Creates a new WebSocket connection with the given underlying WebSocket and close callback.
     *
     * @param webSocket the underlying java.net.http WebSocket
     * @param onClose callback invoked when the connection is closed, receives this connection
     */
    public WebSocketConnection(WebSocket webSocket, Consumer<WebSocketConnection> onClose) {
        this.webSocket = webSocket;
        this.onClose = onClose;
    }

    /**
     * Sends a text message over the WebSocket connection.
     *
     * @param message the text message to send
     * @throws IllegalStateException if the connection is not open
     */
    public void sendText(String message) {
        if (!isConnected())
            throw new IllegalStateException("WebSocket connection is not open");
        webSocket.sendText(message, true).join();
    }

    /**
     * Sends binary data over the WebSocket connection.
     *
     * @param data the binary data to send
     * @throws IllegalStateException if the connection is not open
     */
    public void sendBinary(ByteBuffer data) {
        if (!isConnected())
            throw new IllegalStateException("WebSocket connection is not open");
        webSocket.sendBinary(data, true).join();
    }

    /**
     * Checks if the WebSocket connection is open.
     *
     * @return true if the underlying WebSocket exists, false otherwise
     */
    public boolean isConnected() {
        return webSocket != null;
    }

    /**
     * Closes the WebSocket connection with the given status code and reason.
     * <p>
     * This sends a close frame to the server and invokes the onClose callback
     * to allow cleanup of resources.
     *
     * @param statusCode the WebSocket close status code
     * @param reason the reason for closing, may be null
     */
    public void close(int statusCode, String reason) {
        webSocket.sendClose(statusCode, reason);
        onClose.accept(this);
    }
}
