package com.example.notes.data.repository;

import com.example.notes.data.entity.ImageItem;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Database access for ImageItem entities.
 */
public interface ImageItemRepository extends JpaRepository<ImageItem, Long> {

    /** Returns all images owned by the given user, sorted newest-first. */
    List<ImageItem> findByUserOrderByUploadedAtDesc(User user);

    /** Finds an image by ID only if it belongs to the given user (prevents cross-user access). */
    Optional<ImageItem> findByIdAndUser(Long id, User user);
}
