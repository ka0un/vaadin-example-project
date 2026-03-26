package com.example.notes.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;
    private String storedFileName;
    private String mimeType;
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String v) { this.originalFileName = v; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String v) { this.storedFileName = v; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String v) { this.mimeType = v; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
}