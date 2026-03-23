package com.example.notes.service;

import com.example.notes.data.entity.GalleryItem;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.GalleryItemRepository;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;


@Service
public class GalleryService {

    private final GalleryItemRepository galleryItemRepository;
    private final String UPLOAD_DIR = "data/uploads/";

    public GalleryService(GalleryItemRepository galleryItemRepository) {
        this.galleryItemRepository = galleryItemRepository;
        try {
            // Ensure the upload directory exists
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize upload directory", e);
        }
    }

    // Retrieve all gallery items for a specific user.
    public List<GalleryItem> getGalleryItems(User user) {
        return galleryItemRepository.findByUserOrderByUploadTimeDesc(user);
    }

    // Save an uploaded image to disk and metadata to the database.
    public void saveGalleryItem(InputStream inputStream, String originalFileName, String contentType, User user) {
        try {
            String extension = "";
            int dotIndex = originalFileName.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = originalFileName.substring(dotIndex);
            }
            
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            Path path = Paths.get(UPLOAD_DIR, uniqueFileName);

            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            GalleryItem item = new GalleryItem(originalFileName, contentType, path.toString(), user);
            galleryItemRepository.save(item);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save gallery item", e);
        }
    }

    // Delete a gallery item from both disk and database.
    public void deleteGalleryItem(GalleryItem item) {
        try {
            Files.deleteIfExists(Paths.get(item.getFilePath()));
            galleryItemRepository.delete(item);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete gallery item", e);
        }
    }
}
