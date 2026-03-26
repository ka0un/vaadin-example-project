package com.example.notes.data.entity;


import com.vaadin.flow.server.StreamResource;
import jakarta.persistence.*;

@Entity
public class UserImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String contentType;

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] data;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserImage() {
    }

    public UserImage(String fileName, String contentType, byte[] data, User user) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
        this.user = user;
    }

    public UserImage(String fileName, String contentType, byte[] data) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
