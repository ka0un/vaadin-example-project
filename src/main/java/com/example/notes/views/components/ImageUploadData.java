package com.example.notes.views.components;

/**
 * Immutable value object for transferring upload form data.
 */
public record ImageUploadData(String caption, String imageName, String imageMimeType, byte[] imageData) {
}
