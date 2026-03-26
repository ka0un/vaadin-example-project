package com.example.notes.controller;

import com.example.notes.service.ImageItemService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * REST endpoint that serves raw image bytes from the local filesystem.
 * Whitelisted in {@link com.example.notes.security.SecurityConfig} so Vaadin
 * Image components can load images once the user is authenticated.
 */
@RestController
@RequestMapping("/images")
public class ImageServeController {

    private final ImageItemService imageItemService;

    public ImageServeController(ImageItemService imageItemService) {
        this.imageItemService = imageItemService;
    }

    /**
     * Serves a stored image file by its UUID-based filename.
     * Returns 404 if the file does not exist on disk.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> serveImage(@PathVariable String filename) throws IOException {
        Path filePath = imageItemService.resolveFilePath(filename);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] data = Files.readAllBytes(filePath);
        String mimeType = Files.probeContentType(filePath);
        MediaType mediaType = mimeType != null
                ? MediaType.parseMediaType(mimeType)
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(data);
    }
}
