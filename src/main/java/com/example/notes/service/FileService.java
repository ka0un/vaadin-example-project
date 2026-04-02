package com.example.notes.service;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    public void saveFiles(byte[] imageBytes, File originalFile, File thumbFile) throws IOException {

        Files.write(originalFile.toPath(), imageBytes);

        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(300, 300)
                .outputFormat("jpg")
                .toFile(thumbFile);
    }

    public String extractAndValidateFormat(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is empty");
        }

        if (!fileName.contains(".")) {
            throw new IllegalArgumentException("File must have an extension");
        }

        String format = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        if (!List.of("jpg", "jpeg", "png", "webp").contains(format)) {
            throw new IllegalArgumentException("Unsupported file format");
        }

        return format;
    }

    public boolean deleteFileIfExists(File file) {
        try {
            return Files.deleteIfExists(file.toPath());
        } catch (IOException exception) {
            LOGGER.warn("Failed to delete file {}", file, exception);
            return false;
        }
    }

    public void moveFiles(Path newPath, Path oldPath, StandardCopyOption standardCopyOption) throws IOException {
        Files.move(newPath, oldPath, standardCopyOption);
    }
}
