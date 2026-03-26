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

    private final String uploadDir = "uploads/";
    private final UserRepository userRepository;

    public ImageService(ImageRepository imageRepository, UserRepository userRepository) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void saveImage(String fileName, InputStream inputStream, long fileSize, User user) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        inputStream.transferTo(baos);
        byte[] imageBytes = baos.toByteArray();

        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        String format = fileName.substring(fileName.lastIndexOf('.') + 1);
        Long id = generateSecureRandomId();

        Image image = new Image();
        image.setId(id);
        image.setFileName(fileName);
        image.setFileSize(fileSize);
        image.setFormat(format);
        image.setWidth(width);
        image.setHeight(height);
        image.setUploadTime(LocalDateTime.now());
        image.setUser(user);

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File targetFile = new File(uploadDir + id + "." + format);
        Files.write(targetFile.toPath(), imageBytes);

        String thumbPath = uploadDir + id + "_thumb." + format;

        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(300, 300) // auto keeps aspect ratio
                .toFile(new File(thumbPath));

        imageRepository.save(image);
    }

    @Transactional
    public void deleteImage(Long id, User user) throws IOException {

        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        if (!img.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        Files.deleteIfExists(Paths.get(uploadDir).resolve(String.valueOf(id)+"."+img.getFormat()));
        Files.deleteIfExists(Paths.get(uploadDir).resolve(String.valueOf(id)+"_thumb."+img.getFormat()));

        imageRepository.delete(img);
    }

    public ImageDto getImageForUser(Long id){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetails springUser = (UserDetails) authentication.getPrincipal();
        com.example.notes.data.entity.User loggedUser = userRepository.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Image img = imageRepository.findByIdAndUserId(id, loggedUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        Path path = Paths.get(uploadDir).resolve(id +"."+ img.getFormat());

        Resource resource = null;

        try {
            resource = new UrlResource(path.toUri());
        }catch (MalformedURLException ignored){}

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
            if (!Files.exists(thumbPath)) {
                // skip or optionally generate thumbnail on the fly
                continue;
            }

            Resource resource = null;
            try {
                resource = new UrlResource(thumbPath.toUri());
            } catch (MalformedURLException ignored) {}

            thumbnails.add(new ImageThumbnailDto(img.getId(), resource));
        }

        return thumbnails;
    }

    private static final SecureRandom random = new SecureRandom();

    private Long generateSecureRandomId() {
        long id;
        do {
            id = Math.abs(random.nextLong());
        } while (id < 1_000_000_000L || imageRepository.existsById(id));
        return id;
    }
}
