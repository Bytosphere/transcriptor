package io.github.bytosphere.core;

import io.github.bytosphere.net.WebSocketConnection;
import lombok.NonNull;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;

/**
 * A transcription session backed by a WebSocket connection.
 * <p>
 * This implementation wraps a {@link io.github.bytosphere.net.WebSocketConnection} and
 * provides the transcription session interface for sending and receiving audio/transcript
 * data over WebSocket. It handles both text and binary message transmission.
 * <p>
 * The session is considered active while the underlying WebSocket connection is connected.
 * Use {@link #close()} to terminate the session.
 *
 * @see TranscriptionSession
 * @see io.github.bytosphere.net.WebSocketConnection
 */
public class WebSocketTranscriptionSession implements TranscriptionSession {

    private final WebSocketConnection connection;

    /**
     * Creates a new WebSocket transcription session with the given connection.
     *
     * @param connection the underlying WebSocket connection to use
     * @throws NullPointerException if connection is null
     */
    public WebSocketTranscriptionSession(WebSocketConnection connection) {
        this.connection = connection;
    }

    /**
     * Sends a text message over the WebSocket connection.
     *
     * @param message the text message to send
     * @throws IllegalStateException if the connection is not open
     */
    public void sendText(String message) {
        connection.sendText(message);
    }

    /**
     * Sends binary data over the WebSocket connection.
     * <p>
     * The byte array is wrapped in a {@link ByteBuffer} before sending.
     *
     * @param data the binary data to send
     * @throws IllegalStateException if the connection is not open
     */
    public void sendBinary(byte[] data) {
        sendBinary(ByteBuffer.wrap(data));
    }

    /**
     * Sends binary data over the WebSocket connection.
     *
     * @param data the binary data to send
     * @throws IllegalStateException if the connection is not open
     */
    public void sendBinary(ByteBuffer data) {
        connection.sendBinary(data);
    }

    /**
     * Checks if this session is currently active.
     *
     * @return true if the underlying WebSocket connection is open, false otherwise
     */
    @Override
    public boolean isActive() {
        return connection.isConnected();
    }

    /**
     * Closes the WebSocket connection with a custom status code and reason.
     *
     * @param statusCode the WebSocket close status code
     * @param reason the reason for closing
     */
    public void close(int statusCode, @NonNull String reason) {
        connection.close(statusCode, reason);
    }

    /**
     * Closes the transcription session.
     */
    @Override
    public void close() {
        connection.close(WebSocket.NORMAL_CLOSURE, "");
    }
}
