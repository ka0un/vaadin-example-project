package com.example.notes.service;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserImage;
import com.example.notes.data.repository.UserImageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ImageService {

    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp"
    );

    private final UserImageRepository userImageRepository;

    public ImageService(UserImageRepository userImageRepository) {
        this.userImageRepository = userImageRepository;
    }

    public List<UserImage> getImages(User user) {
        return userImageRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void saveImage(String fileName, String contentType, byte[] data, User user) {
        validateImage(fileName, contentType, data);

        String safeFileName = (fileName == null || fileName.isBlank()) ? "uploaded-image" : fileName;
        String normalizedContentType = normalizeContentType(safeFileName, contentType);
        userImageRepository.save(new UserImage(safeFileName, normalizedContentType, data, user));
    }

    public void deleteImage(UserImage image, User user) {
        if (image.getUser() == null || user.getId() == null || !user.getId().equals(image.getUser().getId())) {
            throw new IllegalArgumentException("You can only delete your own images");
        }
        userImageRepository.delete(image);
    }

    private void validateImage(String fileName, String contentType, byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Please select an image to upload");
        }

        if (data.length > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image is too large. Maximum size is 5 MB");
        }

        if (!isAllowedContentType(contentType) && inferContentTypeFromFileName(fileName) == null) {
            throw new IllegalArgumentException("Only PNG, JPG, GIF, and WEBP images are allowed");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        return contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
    }

    private String normalizeContentType(String fileName, String contentType) {
        if (isAllowedContentType(contentType)) {
            return contentType.toLowerCase(Locale.ROOT);
        }

        String inferred = inferContentTypeFromFileName(fileName);
        return inferred == null ? "application/octet-stream" : inferred;
    }

    private String inferContentTypeFromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        String normalized = fileName.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".png")) {
            return "image/png";
        }
        if (normalized.endsWith(".jpg") || normalized.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (normalized.endsWith(".gif")) {
            return "image/gif";
        }
        if (normalized.endsWith(".webp")) {
            return "image/webp";
        }

        return null;
    }
}

