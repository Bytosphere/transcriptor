# Transcriptor

A Java library for audio processing and transcription.

## Overview

Transcriptor provides a simple provider + listener model for building real-time transcription applications. Connect to any speech-to-text service over WebSocket, process audio from various sources, and receive transcriptions through a clean callback interface.

## Features

- **Provider:** Simple callback-based API for receiving transcriptions.
- **WebSocket Support**: Built-in WebSocket client for connecting to transcription services.
- **Audio Engine**: Capture and process audio from microphone or file sources.
- **Session Management**: Control active transcription sessions (cancel, check status).
- **Concurrency Support**: Handle multiple simultaneous transcription sessions.

## Installation

Download the JAR from the [releases](https://github.com/bytosphere/transcriptor/releases) and add it to your classpath, or use with a build tool:

### Maven

```xml
<dependency>
    <groupId>io.github.bytosphere</groupId>
    <artifactId>transcriptor</artifactId>
    <version>1.0.0</version>
</xml>
```

### Gradle (Kotlin)

```kotlin
implementation("io.github.bytosphere:transcriptor:1.0.0")
```

## Quickstart

### 1. Create a `TranscriptListener`

Define how to handle transcription events:

```java
import io.github.bytosphere.core.TranscriptListener;

TranscriptListener<String> listener = new TranscriptListener<>() {
    
    @Override
    public void onTranscript(String transcript) {
        System.out.println("Received: " + transcript);
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }

    @Override
    public void onClose(int statusCode, String reason) {
        System.out.println("Closed: " + statusCode + " - " + reason);
    }
};
```

Or use a simple `Consumer` for just the transcript:

```java
import java.util.function.Consumer;

Consumer<String> consumer = transcript -> System.out.println("Received: " + transcript);
```

### 2. Create a `TranscriptProvider`

Implement your own provider or extend `WebSocketTranscriptProvider`:

```java
import io.github.bytosphere.provider.WebSocketTranscriptProvider;
import io.github.bytosphere.net.AbstractWebSocketListener;
import io.github.bytosphere.core.TranscriptListener;
import io.github.bytosphere.core.TranscriptionSession;

public class MyTranscriptProvider extends WebSocketTranscriptProvider<String> {

    @Override
    public TranscriptionSession listen(TranscriptListener<String> listener) {
        // Connect to your transcription service
        return client.connect(
            URI.create("wss://api.example.com/transcribe"),
            new AbstractWebSocketListener() {
                
                @Override
                protected void onMessage(String message) {
                    listener.onTranscript(message);
                }

                @Override
                protected void onOpen(WebSocket ws) {
                    // Send audio data here
                }

                @Override
                protected void onError(Throwable error) {
                    listener.onError(error);
                }

                @Override
                protected void onClose(int statusCode, String reason) {
                    listener.onClose(statusCode, reason);
                }
            }
        ).thenApply(connection -> (TranscriptionSession) connection).join();
    }
}
```

### 3. Start Transcribing

```java
public class Main {
    public static void main(String[] args) {
        TranscriptProvider<String> provider = new MyTranscriptProvider();

        // Start listening
        TranscriptionSession session = provider.listen(transcript -> {
            System.out.println("Transcript: " + transcript);
        });

        // When done, cancel the session
        // session.cancel();
    }
}
```

## Architecture

- `TranscriptListener`: Callback interface for receiving transcription events.
- `TranscriptProvider`: Interface for starting transcription sessions.
- `TranscriptionSession`: Controls an active session (cancel, check status).
- `WebSocketClient`: Manages WebSocket connections.
- `AudioEngine`: Captures and processes audio from various sources.

## Requirements

- Java 17 or later

## License

MIT License – see [LICENSE](LICENSE) for details.