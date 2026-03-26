package com.example.notes.data.repository;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUser(User user);
    Optional<Image> findByIdAndUserId(Long id, Long id1);
}