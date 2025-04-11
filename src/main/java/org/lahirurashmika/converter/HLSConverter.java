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
    // Default values for HLS parameters
    private static final int DEFAULT_SEGMENT_DURATION = 10;
    private static final int DEFAULT_PLAYLIST_SIZE = 0; 

    public String[] convertToHls(String inputPath, String outputPath,
            String outputFileName, OutputType outputType)
            throws Exception {
        return convertToHls(inputPath, outputPath, outputFileName, outputType,
                DEFAULT_SEGMENT_DURATION, DEFAULT_PLAYLIST_SIZE,
                null, null);
    }

    public String[] convertToHls(String inputPath, String outputPath,
            String outputFileName, OutputType outputType, int segmentDurationSec)
            throws Exception {
        return convertToHls(inputPath, outputPath, outputFileName, outputType,
                segmentDurationSec, DEFAULT_PLAYLIST_SIZE,
                null, null);
    }

    public String[] convertToHls(String inputPath, String outputPath,
            String outputFileName, OutputType outputType, int segmentDurationSec, int playlistSize)
            throws Exception {
        return convertToHls(inputPath, outputPath, outputFileName, outputType,
                segmentDurationSec, playlistSize,
                null, null);
    }

    public String[] convertToHls(String inputPath, String outputPath,
            String outputFileName, OutputType outputType, int segmentDurationSec, int playlistSize, String videoCodec)
            throws Exception {
        return convertToHls(inputPath, outputPath, outputFileName, outputType,
                segmentDurationSec, playlistSize,
                videoCodec, null);
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
                    if (frame == null)
                        break;
                    recorder.record(frame);
                }
            }
        }

        return outputFile;
    }

    public String[] convertToHls(String inputPath, String outputPath,
            String outputFileName, OutputType outputType,
            int segmentDurationSec, int playlistSize,
            String videoCodec, String audioCodec)
            throws Exception {

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

                // Configure HLS parameters
                recorder.setFormat("hls");
                recorder.setOption("hls_time", String.valueOf(segmentDurationSec));
                recorder.setOption("hls_list_size", String.valueOf(playlistSize));
                recorder.setOption("hls_segment_filename", segmentPattern);

                // Set codecs (use parameters if provided, otherwise fallback to OutputType)
                configureCodecs(recorder, outputType, videoCodec, audioCodec);

                recorder.start();

                while (true) {
                    org.bytedeco.javacv.Frame frame = grabber.grab();
                    if (frame == null)
                        break;
                    recorder.record(frame);
                }
            }
        }

        return collectSegments(outputPath, outputFileName);
    }

    private void configureCodecs(FFmpegFrameRecorder recorder,
            OutputType outputType,
            String videoCodecParam,
            String audioCodecParam) {

        // Determine final codecs to use
        String videoCodec = (videoCodecParam != null) ? videoCodecParam : outputType.getVideoCodec();
        String audioCodec = (audioCodecParam != null) ? audioCodecParam : outputType.getAudioCodec();

        // Set video codec if applicable
        if (videoCodec != null) {
            int codecId = getAvCodec(videoCodec);
            if (codecId == avcodec.AV_CODEC_ID_NONE) {
                throw new IllegalArgumentException("Unsupported video codec: " + videoCodec);
            }
            recorder.setVideoCodec(codecId);
        }

        // Set audio codec
        if (audioCodec != null) {
            int codecId = getAvCodec(audioCodec);
            if (codecId == avcodec.AV_CODEC_ID_NONE) {
                throw new IllegalArgumentException("Unsupported audio codec: " + audioCodec);
            }
            recorder.setAudioCodec(codecId);
        }
    }

    private int getAvCodec(String codecName) {
        switch (codecName.toLowerCase()) {
            case "libx264":
            case "h264":
                return avcodec.AV_CODEC_ID_H264;
            case "libx265":
            case "h265":
                return avcodec.AV_CODEC_ID_HEVC;
            case "aac":
                return avcodec.AV_CODEC_ID_AAC;
            case "libmp3lame":
            case "mp3":
                return avcodec.AV_CODEC_ID_MP3;
            case "vp9":
                return avcodec.AV_CODEC_ID_VP9;
            default:
                return avcodec.AV_CODEC_ID_NONE;
        }
    }

    private String[] collectSegments(String outputPath, String baseName) {
        File dir = new File(outputPath);
        List<String> segments = new ArrayList<>();
        segments.add(outputPath + File.separator + baseName + ".m3u8");

        File[] tsFiles = dir.listFiles((d, name) -> name.startsWith(baseName) && name.endsWith(".ts"));

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
