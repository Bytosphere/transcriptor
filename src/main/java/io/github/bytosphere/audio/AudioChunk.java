package io.github.bytosphere.audio;

import lombok.Getter;
import lombok.Setter;

/** A chunk of audio data that gets transmitted. */
@Getter
public class AudioChunk {

    @Setter
    private byte[] data;

    public AudioChunk(int size) {
        this.data = new byte[size];
    }
}
