package com.example.notes.service;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ImageDbService {

    private final ImageRepository imageRepository;

    public ImageDbService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Transactional
    public void saveImageToDatabase(Long id,
                                    String fileName,
                                    long fileSize,
                                    String format,
                                    int width,
                                    int height,
                                    User user) {

        Image image = new Image();
        image.setId(id);
        image.setFileName(fileName);
        image.setFileSize(fileSize);
        image.setFormat(format);
        image.setWidth(width);
        image.setHeight(height);
        image.setUploadTime(LocalDateTime.now());
        image.setUser(user);

        imageRepository.save(image);
    }

    @Transactional
    protected void deleteImageFromDatabase(Image img) {
        imageRepository.delete(img);
    }

    @Transactional
    public void updateImageMetadata(Image image, int width, int height, long fileSize) {

        image.setWidth(width);
        image.setHeight(height);
        image.setFileSize(fileSize);
        image.setUploadTime(LocalDateTime.now());

        imageRepository.save(image);
    }
}
