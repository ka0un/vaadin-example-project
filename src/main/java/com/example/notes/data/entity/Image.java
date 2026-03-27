package com.example.notes.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing an uploaded image.
 * Stores image binary data along with metadata like upload date, size, dimensions, and favorite status.
 */
@Entity
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contentType;

    @Lob
    @Column(columnDefinition = "BLOB", nullable = false)
    private byte[] data;

    /** Upload timestamp, set automatically on save */
    @Column(nullable = false)
    private LocalDateTime uploadDate;

    /** Size in bytes */
    @Column(nullable = false)
    private long fileSize;

    /** Image width in pixels (0 if unknown) */
    @Column(nullable = false)
    private int width;

    /** Image height in pixels (0 if unknown) */
    @Column(nullable = false)
    private int height;

    /** Whether the user has marked this image as a favourite */
    @Column(nullable = false)
    private boolean favorite = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Image() {}

    public Image(String name, String contentType, byte[] data,
                 LocalDateTime uploadDate, long fileSize, int width, int height,
                 User user) {
        this.name = name;
        this.contentType = contentType;
        this.data = data;
        this.uploadDate = uploadDate;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.user = user;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
