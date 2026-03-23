package com.example.notes.service;

import com.example.notes.data.entity.ImageEntity;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    // Folder where uploaded images will be saved on disk
    private final Path uploadDir = Paths.get("uploads");

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;

        // Create uploads folder if it doesn't exist
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }
    // Save image file to disk and save info to database
    public void saveImage(String fileName, String mimeType, InputStream inputStream, User user) {

        try {
            // Clean the file name (remove special characters)
            String cleanFileName = StringUtils.cleanPath(fileName);

            // Generate a unique file name to avoid duplicates
            // e.g. "a1b2c3d4-photo.jpg"
            String uniqueFileName = UUID.randomUUID() + "-" + cleanFileName;

            // Full path where file will be saved
            Path targetPath = uploadDir.resolve(uniqueFileName);

            // Copy the uploaded file to the uploads folder
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Save image info to database
            ImageEntity image = new ImageEntity(cleanFileName, targetPath.toString(), mimeType, user);
            imageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + fileName, e);
        }
    }

    // Get all images for a specific user (newest first)
    public List<ImageEntity> getImages(User user) {
        return imageRepository.findByUserOrderByUploadedAtDesc(user);
    }

    // Delete image from disk and from database
    public void deleteImage(ImageEntity image) {
        try {
            // Delete file from disk
            Path filePath = Paths.get(image.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete record from database
            imageRepository.delete(image);

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + image.getFileName(), e);
        }
    }

    // Read image file from disk as byte array (needed to display in browser)
    public byte[] loadImageAsBytes(ImageEntity image) {
        try {
            Path filePath = Paths.get(image.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load image: " + image.getFileName(), e);
        }
    }

}
