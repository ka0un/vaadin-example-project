package com.example.notes.data.repository;

import com.example.notes.data.entity.ImageMetadata;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageMetadata, Long> {
    List<ImageMetadata> findByUserOrderByUploadedAtDesc(User user);
    long countByUser(User user);
}
