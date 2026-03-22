package com.example.notes.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(length = 255)
    private String imageName;

    @Column(length = 100)
    private String imageMimeType;

    @Lob
    @Column
    private byte[] imageData;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Note() {}

    public Note(String content, User user) {
        this.content = content;
        this.user = user;
    }

    public Note(String content, String imageName, String imageMimeType, byte[] imageData, User user) {
        this.content = content;
        this.imageName = imageName;
        this.imageMimeType = imageMimeType;
        this.imageData = imageData;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageMimeType() {
        return imageMimeType;
    }

    public void setImageMimeType(String imageMimeType) {
        this.imageMimeType = imageMimeType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public boolean hasImage() {
        return imageData != null && imageData.length > 0;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
