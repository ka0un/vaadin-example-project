package com.example.notes.service;

/**
 * Data Transfer Object (DTO) representing image metadata.
 * This class is used to send structured image information
 * from the backend service layer to the UI layer.
 */
public class ImageInfo {

    // Unique stored filename (includes UUID to avoid conflicts)
    private final String storedName;

    // Original filename (shown to the user)
    private final String displayName;

    // File size in KB (used for UI display)
    private final long sizeKb;

    // Human-readable upload timestamp (formatted string)
    private final String uploadedAt;


    public ImageInfo(String storedName, String displayName, long sizeKb, String uploadedAt) {
        this.storedName = storedName;
        this.displayName = displayName;
        this.sizeKb = sizeKb;
        this.uploadedAt = uploadedAt;
    }

    // Getter for stored file name (used for file operations like delete/load)
    public String getStoredName() {
        return storedName;
    }

    // Getter for display name (used in UI)
    public String getDisplayName() {
        return displayName;
    }

    // Getter for file size (used in UI metadata)
    public long getSizeKb() {
        return sizeKb;
    }

    // Getter for formatted upload time (used in UI)
    public String getUploadedAt() {
        return uploadedAt;
    }
}