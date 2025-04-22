package org.lahirurashmika.converter;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HLSConverterMultipartFile {

        // Convert MultipartFile to HLS (returns array of MultipartFiles)
        public MultipartFile[] convertToHlsMultipartFile(
                        MultipartFile inputFile,
                        String outputFileName,
                        OutputType outputType) throws Exception {
                return convertToHlsMultipartFile(inputFile, outputFileName, outputType, 10, 5);
        }

        public MultipartFile[] convertToHlsMultipartFile(
                        MultipartFile inputFile,
                        String outputFileName,
                        OutputType outputType,
                        int segmentDuration) throws Exception {
                return convertToHlsMultipartFile(inputFile, outputFileName, outputType, segmentDuration, 5);
        }

        public MultipartFile[] convertToHlsMultipartFile(
                        MultipartFile inputFile,
                        String outputFileName,
                        OutputType outputType,
                        int segmentDuration,
                        int hlsListSize) throws Exception {

                // 1. Save input file to temp directory
                Path tempDir = Files.createTempDirectory("hls-input-");
                Path inputPath = tempDir.resolve(Objects.requireNonNull(inputFile.getOriginalFilename()));
                inputFile.transferTo(inputPath);

                // 2. Convert to HLS
                String[] generatedFiles = new HLSConverter().convertToHls(
                                inputPath.toString(),
                                tempDir.toString(),
                                outputFileName,
                                outputType,
                                segmentDuration,
                                hlsListSize,
                                null,
                                null);

                // 3. Convert generated files to MultipartFile[]
                List<MultipartFile> result = new ArrayList<>();
                for (String filePath : generatedFiles) {
                        File file = new File(filePath);
                        result.add(new MockMultipartFile(
                                        file.getName(),
                                        file.getName(),
                                        "application/octet-stream",
                                        Files.readAllBytes(file.toPath())));
                }

                // 4. Cleanup temp files
                FileUtils.deleteDirectory(tempDir.toFile());

                return result.toArray(new MultipartFile[0]);
        }

        // Convert HLS (MultipartFile) to output format
        public MultipartFile convertFromHlsMultipartFile(
                        MultipartFile hlsFile,
                        String outputFileName,
                        OutputType outputType) throws Exception {

                // 1. Save HLS file to temp directory
                Path tempDir = Files.createTempDirectory("hls-output-");
                Path m3u8Path = tempDir.resolve(Objects.requireNonNull(hlsFile.getOriginalFilename()));
                Files.write(m3u8Path, hlsFile.getBytes());

                // 2. Convert from HLS
                String outputFilePath = new HLSConverter().convertFromHls(
                                m3u8Path.toString(),
                                tempDir.toString(),
                                outputFileName,
                                outputType);

                // 3. Read output file into MultipartFile
                File outputFile = new File(outputFilePath);
                MultipartFile result = new MockMultipartFile(
                                outputFile.getName(),
                                outputFile.getName(),
                                "application/octet-stream",
                                Files.readAllBytes(outputFile.toPath()));

                // 4. Cleanup
                FileUtils.deleteDirectory(tempDir.toFile());
                return result;
        }

}
