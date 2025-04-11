package org.lahirurashmika.converter;

import lombok.Getter;

@Getter
public enum OutputType {
    VIDEO_MP4("mp4", "libx264", "aac"),
    AUDIO_AAC("aac", null, "aac"),
    AUDIO_MP3("mp3", null, "libmp3lame");

    private final String format;
    private final String videoCodec;
    private final String audioCodec;

    OutputType(String format, String videoCodec, String audioCodec) {
        this.format = format;
        this.videoCodec = videoCodec;
        this.audioCodec = audioCodec;
    }
}
