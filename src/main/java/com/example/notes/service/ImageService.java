package com.example.notes.service;

import com.example.notes.data.dto.ImageDto;
import com.example.notes.data.dto.ImageThumbnailDto;
import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import com.example.notes.data.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final FileService fileService;

    private final String uploadDir = "uploads/";
    private final UserRepository userRepository;

    public ImageService(ImageRepository imageRepository, UserRepository userRepository,  FileService fileService) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    public void saveImage(String fileName, InputStream inputStream, long fileSize, User user) throws IOException {

        String format = fileService.extractAndValidateFormat(fileName);

        byte[] imageBytes = inputStream.readAllBytes();

        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file");
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Long id = generateSecureRandomId();

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File originalFile = new File(uploadDir + id + "." + format);
        File thumbFile = new File(uploadDir + id + "_thumb.jpg");

        try {
            fileService.saveFiles(imageBytes, originalFile, thumbFile);

            saveImageToDatabase(id, fileName, fileSize, format, width, height, user);

        } catch (Exception e) {
            fileService.deleteFileIfExists(originalFile);
            fileService.deleteFileIfExists(thumbFile);
            throw e;
        }
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

    public void deleteImage(Long id, User user) throws IOException {

        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        if (!img.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        deleteImageFromDatabase(img);

        fileService.deleteFileIfExists(Paths.get(uploadDir).resolve(String.valueOf(id)+"."+img.getFormat()).toFile());
        fileService.deleteFileIfExists(Paths.get(uploadDir).resolve(String.valueOf(id)+"_thumb.jpg").toFile());


    }

    @Transactional
    protected void deleteImageFromDatabase(Image img) throws IOException {
        imageRepository.delete(img);
    }

    public ImageDto getImageForUser(Long id) throws Exception {

        Image img = authorize(id);

        Path path = Paths.get(uploadDir).resolve(id +"."+ img.getFormat());

        Resource resource = null;

        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                resource = new UrlResource(path.toUri());
            } catch (MalformedURLException ignored) {}
        }

        return new ImageDto(img, resource);
    }

    public List<ImageThumbnailDto> getAllImagesByUser(String format, int sortBy) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails springUser = (UserDetails) authentication.getPrincipal();

        User loggedUser = userRepository.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Sort sort = switch (sortBy) {
            case 1 -> Sort.by("uploadTime").ascending();
            case 2 -> Sort.by("fileSize").descending();
            case 3 -> Sort.by("fileSize").ascending();
            default -> Sort.by("uploadTime").descending();
        };

        List<Image> images;

        if (format == null || format.equalsIgnoreCase("all")) {
            images = imageRepository.findByUser(loggedUser, sort);
        } else {
            images = imageRepository.findByUserAndFormatIgnoreCase(loggedUser, format, sort);
        }

        List<ImageThumbnailDto> thumbnails = new ArrayList<>();

        for (Image img : images) {
            Path thumbPath = Paths.get(uploadDir)
                    .resolve(img.getId() + "_thumb.jpg");

            Resource resource = null;

            if (Files.exists(thumbPath)) {
                try {
                    resource = new UrlResource(thumbPath.toUri());
                } catch (MalformedURLException ignored) {}
            }

            thumbnails.add(new ImageThumbnailDto(img.getId(), resource));
        }

        return thumbnails;
    }

    public void updateImage(Long imageId, byte[] imageBytes) throws Exception {

        Image existingImage = authorize(imageId);

        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (bufferedImage == null) {
            throw new IllegalArgumentException("Invalid image data");
        }

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        String format = existingImage.getFormat();

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File oldOriginal = new File(uploadDir + imageId + "." + format);
        File oldThumb = new File(uploadDir + imageId + "_thumb.jpg");

        File newOriginal = new File(uploadDir + imageId + "_new." + format);
        File newThumb = new File(uploadDir + imageId + "_thumb_new.jpg");

        try {
            fileService.saveFiles(imageBytes, newOriginal, newThumb);

            updateImageMetadata(existingImage, width, height, imageBytes.length);

            fileService.moveFiles(newOriginal.toPath(), oldOriginal.toPath(), StandardCopyOption.REPLACE_EXISTING);
            fileService.moveFiles(newThumb.toPath(), oldThumb.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {

            fileService.deleteFileIfExists(newOriginal);
            fileService.deleteFileIfExists(newThumb);
            throw e;
        }
    }

    @Transactional
    public void updateImageMetadata(Image image, int width, int height, long fileSize) {

        image.setWidth(width);
        image.setHeight(height);
        image.setFileSize(fileSize);
        image.setUploadTime(LocalDateTime.now());

        imageRepository.save(image);
    }

    private static final SecureRandom random = new SecureRandom();

    private Long generateSecureRandomId() {
        long id;
        do {
            id = Math.abs(random.nextLong());
        } while (id < 1_000_000_000L || imageRepository.existsById(id));
        return id;
    }

    private Image authorize(Long imageId) throws ResponseStatusException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetails springUser = (UserDetails) authentication.getPrincipal();
        com.example.notes.data.entity.User loggedUser = userRepository.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Image img = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!img.getUser().getId().equals(loggedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return img;
    }
}
