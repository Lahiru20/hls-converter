# HLS Converter

> A powerful Java library for converting media files to and from HLS (HTTP Live Streaming) format

## Overview

HLS Converter is a powerful dependency designed for seamless conversion of video and audio files to the HLS (HTTP Live Streaming) format (.m3u8) with just a single line of code. It also supports reverse conversion of .m3u8 files back into audio or video formats, including all HLS supported formats such as MP4, MP3, and more.

### Key Features

- ⚡ **One-Line Conversion**: Transform media files to HLS format with minimal code, ideal for streaming applications.
- 🔄 **Bi-directional Support**: Convert to HLS and back again with comprehensive format support.
- ⚙️ **Customizable Settings**: Configure segment duration, playlist size, and codecs to match your requirements.
- 🌐 **Web Application Ready**: Seamless integration with Spring's MultipartFile for web applications.
- 📊 **Multiple Formats**: Support for MP4, WEBM, AAC, MP3, OPUS and more media formats.
- 🔌 **Easy Integration**: Simple API that works with both file systems and Spring applications.

## Installation

### Maven

```xml
<dependency>
    <groupId>com.yourcompany</groupId>
    <artifactId>hls-converter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.yourcompany:hls-converter:1.0.0'
```

> **Note:** This library requires JavaCV and FFmpeg as dependencies. Make sure they are properly installed on your system or included in your project dependencies.

## Usage

### Basic Usage

The library provides two versatile classes to handle different use cases:

- **`HLSConverter`**: Designed for file-based operations, allowing you to convert media files to and from HLS format effortlessly.
- **`HLSConverterMultipartFile`**: Perfect for web applications, supporting operations with Spring's `MultipartFile` for seamless integration with file uploads.

#### Converting a file to HLS

```java
import com.yourcompany.hlsconverter.HLSConverter;
import com.yourcompany.hlsconverter.OutputType;

// Basic conversion with default parameters
HLSConverter converter = new HLSConverter();
String[] outputFiles = converter.convertToHls(
    "/path/to/input.mp4",
    "/output/directory",
    "output_filename",
    OutputType.VIDEO_MP4
);
```

#### Converting HLS back to a regular format

```java
// Convert HLS file back to MP4
String outputFile = converter.convertFromHls(
    "/path/to/playlist.m3u8",
    "/output/directory",
    "output_filename",
    OutputType.VIDEO_MP4
);
```

### Working with Spring MultipartFile

```java
import com.yourcompany.hlsconverter.HLSConverterMultipartFile;
import com.yourcompany.hlsconverter.OutputType;
import org.springframework.web.multipart.MultipartFile;

// Convert uploaded file to HLS
HLSConverterMultipartFile converter = new HLSConverterMultipartFile();
MultipartFile[] hlsFiles = converter.convertToHlsMultipartFile(
    uploadedFile,
    "output_filename",
    OutputType.VIDEO_MP4
);
```

## API Reference

### HLSConverter Class

#### Converting to HLS

```java
// Basic conversion with default settings (10-second segments, unlimited playlist size)
public String[] convertToHls(String inputPath, String outputPath, String outputFileName, OutputType outputType) throws Exception

// Custom segment duration
public String[] convertToHls(String inputPath, String outputPath, String outputFileName, OutputType outputType, int segmentDurationSec) throws Exception

// Custom segment duration and playlist size
public String[] convertToHls(String inputPath, String outputPath, String outputFileName, OutputType outputType, int segmentDurationSec, int playlistSize) throws Exception

// Full customization with codec selection
public String[] convertToHls(String inputPath, String outputPath, String outputFileName, OutputType outputType, int segmentDurationSec, int playlistSize, String videoCodec, String audioCodec) throws Exception
```

#### Converting from HLS

```java
public String convertFromHls(String inputPath, String outputPath, String outputFileName, OutputType outputType) throws IOException, FFmpegFrameGrabber.Exception
```

### HLSConverterMultipartFile Class

#### Converting to HLS

```java
// Default settings (10-second segments, 5 files in playlist)
public MultipartFile[] convertToHlsMultipartFile(MultipartFile inputFile, String outputFileName, OutputType outputType) throws Exception

// Custom segment duration
public MultipartFile[] convertToHlsMultipartFile(MultipartFile inputFile, String outputFileName, OutputType outputType, int segmentDuration) throws Exception

// Custom segment duration and playlist size
public MultipartFile[] convertToHlsMultipartFile(MultipartFile inputFile, String outputFileName, OutputType outputType, int segmentDuration, int hlsListSize) throws Exception
```

#### Converting from HLS

```java
public MultipartFile convertFromHlsMultipartFile(MultipartFile hlsFile, String outputFileName, OutputType outputType) throws Exception
```

### OutputType Enum

This enum defines the supported output formats and their default codecs:

| Type | Format | Video Codec | Audio Codec |
|------|--------|-------------|-------------|
| VIDEO_MP4 | mp4 | libx264 | aac |
| VIDEO_WEBM | webm | vp9 | libopus |
| AUDIO_AAC | aac | null | aac |
| AUDIO_MP3 | mp3 | null | libmp3lame |
| AUDIO_OPUS | opus | null | libopus |

## Examples

### Example 1: Basic Conversion

```java
import com.yourcompany.hlsconverter.HLSConverter;
import com.yourcompany.hlsconverter.OutputType;

public class BasicExample {
    public static void main(String[] args) {
        try {
            // Initialize converter
            HLSConverter converter = new HLSConverter();
            
            // Convert MP4 to HLS with default settings
            String[] outputFiles = converter.convertToHls(
                "input.mp4",
                "output_directory",
                "video_stream",
                OutputType.VIDEO_MP4
            );
            
            // Print paths of generated files
            System.out.println("Generated files:");
            for (String file : outputFiles) {
                System.out.println(file);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Example 2: Spring Boot Integration

```java
import com.yourcompany.hlsconverter.HLSConverterMultipartFile;
import com.yourcompany.hlsconverter.OutputType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ConversionController {

    @PostMapping("/convert")
    public ResponseEntity<?> convertVideo(@RequestParam("file") MultipartFile file) {
        try {
            HLSConverterMultipartFile converter = new HLSConverterMultipartFile();
            
            // Convert uploaded file to HLS
            MultipartFile[] hlsFiles = converter.convertToHlsMultipartFile(
                file,
                "video_stream",
                OutputType.VIDEO_MP4
            );
            
            // In a real application, you would save these files or return them to client
            return ResponseEntity.ok("Converted " + hlsFiles.length + " HLS files");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error converting file: " + e.getMessage());
        }
    }
}
```

### Example 3: Advanced Configuration

```java
import com.yourcompany.hlsconverter.HLSConverter;
import com.yourcompany.hlsconverter.OutputType;

public class AdvancedExample {
    public static void main(String[] args) {
        try {
            // Initialize converter
            HLSConverter converter = new HLSConverter();
            
            // Convert with custom settings
            // - 5-second segments
            // - Keep only 10 segments in playlist
            // - Force H.265 video codec
            String[] outputFiles = converter.convertToHls(
                "input.mp4",
                "output_directory",
                "video_stream",
                OutputType.VIDEO_MP4,
                5,  // segment duration
                10, // playlist size
                "libx265", // video codec
                null  // use default audio codec
            );
            
            System.out.println("Generated " + outputFiles.length + " files");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Example 4: Converting HLS Back to Regular Format

```java
import com.yourcompany.hlsconverter.HLSConverter;
import com.yourcompany.hlsconverter.OutputType;

public class HlsToMp4Example {
    public static void main(String[] args) {
        try {
            HLSConverter converter = new HLSConverter();
            
            // Convert HLS playlist to MP4 file
            String outputFile = converter.convertFromHls(
                "input.m3u8",
                "output_directory",
                "final_video",
                OutputType.VIDEO_MP4
            );
            
            System.out.println("Generated output file: " + outputFile);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

