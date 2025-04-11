package org.lahirurashmika;

import org.lahirurashmika.converter.HLSConverter;
import org.lahirurashmika.converter.OutputType;

public class Main {
    public static void main(String[] args) {
        HLSConverter converter = new HLSConverter();

        try {
            // Convert video to HLS
            String[] hlsFiles = converter.convertToHls(
                    "src/main/resources/input/input.mp4",
                    "src/main/resources/output",
                    "converted_video",
                    OutputType.VIDEO_MP4
            );
            System.out.println("Generated HLS files:");
            for (String file : hlsFiles) {
                System.out.println(" - " + file);
            }

            // Convert HLS to MP4
            String mp3File = converter.convertFromHls(
                    "src/main/resources/input/converted_video.m3u8",
                    "src/main/resources/output",
                    "converted_video",
                    OutputType.VIDEO_MP4
            );
            System.out.println("\nGenerated audio file: " + mp3File);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}