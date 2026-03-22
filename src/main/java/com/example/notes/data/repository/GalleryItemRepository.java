package com.example.notes.data.repository;

import com.example.notes.data.entity.GalleryItem;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for GalleryItem entities.
 */
public interface GalleryItemRepository extends JpaRepository<GalleryItem, Long> {
    List<GalleryItem> findByUserOrderByUploadTimeDesc(User user);
}
