package com.example.notes.service;

import com.example.notes.data.entity.ImageEntity;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public ImageEntity saveImage(ImageEntity image) {
        return imageRepository.save(image);
    }

    public List<ImageEntity> getImagesByUser(User user) {
        return imageRepository.findByUser(user);
    }

    public void deleteImage(ImageEntity image) {
        imageRepository.delete(image);
    }

    public void deleteImageById(Long id) {
        imageRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ImageEntity> getImageById(Long id) {
        return imageRepository.findById(id);
    }
}
