package com.example.notes.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents an uploaded image stored on disk with metadata in the database.
 * Each image belongs to a single user.
 */
@Entity
@Table(name = "image_item")
public class ImageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique filename used on disk (UUID-based) */
    @Column(nullable = false)
    private String filename;

    /** The original name the user uploaded */
    @Column(nullable = false)
    private String originalName;

    /** Optional user-supplied caption */
    @Column(length = 512)
    private String caption;

    /** Timestamp of when the image was uploaded */
    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    /** The user who owns this image */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ImageItem() {}

    public ImageItem(String filename, String originalName, String caption,
                     LocalDateTime uploadedAt, User user) {
        this.filename = filename;
        this.originalName = originalName;
        this.caption = caption;
        this.uploadedAt = uploadedAt;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
