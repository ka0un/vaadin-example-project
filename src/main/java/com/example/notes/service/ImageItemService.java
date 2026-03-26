package com.example.notes.service;

import com.example.notes.data.entity.ImageItem;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service that handles image uploading, retrieval, and deletion.
 * Images are saved to the filesystem under {@code app.upload.dir};
 * metadata is persisted in the database.
 */
@Service
public class ImageItemService {

    /** Allowed MIME types for uploaded images */
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /** Maximum allowed file size: 10 MB */
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final ImageItemRepository repository;
    private final Path uploadDir;

    public ImageItemService(ImageItemRepository repository,
                            @Value("${app.upload.dir:uploads}") String uploadDirPath) throws IOException {
        this.repository = repository;
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        // Ensure the uploads directory exists on startup
        Files.createDirectories(this.uploadDir);
    }

    /**
     * Validates and saves an uploaded image.
     *
     * @param inputStream  raw bytes from the upload
     * @param originalName original filename from the browser
     * @param contentType  MIME type declared by the browser
     * @param fileSize     reported size in bytes (used for size guard)
     * @param caption      optional user-supplied caption
     * @param user         the authenticated user
     * @throws IllegalArgumentException if the file type or size is invalid
     * @throws IOException              if writing to disk fails
     */
    public ImageItem saveImage(InputStream inputStream,
                               String originalName,
                               String contentType,
                               long fileSize,
                               String caption,
                               User user) throws IOException {

        // --- Validation ---
        if (inputStream == null) {
            throw new IllegalArgumentException("No file data received.");
        }
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Please upload a JPEG, PNG, GIF, or WebP image.");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File is too large. Maximum allowed size is 10 MB.");
        }

        // --- Persist to disk with a UUID filename to avoid collisions ---
        String extension = getExtension(originalName);
        String uniqueFilename = UUID.randomUUID() + "." + extension;
        Path targetPath = uploadDir.resolve(uniqueFilename);
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

        // --- Persist metadata to database ---
        ImageItem item = new ImageItem(
                uniqueFilename,
                originalName != null ? originalName : uniqueFilename,
                caption != null ? caption.trim() : "",
                LocalDateTime.now(),
                user
        );
        return repository.save(item);
    }

    /** Returns all images belonging to the given user, newest first. */
    public List<ImageItem> getImages(User user) {
        return repository.findByUserOrderByUploadedAtDesc(user);
    }

    /**
     * Deletes an image from disk and database.
     * Only deletes if the image belongs to the given user (prevents cross-user access).
     */
    public void deleteImage(Long id, User user) throws IOException {
        ImageItem item = repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Image not found or access denied."));

        // Remove from filesystem
        Path filePath = uploadDir.resolve(item.getFilename());
        Files.deleteIfExists(filePath);

        // Remove from database
        repository.delete(item);
    }

    /**
     * Resolves the absolute file path for a given on-disk filename.
     * Used by {@link com.example.notes.controller.ImageServeController}.
     */
    public Path resolveFilePath(String filename) {
        return uploadDir.resolve(filename).normalize();
    }

    // --- Helpers ---

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
