package com.example.notes.data.repository;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {

    List<UserImage> findByUserOrderByCreatedAtDesc(User user);
}

