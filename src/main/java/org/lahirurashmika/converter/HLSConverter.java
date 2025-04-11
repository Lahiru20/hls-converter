package org.lahirurashmika.converter;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HLSConverter {

    public String[] convertToHls(String inputPath, String outputPath,
                                 String outputFileName, OutputType outputType)
            throws IOException, FFmpegFrameGrabber.Exception {

        validatePaths(inputPath, outputPath);
        String baseName = outputPath + File.separator + outputFileName;
        String m3u8Path = baseName + ".m3u8";
        String segmentPattern = baseName + "_%03d.ts";

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath)) {
            grabber.start();

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                    m3u8Path,
                    grabber.getImageWidth(),
                    grabber.getImageHeight(),
                    grabber.getAudioChannels())) {

                recorder.setFormat("hls");
                if (outputType.getVideoCodec() != null) {
                    recorder.setVideoCodec(getAvCodec(outputType.getVideoCodec()));
                }
                recorder.setAudioCodec(getAvCodec(outputType.getAudioCodec()));
                recorder.setOption("hls_time", "10");
                recorder.setOption("hls_list_size", "0");
                recorder.setOption("hls_segment_filename", segmentPattern); // Use manual pattern
                recorder.start();

                while (true) {
                    org.bytedeco.javacv.Frame frame = grabber.grab();
                    if (frame == null) break;
                    recorder.record(frame);
                }
            }
        }

        return collectSegments(outputPath, outputFileName);
    }

    public String convertFromHls(String inputPath, String outputPath,
                                 String outputFileName, OutputType outputType)
            throws IOException, FFmpegFrameGrabber.Exception {

        validatePaths(inputPath, outputPath);
        String outputFile = outputPath + File.separator +
                outputFileName + "." + outputType.getFormat();

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath)) {
            grabber.start();

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                    outputFile,
                    grabber.getImageWidth(),
                    grabber.getImageHeight(),
                    grabber.getAudioChannels())) {

                recorder.setFormat(outputType.getFormat());
                if (outputType.getVideoCodec() != null) {
                    recorder.setVideoCodec(getAvCodec(outputType.getVideoCodec()));
                }
                recorder.setAudioCodec(getAvCodec(outputType.getAudioCodec()));
                recorder.start();

                while (true) {
                    org.bytedeco.javacv.Frame frame = grabber.grab();
                    if (frame == null) break;
                    recorder.record(frame);
                }
            }
        }

        return outputFile;
    }

    private int getAvCodec(String codecName) {
        switch (codecName) {
            case "libx264":
                return avcodec.AV_CODEC_ID_H264;
            case "aac":
                return avcodec.AV_CODEC_ID_AAC;
            case "libmp3lame":
                return avcodec.AV_CODEC_ID_MP3;
            default:
                return avcodec.AV_CODEC_ID_NONE;
        }
    }

    private String[] collectSegments(String outputPath, String baseName) {
        File dir = new File(outputPath);
        List<String> segments = new ArrayList<>();
        segments.add(outputPath + File.separator + baseName + ".m3u8");

        File[] tsFiles = dir.listFiles((d, name) ->
                name.startsWith(baseName) && name.endsWith(".ts"));

        if (tsFiles != null) {
            for (File ts : tsFiles) {
                segments.add(ts.getAbsolutePath());
            }
        }
        return segments.toArray(new String[0]);
    }

    private void validatePaths(String inputPath, String outputPath) throws IOException {
        if (!Files.exists(Path.of(inputPath))) {
            throw new IOException("Input file not found: " + inputPath);
        }
        if (!Files.isDirectory(Path.of(outputPath))) {
            throw new IOException("Output path is not a directory: " + outputPath);
        }
    }
}
