package com.example.notes.data.entity;

import jakarta.persistence.*;

@Entity
public class ImageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String mimeType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 10485760) // Allows files up to 10MB in the database
    private byte[] data;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
}