package com.example.notes.data.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    // Which user uploaded this image
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ImageEntity() {}

    public ImageEntity(String fileName, String filePath, String mimeType, User user) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.uploadedAt = LocalDateTime.now();
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

}
