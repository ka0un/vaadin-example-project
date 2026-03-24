package com.example.notes.service;

import com.example.notes.data.entity.ImageMetadata;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import com.example.notes.data.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    public ImageService(ImageRepository imageRepository, UserRepository userRepository) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Gets the currently authenticated user.
     */
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Saves an image for the currently authenticated user.
     */
    @Transactional
    public ImageMetadata saveImage(String originalFileName, String contentType,
                                    byte[] imageData) {
        User user = getCurrentUser();
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        ImageMetadata metadata = new ImageMetadata(
                storedFileName, originalFileName, contentType,
                imageData.length, imageData, user
        );

        return imageRepository.save(metadata);
    }

    /**
     * Gets all images for the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public List<ImageMetadata> getUserImages() {
        User user = getCurrentUser();
        return imageRepository.findByUserOrderByUploadedAtDesc(user);
    }

    /**
     * Gets a specific image by ID (only if it belongs to the current user).
     */
    @Transactional(readOnly = true)
    public Optional<ImageMetadata> getImage(Long id) {
        User user = getCurrentUser();
        return imageRepository.findById(id)
                .filter(img -> img.getUser().getId().equals(user.getId()));
    }

    /**
     * Gets the image data as an InputStream.
     */
    @Transactional(readOnly = true)
    public InputStream getImageStream(Long id) {
        return getImage(id)
                .map(img -> (InputStream) new ByteArrayInputStream(img.getImageData()))
                .orElse(null);
    }

    /**
     * Deletes an image by ID (only if it belongs to the current user).
     */
    @Transactional
    public boolean deleteImage(Long id) {
        Optional<ImageMetadata> image = getImage(id);
        if (image.isPresent()) {
            imageRepository.delete(image.get());
            return true;
        }
        return false;
    }

    /**
     * Updates the description of an image.
     */
    @Transactional
    public boolean updateDescription(Long id, String description) {
        Optional<ImageMetadata> image = getImage(id);
        if (image.isPresent()) {
            image.get().setDescription(description);
            imageRepository.save(image.get());
            return true;
        }
        return false;
    }

    /**
     * Returns the count of images for the current user.
     */
    @Transactional(readOnly = true)
    public long getImageCount() {
        User user = getCurrentUser();
        return imageRepository.countByUser(user);
    }
}
