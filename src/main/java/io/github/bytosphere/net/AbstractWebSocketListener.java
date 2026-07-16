package io.github.bytosphere.net;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

/**
 * Minimal {@link WebSocket.Listener} base class shared by all transcript
 * providers. Handles only what every provider needs regardless of what it
 * does with the result: reassembling fragmented text/binary frames and
 * requesting the next frame (backpressure).
 *
 * <p>Lifecycle hooks ({@link #onOpen}, {@link #onClose},
 * {@link #onError}) default to no-ops — subclasses decide whether to
 * publish an event, log, reconnect, etc. This class has no opinion on
 * event publishing.
 */
public abstract class AbstractWebSocketListener {

    private final WebSocket.Listener listener;

    public AbstractWebSocketListener() {
        this.listener = new InternalWebSocketListener();
    }

    /**
     * Returns the wrapped {@link WebSocket.Listener} instance.
     */
    public WebSocket.Listener asWebSocketListener() {
        return listener;
    }

    /**
     * Called once a complete text message has been reassembled from frames.
     */
    protected abstract void onMessage(String message);

    /**
     * Called once a complete binary message has been reassembled from frames. Default: ignore.
     */
    protected void onBinaryMessage(ByteBuffer message) {
        // most providers are text/JSON only; override if a provider sends binary frames
    }

    /**
     * Called on connection open, before backpressure is requested. Default: no-op.
     */
    protected void onOpen(WebSocket webSocket) {
    }

    /**
     * Called on connection close. Default: no-op.
     */
    protected void onClose(int statusCode, String reason) {
    }

    /**
     * Called on transport error. Default: no-op.
     */
    protected void onError(Throwable error) {
    }

    /** An internal listener that reassembles text/binary frames. */
    private final class InternalWebSocketListener implements WebSocket.Listener {

        private final StringBuilder textBuffer = new StringBuilder();
        private ByteArrayAccumulator binaryBuffer;

        @Override
        public void onOpen(WebSocket webSocket) {
            AbstractWebSocketListener.this.onOpen(webSocket);
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                String complete = textBuffer.toString();
                textBuffer.setLength(0);
                AbstractWebSocketListener.this.onMessage(complete);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            if (binaryBuffer == null) {
                binaryBuffer = new ByteArrayAccumulator();
            }
            binaryBuffer.append(data);
            if (last) {
                ByteBuffer complete = ByteBuffer.wrap(binaryBuffer.toByteArrayAndReset());
                binaryBuffer = null;
                AbstractWebSocketListener.this.onBinaryMessage(complete);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            AbstractWebSocketListener.this.onClose(statusCode, reason);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            AbstractWebSocketListener.this.onError(error);
        }
    }

    /**
     * Small growable byte accumulator that avoids pulling in an extra dependency for this.
     */
    private static final class ByteArrayAccumulator {

        private byte[] buf = new byte[1024];
        private int size = 0;

        void append(ByteBuffer data) {
            int needed = size + data.remaining();
            if (needed > buf.length) {
                byte[] grown = new byte[Math.max(needed, buf.length * 2)];
                System.arraycopy(buf, 0, grown, 0, size);
                buf = grown;
            }
            data.get(buf, size, data.remaining());
            size = needed;
        }

        byte[] toByteArrayAndReset() {
            byte[] result = new byte[size];
            System.arraycopy(buf, 0, result, 0, size);
            size = 0;
            return result;
        }
    }
}
