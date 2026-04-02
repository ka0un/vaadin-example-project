package com.example.notes.service;

import com.example.notes.data.dto.ImageDto;
import com.example.notes.data.dto.ImageThumbnailDto;
import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.ImageRepository;
import com.example.notes.data.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserRepository userRepository;
    private final Path uploadDir;
    private final ImageDbService imageDbService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    public ImageService(ImageRepository imageRepository,
                        UserRepository userRepository,
                        FileService fileService,
                        @Value("${notes.upload-dir:uploads}") String uploadDir,
                        ImageDbService imageDbService) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.imageDbService = imageDbService;
        this.uploadDir = Path.of(uploadDir).normalize();
    }

    public void saveImage(String fileName, InputStream inputStream, long fileSize, User user) throws IllegalArgumentException, IOException {

        String format = fileService.extractAndValidateFormat(fileName);

        byte[] imageBytes = inputStream.readAllBytes();

        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IllegalArgumentException();
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Long id = generateSecureRandomId();

        ensureUploadDirectoryExists();

        File originalFile = resolveUploadPath(id + "." + format).toFile();
        File thumbFile = resolveUploadPath(id + "_thumb.jpg").toFile();

        try {
            fileService.saveFiles(imageBytes, originalFile, thumbFile);

            imageDbService.saveImageToDatabase(id, fileName, fileSize, format, width, height, user);

        } catch (Exception e) {
            fileService.deleteFileIfExists(originalFile);
            fileService.deleteFileIfExists(thumbFile);
            throw e;
        }
    }

    public void deleteImage(Long id, User user) throws EntityNotFoundException, AccessDeniedException {

        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        if (!img.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        imageDbService.deleteImageFromDatabase(img);

        boolean originalDeleted = fileService.deleteFileIfExists(resolveUploadPath(id + "." + img.getFormat()).toFile());

        boolean thumbDeleted = fileService.deleteFileIfExists(resolveUploadPath(id + "_thumb.jpg").toFile());

        if (!originalDeleted || !thumbDeleted) {
            LOGGER.warn("File deletion failed for image id: {}", id);
        }

    }

    public ImageDto getImageForUser(Long id) throws IOException {

        Image img = authorize(id);

        Path path = resolveUploadPath(id +"."+ img.getFormat());

        Resource resource = createResourceIfPresent(path);

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
            Path thumbPath = resolveUploadPath(img.getId() + "_thumb.jpg");

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

        ensureUploadDirectoryExists();

        File oldOriginal = resolveUploadPath(imageId + "." + format).toFile();
        File oldThumb = resolveUploadPath(imageId + "_thumb.jpg").toFile();

        File newOriginal = resolveUploadPath(imageId + "_new." + format).toFile();
        File newThumb = resolveUploadPath(imageId + "_thumb_new.jpg").toFile();

        try {
            fileService.saveFiles(imageBytes, newOriginal, newThumb);

            fileService.moveFiles(newOriginal.toPath(), oldOriginal.toPath(), StandardCopyOption.REPLACE_EXISTING);
            fileService.moveFiles(newThumb.toPath(), oldThumb.toPath(), StandardCopyOption.REPLACE_EXISTING);

            imageDbService.updateImageMetadata(existingImage, width, height, imageBytes.length);

        } catch (Exception e) {

            fileService.deleteFileIfExists(newOriginal);
            fileService.deleteFileIfExists(newThumb);
            throw e;
        }
    }

    private static final SecureRandom random = new SecureRandom();

    private void ensureUploadDirectoryExists() throws IOException {
        Files.createDirectories(uploadDir);
    }

    private Path resolveUploadPath(String fileName) {
        return uploadDir.resolve(fileName);
    }

    private Resource createResourceIfPresent(Path path) throws IOException {
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return null;
        }

        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException exception) {
            throw new IOException("Failed to create resource for " + path, exception);
        }
    }

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
