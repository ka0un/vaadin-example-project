package com.example.notes.service;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing images — saving, retrieving, deleting, searching, and favorites.
 */
@Service
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    /**
     * Reads image from stream, extracts metadata (size, dimensions), and persists it.
     */
    @Transactional
    public void saveImage(String fileName, String contentType, InputStream inputStream, User user) throws IOException {
        byte[] data = inputStream.readAllBytes();
        long fileSize = data.length;
        int width = 0;
        int height = 0;

        // Attempt to read image dimensions
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            BufferedImage bufferedImage = ImageIO.read(bis);
            if (bufferedImage != null) {
                width = bufferedImage.getWidth();
                height = bufferedImage.getHeight();
            }
        } catch (Exception e) {
            // Non-critical: dimensions remain 0 if unreadable
        }

        Image image = new Image(fileName, contentType, data,
                LocalDateTime.now(), fileSize, width, height, user);
        imageRepository.save(image);
    }

    /** Returns all images for a user, ordered by upload date (newest first). */
    public List<Image> getImagesByUser(User user) {
        return imageRepository.findByUser(user);
    }

    /**
     * Returns images filtered by name search. If query is blank, returns all images.
     */
    public List<Image> getImagesByUserFiltered(User user, String nameFilter) {
        if (nameFilter == null || nameFilter.isBlank()) {
            return imageRepository.findByUser(user);
        }
        return imageRepository.findByUserAndNameContainingIgnoreCase(user, nameFilter.trim());
    }

    /** Returns the count of images uploaded by a user. */
    public long countImagesByUser(User user) {
        return imageRepository.countByUser(user);
    }

    /** Toggles the favorite status of an image and persists the change. */
    @Transactional
    public boolean toggleFavorite(Image image) {
        image.setFavorite(!image.isFavorite());
        imageRepository.save(image);
        return image.isFavorite();
    }

    /** Returns only favourite images for a user. */
    public List<Image> getFavoritesByUser(User user) {
        return imageRepository.findByUserAndFavoriteTrue(user);
    }

    @Transactional
    public void deleteImage(Image image) {
        imageRepository.delete(image);
    }
}
