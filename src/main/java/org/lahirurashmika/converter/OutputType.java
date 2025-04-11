package org.lahirurashmika.converter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutputType {
    VIDEO_MP4("mp4", "libx264", "aac"),
    VIDEO_WEBM("webm", "vp9", "libopus"),
    AUDIO_AAC("aac", null, "aac"),
    AUDIO_MP3("mp3", null, "libmp3lame"),
    AUDIO_OPUS("opus", null, "libopus");

    private final String format;
    private final String videoCodec;
    private final String audioCodec;

}
