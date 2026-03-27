package com.example.notes.service;

import com.example.notes.data.dto.ImageDto;
import com.example.notes.data.dto.ImageThumbnailDto;
import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import com.example.notes.data.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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

        // 1. Validate file name
        String format = fileService.extractAndValidateFormat(fileName);

        // 2. Read into memory (you can later optimize this)
        byte[] imageBytes = inputStream.readAllBytes();

        // 3. Validate image content
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
        File thumbFile = new File(uploadDir + id + "_thumb.jpg"); // always jpg for thumbnails

        try {
            // 4. Save files FIRST
            fileService.saveFiles(imageBytes, originalFile, thumbFile);

            // 5. Save DB (transactional)
            saveImageToDatabase(id, fileName, fileSize, format, width, height, user);

        } catch (Exception e) {
            // 6. Cleanup if anything fails
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
        fileService.deleteFileIfExists(Paths.get(uploadDir).resolve(String.valueOf(id)+"_thumb."+img.getFormat()).toFile());


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

    public List<ImageThumbnailDto> getAllImagesByUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails springUser = (UserDetails) authentication.getPrincipal();

        com.example.notes.data.entity.User loggedUser =
                userRepository.findByUsername(springUser.getUsername())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Image> images = imageRepository.findByUser(loggedUser);
        List<ImageThumbnailDto> thumbnails = new ArrayList<>();

        for (Image img : images) {
            Path thumbPath = Paths.get(uploadDir).resolve(img.getId() + "_thumb." + img.getFormat());
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

        // 1. Authorization
        Image existingImage = authorize(imageId);

        // 2. Validate image
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (bufferedImage == null) {
            throw new IllegalArgumentException("Invalid image data");
        }

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        String format = existingImage.getFormat(); // keep same format

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // OLD FILES
        File oldOriginal = new File(uploadDir + imageId + "." + format);
        File oldThumb = new File(uploadDir + imageId + "_thumb.jpg");

        // TEMP FILES (important!)
        File newOriginal = new File(uploadDir + imageId + "_new." + format);
        File newThumb = new File(uploadDir + imageId + "_thumb_new.jpg");

        try {
            // 3. Save new files FIRST (temp)
            fileService.saveFiles(imageBytes, newOriginal, newThumb);

            // 4. Update DB (transactional)
            updateImageMetadata(existingImage, width, height, imageBytes.length);

            // 5. Replace files (atomic-ish)
            fileService.moveFiles(newOriginal.toPath(), oldOriginal.toPath(), StandardCopyOption.REPLACE_EXISTING);
            fileService.moveFiles(newThumb.toPath(), oldThumb.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            // Cleanup temp files
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
