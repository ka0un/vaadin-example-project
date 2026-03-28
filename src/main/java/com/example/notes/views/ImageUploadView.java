package com.example.notes.views;

import com.example.notes.service.ImageInfo;
import com.example.notes.service.ImageStorageService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Route("images")
public class ImageUploadView extends VerticalLayout {

    // Service layer to handle file storage operations
    private final ImageStorageService storageService;

    // Upload component for handling multi-file uploads
    private final Upload upload;

    // Layout to display image cards responsively
    private final FlexLayout galleryLayout;

    // Search field for filtering images by name
    private final TextField searchField;

    // Label to show total number of images
    private final Span countLabel;

    public ImageUploadView(ImageStorageService storageService) {
        this.storageService = storageService;

        // Basic layout configuration
        setWidthFull();
        setSpacing(true);
        setPadding(true);

        // Apply gradient background and full screen height
        getStyle()
                .set("background", "linear-gradient(135deg, #77a6f7, #b8cdf2)")
                .set("min-height", "100vh");

        // Page title styling
        H2 title = new H2("Image Upload Gallery");
        title.getStyle()
                .set("font-weight", "700")
                .set("margin-bottom", "10px");

        // Buffer to temporarily store multiple uploaded files in memory
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();

        upload = new Upload(buffer);

        // UI styling for upload box
        upload.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "10px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)");

        // Restrict accepted file types (image only)
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg");

        // Limit file size (5MB per file)
        upload.setMaxFileSize(5 * 1024 * 1024);

        // Limit total number of files per upload session
        upload.setMaxFiles(10);

        // Handle rejected files (wrong type, too large, etc.)
        upload.addFileRejectedListener(event ->
                Notification.show("Upload rejected: " + event.getErrorMessage()));

        // Handle general upload failures
        upload.addFailedListener(event ->
                Notification.show("Upload failed. Please check file type, size, or try again."));

        // Handle successful upload
        upload.addSucceededListener(event -> {
            try {
                // Save uploaded file using service layer
                storageService.saveFile(
                        event.getFileName(),
                        buffer.getInputStream(event.getFileName())
                );

                Notification.show("Image uploaded successfully");

                // Refresh gallery after upload
                refreshGallery();

            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
        });

        // Search field configuration
        searchField = new TextField();
        searchField.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,0.08)");

        searchField.setPlaceholder("Search images...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("300px");

        // EAGER → triggers filtering while typing (better UX)
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        // Refresh gallery whenever search input changes
        searchField.addValueChangeListener(event -> refreshGallery());

        countLabel = new Span();

        // Toolbar layout (search + count)
        HorizontalLayout toolbar = new HorizontalLayout(searchField, countLabel);
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // Expand search field to take available space
        toolbar.expand(searchField);

        // Gallery layout (responsive grid-like behavior)
        galleryLayout = new FlexLayout();
        galleryLayout.setWidthFull();

        // Enable wrapping for responsiveness
        galleryLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        // Add spacing between cards
        galleryLayout.getStyle().set("gap", "16px");

        add(title, upload, toolbar, galleryLayout);

        // Initial load of images
        refreshGallery();
    }

    private void refreshGallery() {

        // Clear existing UI components
        galleryLayout.removeAll();

        // Fetch all image metadata from service layer
        List<ImageInfo> images = storageService.getAllImageInfos();

        // Normalize search keyword (null-safe)
        String keyword = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        // Apply filtering if user entered a search keyword
        if (!keyword.isEmpty()) {
            images = images.stream()
                    .filter(image ->
                            image.getDisplayName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        // Update total count label
        countLabel.setText("Total images: " + images.size());

        // Handle empty state
        if (images.isEmpty()) {
            galleryLayout.add(new Paragraph("No images found."));
            return;
        }

        // Loop through images and render UI cards
        for (ImageInfo imageInfo : images) {

            // File reference for reading image from disk
            File file = new File("uploads/" + imageInfo.getStoredName());

            // StreamResource allows Vaadin to serve file dynamically
            StreamResource resource = new StreamResource(
                    imageInfo.getStoredName(),
                    () -> {
                        try {
                            return new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });

            // Create image component
            Image img = new Image(resource, imageInfo.getDisplayName());

            // Create reusable card component (with delete callback)
            ImageCard card = new ImageCard(img, imageInfo, () -> {
                try {
                    storageService.deleteFile(imageInfo.getStoredName());

                    // Refresh UI after deletion
                    refreshGallery();

                } catch (Exception e) {
                    Notification.show(e.getMessage());
                }
            });

            galleryLayout.add(card);
        }
    }
}