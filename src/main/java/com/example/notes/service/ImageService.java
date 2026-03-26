package com.example.notes.service;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserImage;
import com.example.notes.data.repository.ImageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageService {
    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }


    @Transactional
    public List<UserImage> saveAll(List<UserImage> images) {
        return imageRepository.saveAll(images);
    }

    public List<UserImage> getByUser(User user) {
        return imageRepository.findByUser(user);
    }

    @Transactional
    public void deleteImagesByIds(List<Long> ids) {
        imageRepository.deleteAllByIdInBatch(ids);
    }
}
