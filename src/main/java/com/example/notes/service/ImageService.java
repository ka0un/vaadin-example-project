package com.example.notes.service;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ImageService {
    //Handles the core logic of saving an image.
    private final ImageRepository imageRepository;

    private final String uploadDir = "uploads/images/";

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Image saveImage(MemoryBuffer buffer, User user) throws IOException {
        return saveImage(buffer.getFileName(), buffer.getInputStream(), user);
    }

    //Validates file extension for security.
    public Image saveImage(String originalName, InputStream inputStream, User user) throws IOException {
        String normalizedName = originalName == null ? "" : originalName.toLowerCase(Locale.ENGLISH);


        // Robust Handling: Ensure only actual images are uploaded by checking file extensions.
        if (!normalizedName.matches(".*\\.(png|jpg|jpeg|gif|webp)$")) {
            throw new IllegalArgumentException("Invalid file type");
        }

        //Generates a unique UUID to prevent filename collisions.
        String fileName = UUID.randomUUID() + "_" + originalName;

        //nsure the upload directory exists on the system
        File file = new File(uploadDir + fileName);
        file.getParentFile().mkdirs();

        try (InputStream input = inputStream) {
            Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        //Map the file details to our JPA Entity for database storage
        Image image = new Image();
        image.setFileName(fileName);
        image.setFilePath(file.getAbsolutePath());
        image.setUser(user);

        return imageRepository.save(image);
    }

    public List<Image> getUserImages(User user) {
        return imageRepository.findByUserOrderByIdDesc(user);
    }
}