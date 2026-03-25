package com.example.notes.data.entity;

import jakarta.persistence.*;

@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String filePath;

    // Added field for Favorites logic
    private boolean favorite = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 🔽 Constructors
    public Image() {}

    public Image(String fileName, String filePath, User user) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.user = user;
        this.favorite = false; // Default to not favorite
    }

    // 🔽 Getters & Setters
    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // 🔽 Added Getters & Setters for Favorites
    public boolean isFavorite() { 
        return favorite; 
    }

    public void setFavorite(boolean favorite) { 
        this.favorite = favorite; 
    }
}