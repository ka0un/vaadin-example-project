package com.example.notes.data.repository;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    /** Fetch all images for a given user */
    List<Image> findByUser(User user);

    /** Fetch images for a user whose name contains the search term (case-insensitive) */
    List<Image> findByUserAndNameContainingIgnoreCase(User user, String name);

    /** Fetch only favourite images for a user */
    List<Image> findByUserAndFavoriteTrue(User user);

    /** Count total images uploaded by a user */
    long countByUser(User user);
}
