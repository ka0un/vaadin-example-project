package com.example.notes.data.repository;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUserOrderByIdDesc(User user);
}