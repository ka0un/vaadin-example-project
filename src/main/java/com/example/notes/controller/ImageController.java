package com.example.notes.controller;

import com.example.notes.data.dto.ImageDto;
import com.example.notes.data.dto.ImageThumbnailDto;
import com.example.notes.data.entity.Image;
import com.example.notes.data.repository.ImageRepository;
import com.example.notes.service.ImageService;
import com.example.notes.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public ImageDto getImage(@PathVariable @NotBlank String id) throws IOException {

        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        }

        return imageService.getImageForUser(Long.valueOf(id));
    }

    @GetMapping("/all")
    public List<ImageThumbnailDto> getAllThumbnails() throws IOException {
        return imageService.getAllImagesByUser();
    }
}
