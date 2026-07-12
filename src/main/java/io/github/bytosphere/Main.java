package io.github.bytosphere;

import io.github.bytosphere.core.TranscriptionSession;
import io.github.bytosphere.provider.DefaultTranscriptProvider;

import java.net.URI;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        DefaultTranscriptProvider provider = new DefaultTranscriptProvider();

        URI url = URI.create("ws://localhost:8080/ws");

        // Listen for transcripts.
        TranscriptionSession session = provider.listen(url, (message) -> {
            System.out.println("Transcript: " + message);
        });

        // Simulate WebSocket transcription.
        provider.simulateTranscript("Hello, this is a simulated transcript.");
        provider.simulateTranscript("This is the second message.");
        provider.simulateTranscript("And this is the third message.");

        // Simulate WebSocket disconnection.
        Thread.sleep(5000);

        session.disconnect();
        session.close();
    }
}