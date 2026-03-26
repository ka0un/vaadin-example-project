package com.example.notes.service;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    
    // The folder on your computer where images are stored
    private final String uploadDir = "uploads/images/";

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    /**
     * Entry point for uploading a single file from a buffer
     */
    public Image saveImage(MemoryBuffer buffer, User user) throws IOException {
        return saveImage(buffer.getFileName(), buffer.getInputStream(), user);
    }

    /**
     * Core logic to validate, save to disk, and save to database
     */
    public Image saveImage(String originalName, InputStream inputStream, User user) throws IOException {
        String normalizedName = originalName == null ? "" : originalName.toLowerCase(Locale.ENGLISH);

        // Security check: Only allow common image formats
        if (!normalizedName.matches(".*\\.(png|jpg|jpeg|gif|webp)$")) {
            throw new IllegalArgumentException("Invalid file type");
        }

        // Create a unique name (UUID) so "cat.jpg" doesn't overwrite another "cat.jpg"
        String fileName = UUID.randomUUID() + "_" + originalName;

        // Prepare the file path and create folders if they don't exist
        File file = new File(uploadDir + fileName);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        // Copy the uploaded data into the physical file on your hard drive
        try (InputStream input = inputStream) {
            Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Create a new Image object to store the file's details in the database
        Image image = new Image();
        image.setFileName(fileName);
        image.setFilePath(file.getAbsolutePath()); // Store full path for easy deletion later
        image.setUser(user);
        image.setFavorite(false); 

        return imageRepository.save(image);
    }

    /**
     * Fetch all images belonging to the logged-in user (Newest first)
     */
    public List<Image> getUserImages(User user) {
        return imageRepository.findByUserOrderByIdDesc(user);
    }

    /**
     * Deletes the physical file from the folder AND the record from the database
     */
    public void deleteImage(Image image) {
        try {
            // Step 1: Find the physical file using the stored path
            // 1. Find the real file/drawing from the toy box/database and throw it away
            Path path = Paths.get(image.getFilePath());
            
            // Step 2: Try to delete it from the 'uploads/images' folder
            boolean wasDeleted = Files.deleteIfExists(path);
            
            // Check if the file is actually there before trying to delete it, and log the outcome
            if (wasDeleted) {
                System.out.println("Physical file deleted successfully.");
            } else {
                System.out.println("File not found on disk, just cleaning up database.");
            }
        } catch (IOException e) {
            // If the file is 'locked' or permissions fail, we log it but keep going
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }

        // Step 3: Remove the 'Note' or entry from the database table
        // 2. Tell the notebook to forget about it
        imageRepository.delete(image);
    }

    /**
     * Switches the heart icon on or off
     */
    public void toggleFavorite(Image image) {
        image.setFavorite(!image.isFavorite());
        imageRepository.save(image); // Update the record in the database
    }
}