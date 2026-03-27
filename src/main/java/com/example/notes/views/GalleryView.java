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
 * GalleryView — main view for the Image Upload and Gallery feature.
 * Allows authenticated users to upload, view, and delete images.
 * Delegates individual image display to the reusable ImageCard component.
 */
@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Image Gallery")
@PermitAll
public class GalleryView extends VerticalLayout {

    private final ImageService imageService;
    private final Div galleryGrid = new Div();
    private final Span imageCount = new Span();

    public GalleryView(ImageService imageService) {
        this.imageService = imageService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader(), buildUploadComponent(), galleryGrid);
        refreshGallery();
    }

    /** Builds the page title and image count label */
    private Div buildHeader() {
        H2 title = new H2("Image Gallery");

        imageCount.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "14px");

        Div header = new Div(title, imageCount);
        header.getStyle().set("display", "flex")
                .set("align-items", "baseline")
                .set("gap", "16px");
        return header;
    }

    /** Builds the upload component with type/size restrictions */
    private Upload buildUploadComponent() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        upload.setMaxFileSize(10 * 1024 * 1024);
        upload.setDropLabel(new Span("Drop images here (JPG, PNG, GIF, WEBP — max 10MB)"));
        upload.setWidthFull();

        upload.addSucceededListener(event -> {
            try {
                imageService.saveImage(
                        event.getFileName(),
                        event.getMIMEType(),
                        buffer.getInputStream()
                );
                Notification.show("\"" + event.getFileName() + "\" uploaded successfully!")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGallery();
            } catch (IllegalArgumentException e) {
                // Handle validation errors gracefully
                Notification.show(e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception e) {
                Notification.show("Unexpected error during upload. Please try again.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFailedListener(event ->
                Notification.show("Upload failed: " + event.getReason().getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR)
        );

        upload.addFileRejectedListener(event ->
                Notification.show(event.getErrorMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR)
        );

        return upload;
    }

    /** Clears and rebuilds the image grid from the database */
    private void refreshGallery() {
        galleryGrid.removeAll();

        // Responsive CSS grid — adapts from 1 column on mobile to many on desktop
        galleryGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))")
                .set("gap", "16px")
                .set("width", "100%")
                .set("margin-top", "16px");

        var images = imageService.getAllImages();

        // Update count label
        imageCount.setText(images.isEmpty() ? "" : images.size() + " image(s)");

        if (images.isEmpty()) {
            Paragraph empty = new Paragraph("No images yet. Upload one above!");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            galleryGrid.add(empty);
            return;
        }

        images.forEach(image ->
                galleryGrid.add(new ImageCard(image, imageService, this::refreshGallery))
        );
    }
}