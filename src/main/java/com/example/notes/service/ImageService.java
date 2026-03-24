package com.example.notes.service;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    private final String uploadDir = "uploads/";

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public void saveImage(String fileName, InputStream inputStream, long fileSize, User user) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        inputStream.transferTo(baos);
        byte[] imageBytes = baos.toByteArray();

        // Extract resolution
        java.awt.image.BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // Extract format
        String format = fileName.substring(fileName.lastIndexOf('.') + 1);

        // Save file
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File targetFile = new File(uploadDir + fileName);
        Files.write(targetFile.toPath(), imageBytes);

        // Save to DB
        Image image = new Image();
        image.setFileName(fileName);
        image.setFilePath(targetFile.getAbsolutePath());
        image.setFileSize(fileSize);
        image.setFormat(format);
        image.setWidth(width);
        image.setHeight(height);
        image.setUploadTime(LocalDateTime.now());

        image.setUser(user);

        imageRepository.save(image);
    }

    @Transactional
    public void deleteImage(Image img, User user) throws IOException {
        System.out.println("Deleting image " + img.getFilePath());
        imageRepository.deleteByFileNameAndUser(img.getFileName(), user);
        System.out.println("reached");
        Path path = Paths.get(uploadDir).resolve(img.getFileName());
        Files.deleteIfExists(path);
    }

    public List<Image> getAllImagesByUser(User user) {
        return imageRepository.findByUser(user);
    }
}
