package com.example.notes.service;

import net.coobird.thumbnailator.Thumbnails;
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

    public void saveFiles(byte[] imageBytes, File originalFile, File thumbFile) throws IOException {

        // Save original
        Files.write(originalFile.toPath(), imageBytes);

        // Create thumbnail (always JPG for consistency & size)
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

        // Allow only safe formats
        if (!List.of("jpg", "jpeg", "png", "webp").contains(format)) {
            throw new IllegalArgumentException("Unsupported file format");
        }

        return format;
    }

    public void deleteFileIfExists(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException ignored) {
        }
    }

    public void moveFiles(Path newPath, Path oldPath, StandardCopyOption standardCopyOption) throws IOException {
        Files.move(newPath, oldPath, standardCopyOption);
    }
}
