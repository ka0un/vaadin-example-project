package com.example.notes.data.repository;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<UserImage, Long> {

    List<UserImage> findByUser(User user);
    List<UserImage> findByUserId(Long userId);

}
