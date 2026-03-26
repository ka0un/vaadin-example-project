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

    // Folder where uploaded images will be saved on disk
    private static final String UPLOAD_DIR = "uploads/";

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
        // Create the uploads folder if it doesn't exist
        new File(UPLOAD_DIR).mkdirs();
    }

    // Save uploaded image to disk and store metadata in DB
    public Image saveImage(String originalFileName, String mimeType, InputStream inputStream) {
        try {
            // Generate a unique filename to avoid conflicts
            String extension = getExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + extension;
            Path targetPath = Paths.get(UPLOAD_DIR + storedFileName);

            // Write file bytes to disk
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Save metadata to database
            Image image = new Image();
            image.setOriginalFileName(originalFileName);
            image.setStoredFileName(storedFileName);
            image.setMimeType(mimeType);

            return imageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    // Get all images (for displaying in gallery)
    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    // Delete image from disk and from database
    public void deleteImage(Long id) {
        imageRepository.findById(id).ifPresent(image -> {
            try {
                Files.deleteIfExists(Paths.get(UPLOAD_DIR + image.getStoredFileName()));
            } catch (IOException e) {
                // Log but don't crash if file is already missing
                System.err.println("Could not delete file: " + e.getMessage());
            }
            imageRepository.delete(image);
        });
    }

    // Create a StreamResource so Vaadin can serve the image to the browser
    public StreamResource getImageResource(Image image) {
        return new StreamResource(image.getOriginalFileName(), () -> {
            try {
                return new FileInputStream(UPLOAD_DIR + image.getStoredFileName());
            } catch (FileNotFoundException e) {
                return InputStream.nullInputStream();
            }
        });
    }

    // Helper: extract file extension e.g. ".jpg"
    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex >= 0) ? fileName.substring(dotIndex) : "";
    }
}