package com.example.notes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class FileServiceTest {

    private final FileService fileService = new FileService();

    @Test
    void deleteFileIfExistsReturnsTrueWhenFileIsDeleted(@TempDir Path tempDir) throws IOException {
        Path file = Files.writeString(tempDir.resolve("image.jpg"), "content");

        boolean deleted = fileService.deleteFileIfExists(file.toFile());

        assertTrue(deleted);
        assertFalse(Files.exists(file));
    }

    @Test
    void deleteFileIfExistsReturnsFalseWhenFileDoesNotExist(@TempDir Path tempDir) {
        Path missingFile = tempDir.resolve("missing.jpg");

        boolean deleted = fileService.deleteFileIfExists(missingFile.toFile());

        assertFalse(deleted);
    }

    @Test
    void deleteFileIfExistsLogsWarningWhenDeletionFails(@TempDir Path tempDir, CapturedOutput output) throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("occupied"));
        Files.writeString(directory.resolve("child.txt"), "content");
        File file = directory.toFile();

        boolean deleted = fileService.deleteFileIfExists(file);

        assertFalse(deleted);
        assertTrue(Files.exists(directory));
        assertTrue(output.getOut().contains("Failed to delete file"));
        assertTrue(output.getOut().contains(file.toString()));
    }
}
