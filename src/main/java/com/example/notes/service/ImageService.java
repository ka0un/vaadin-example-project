package com.example.notes.service;

import com.example.notes.data.entity.Image;
import com.example.notes.data.repository.ImageRepository;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.StreamResource;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    private static final String UPLOAD_DIR = "uploads/";

    // Allowed image MIME types for validation
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
        createUploadDirectory();
    }

    // Creates upload folder on startup if it doesn't exist
    private void createUploadDirectory() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    /**
     * Saves an uploaded image to disk and stores its metadata in the database.
     * Validates file type and size before saving.
     */
    public Image saveImage(String originalFileName, String mimeType, InputStream inputStream) {

        // Validate MIME type
        if (!ALLOWED_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + mimeType + ". Only JPG, PNG, GIF, WEBP allowed."
            );
        }

        // Validate filename is not blank
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }

        try {
            byte[] fileBytes = inputStream.readAllBytes();

            // Validate file size
            if (fileBytes.length > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File exceeds 10MB limit.");
            }

            // Validate file is not empty
            if (fileBytes.length == 0) {
                throw new IllegalArgumentException("Uploaded file is empty.");
            }

            // Generate unique filename to prevent conflicts
            String extension = getExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + extension;
            Path targetPath = Paths.get(UPLOAD_DIR + storedFileName);

            Files.write(targetPath, fileBytes);

            // Persist metadata to database
            Image image = new Image();
            image.setOriginalFileName(originalFileName);
            image.setStoredFileName(storedFileName);
            image.setMimeType(mimeType);

            return imageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    /** Returns all images stored in the database. */
    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    /**
     * Deletes an image from disk and removes its record from the database.
     * Safe to call even if the file is already missing from disk.
     */
    public void deleteImage(Long id) {
        if (id == null) return;

        imageRepository.findById(id).ifPresent(image -> {
            // Delete file from disk
            try {
                Files.deleteIfExists(Paths.get(UPLOAD_DIR + image.getStoredFileName()));
            } catch (IOException e) {
                System.err.println("Warning: Could not delete file " + image.getStoredFileName());
            }
            // Remove record from database
            imageRepository.delete(image);
        });
    }

    /**
     * Creates a StreamResource so Vaadin can serve the image bytes to the browser.
     * Returns an empty stream if the file is missing from disk.
     */
    public StreamResource getImageResource(Image image) {
        return new StreamResource(image.getOriginalFileName(), () -> {
            Path filePath = Paths.get(UPLOAD_DIR + image.getStoredFileName());
            if (!Files.exists(filePath)) {
                System.err.println("Warning: File not found on disk: " + image.getStoredFileName());
                return InputStream.nullInputStream();
            }
            try {
                return new FileInputStream(filePath.toFile());
            } catch (FileNotFoundException e) {
                return InputStream.nullInputStream();
            }
        });
    }

    /** Extracts file extension from filename, e.g. "photo.jpg" → ".jpg" */
    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex >= 0) ? fileName.substring(dotIndex).toLowerCase() : "";
    }
}