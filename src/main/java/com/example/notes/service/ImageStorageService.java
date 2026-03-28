package com.example.notes.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ImageStorageService {

    // Directory where uploaded images are stored
    private final String uploadDir = "uploads/";

    public ImageStorageService() {
        // Ensure the upload directory exists (create it if necessary)
        new File(uploadDir).mkdirs();
    }

    public String saveFile(String fileName, InputStream inputStream) throws Exception {

        // Convert filename to lowercase for consistent validation
        String lowerFileName = fileName.toLowerCase();

        // Validate file type (only allow supported image formats)
        if (!(lowerFileName.endsWith(".png") || lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg"))) {
            throw new Exception("Only PNG, JPG, and JPEG files are allowed.");
        }

        // Generate a unique filename to avoid overwriting existing files
        String newFileName = UUID.randomUUID() + "_" + fileName;

        File file = new File(uploadDir + newFileName);

        // Save uploaded file to disk
        // try-with-resources ensures the stream is closed automatically
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            inputStream.transferTo(outputStream);
        }

        return newFileName;
    }

    public void deleteFile(String fileName) throws Exception {

        File file = new File(uploadDir + fileName);

        // Check whether the file exists before attempting deletion
        if (!file.exists()) {
            throw new Exception("File not found.");
        }

        // Attempt deletion and handle failure case
        if (!file.delete()) {
            throw new Exception("Could not delete file.");
        }
    }

    public List<ImageInfo> getAllImageInfos() {

        File folder = new File(uploadDir);
        File[] files = folder.listFiles();

        List<ImageInfo> imageInfos = new ArrayList<>();

        if (files != null) {

            // Formatter used to display upload date and time in the UI
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (File file : files) {

                String storedName = file.getName();

                // Extract the original filename by removing the UUID prefix
                String displayName = storedName;
                int underscoreIndex = storedName.indexOf("_");

                // Example:
                // storedName = "uuid_filename.jpg"
                // displayName = "filename.jpg"
                if (underscoreIndex != -1 && underscoreIndex + 1 < storedName.length()) {
                    displayName = storedName.substring(underscoreIndex + 1);
                }

                // Convert file size to KB (minimum displayed value is 1 KB)
                long sizeKb = Math.max(1, file.length() / 1024);

                // Convert last modified timestamp into a readable date/time string
                String uploadedAt = Instant.ofEpochMilli(file.lastModified())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(formatter);

                // Create metadata object for UI rendering
                imageInfos.add(new ImageInfo(storedName, displayName, sizeKb, uploadedAt));
            }
        }

        // Sort images by uploaded time (latest first)
        imageInfos.sort(Comparator.comparing(ImageInfo::getUploadedAt).reversed());

        return imageInfos;
    }
}