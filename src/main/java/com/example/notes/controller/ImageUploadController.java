package com.example.notes.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    //  Project root uploads folder
    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {

        try {
            File dir = new File(UPLOAD_DIR);

            // Create folder if not exists
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Save file
            Path path = Paths.get(UPLOAD_DIR, file.getOriginalFilename());
            file.transferTo(path.toFile());

            System.out.println("File saved at: " + path.toAbsolutePath());

            return ResponseEntity.ok("Upload successful");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}