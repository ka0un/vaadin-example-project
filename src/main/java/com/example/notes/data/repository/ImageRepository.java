package com.example.notes.data.repository;

import com.example.notes.data.entity.ImageEntity;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    // Get all images belonging to a specific user
    List<ImageEntity> findByUserOrderByUploadedAtDesc(User user);
}
