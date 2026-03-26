package com.example.notes.views;

import com.example.notes.service.ImageService;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

/**
 * GalleryView — main view for the Image Upload & Gallery feature.
 * Allows authenticated users to upload, view, and delete images.
 * Uses ImageCard component for each individual image display.
 */
@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Image Gallery")
@PermitAll
public class GalleryView extends VerticalLayout {

    private final ImageService imageService;

    // Grid container for image cards
    private final Div galleryGrid = new Div();

    public GalleryView(ImageService imageService) {
        this.imageService = imageService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildTitle(), buildUploadComponent(), buildGalleryGrid());
        refreshGallery();
    }

    private H2 buildTitle() {
        return new H2("Image Gallery");
    }

    private Upload buildUploadComponent() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        upload.setMaxFileSize(10 * 1024 * 1024); // 10MB
        upload.setDropLabel(new Span("Drop images here (JPG, PNG, GIF, WEBP — max 10MB)"));

        // On successful upload — save and refresh gallery
        upload.addSucceededListener(event -> {
            imageService.saveImage(
                    event.getFileName(),
                    event.getMIMEType(),
                    buffer.getInputStream()
            );
            Notification.show("Image uploaded successfully!")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshGallery();
        });

        // On failed upload — show error message
        upload.addFailedListener(event ->
                Notification.show("Upload failed: " + event.getReason().getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR)
        );

        return upload;
    }

    private Div buildGalleryGrid() {
        // CSS grid: auto-fills columns, min 200px each
        galleryGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))")
                .set("gap", "16px")
                .set("width", "100%")
                .set("margin-top", "16px");
        return galleryGrid;
    }

    // Clears and reloads all image cards from the database
    private void refreshGallery() {
        galleryGrid.removeAll();

        var images = imageService.getAllImages();

        if (images.isEmpty()) {
            Paragraph empty = new Paragraph("No images yet. Upload one above!");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            galleryGrid.add(empty);
            return;
        }

        // Create one ImageCard per image
        images.forEach(image ->
                galleryGrid.add(new ImageCard(image, imageService, this::refreshGallery))
        );
    }
}