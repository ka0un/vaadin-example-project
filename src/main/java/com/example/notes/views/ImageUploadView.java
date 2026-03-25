package com.example.notes.views;

import com.example.notes.data.entity.ImageEntity;
import com.example.notes.data.repository.ImageRepository;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Route(value = "upload", layout = MainLayout.class)
@PageTitle("Image Upload | Vaadin Notes App")
@AnonymousAllowed
public class ImageUploadView extends VerticalLayout {

    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Image previewImage = new Image();
    private final Div galleryDiv = new Div();
    private final HorizontalLayout actionLayout = new HorizontalLayout();
    private byte[] currentImageBytes;
    private String currentFileName;
    private Button saveButton;
    private Button discardButton;

    @Autowired
    private ImageRepository imageRepository;

    public ImageUploadView(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
        initializeUI();
        loadGallery();
    }

    private void initializeUI() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Image Upload & Gallery");
        title.addClassName("image-upload-heading");

        Div uploadSection = new Div();
        uploadSection.addClassName("image-upload-section");

        // Upload section
        Upload upload = new Upload(buffer);
        upload.setWidthFull();
        upload.setAcceptedFileTypes("image/*");
        upload.setMaxFiles(1);

        upload.addSucceededListener(event -> {
            handleUploadSuccess();
        });

        upload.addFailedListener(event -> {
            String message = event.getReason() != null
                    ? event.getReason().getMessage()
                    : "Upload failed";
            Notification.show(message);
        });

        // Preview image
        previewImage.addClassName("image-preview");
        previewImage.setVisible(false);

        // Action buttons
        saveButton = new Button("Save to Database", event -> handleSave());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        discardButton = new Button("Discard", event -> handleDiscard());
        discardButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        actionLayout.removeAll();
        actionLayout.add(saveButton, discardButton);
        actionLayout.setSpacing(true);
        actionLayout.setVisible(false);

        VerticalLayout uploadContent = new VerticalLayout(upload, previewImage, actionLayout);
        uploadContent.setPadding(false);
        uploadContent.setSpacing(true);
        uploadContent.setAlignItems(Alignment.CENTER);

        uploadSection.add(uploadContent);
        add(title, uploadSection);

        // Gallery section
        H2 galleryTitle = new H2("Image Gallery");
        add(galleryTitle);

        galleryDiv.addClassName("image-gallery");
        add(galleryDiv);
    }

    private void handleUploadSuccess() {
        try {
            currentImageBytes = buffer.getInputStream().readAllBytes();
            currentFileName = buffer.getFileName();

            // Show preview
            StreamResource resource = new StreamResource(
                    currentFileName,
                    () -> new ByteArrayInputStream(currentImageBytes)
            );

            previewImage.setSrc(resource);
            previewImage.setVisible(true);
            
            // Show action buttons
            actionLayout.setVisible(true);
            
            Notification.show("Preview ready: " + currentFileName);

        } catch (IOException e) {
            Notification.show("Failed to read file: " + e.getMessage());
        }
    }

    private void handleSave() {
        if (currentImageBytes == null || currentFileName == null) {
            Notification.show("No image to save");
            return;
        }

        try {
            ImageEntity image = new ImageEntity(currentFileName, currentImageBytes);
            imageRepository.save(image);

            Notification.show("Image saved: " + currentFileName);
            handleDiscard();
            loadGallery();

        } catch (Exception e) {
            Notification.show("Failed to save image: " + e.getMessage());
        }
    }

    private void handleDiscard() {
        currentImageBytes = null;
        currentFileName = null;
        previewImage.setVisible(false);
        previewImage.setSrc("");
        
        // Hide action buttons
        actionLayout.setVisible(false);
    }

    private void loadGallery() {
        galleryDiv.removeAll();

        try {
            List<ImageEntity> images = imageRepository.findAllByOrderByCreatedAtDesc();

            if (images.isEmpty()) {
                galleryDiv.add("No images stored yet.");
                return;
            }

            for (ImageEntity imageEntity : images) {
                Div imageCard = createImageCard(imageEntity);
                galleryDiv.add(imageCard);
            }

        } catch (Exception e) {
            Notification.show("Failed to load gallery: " + e.getMessage());
        }
    }

    private Div createImageCard(ImageEntity imageEntity) {
        Div card = new Div();
        card.addClassName("image-card");

        // Display image from byte array
        byte[] imageBytes = imageEntity.getData();
        StreamResource resource = new StreamResource(
                imageEntity.getFileName(),
                () -> new ByteArrayInputStream(imageBytes)
        );

        Image img = new Image(resource, imageEntity.getFileName());
        img.addClassName("image-card-thumbnail");
        img.addClickListener(event -> openFullscreenImage(imageEntity));

        // File name label
        Div nameDiv = new Div(imageEntity.getFileName());
        nameDiv.addClassName("image-card-name");

        // Delete button
        Button deleteButton = new Button("Delete", event -> handleDelete(imageEntity));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClassName("image-card-delete");

        card.add(img, nameDiv, deleteButton);
        return card;
    }

    private void openFullscreenImage(ImageEntity imageEntity) {
        Dialog fullScreenDialog = new Dialog();
        fullScreenDialog.setModal(true);
        fullScreenDialog.setCloseOnEsc(true);
        fullScreenDialog.setCloseOnOutsideClick(true);
        fullScreenDialog.setWidth("100vw");
        fullScreenDialog.setHeight("100vh");
        fullScreenDialog.addClassName("fullscreen-dialog");

        // Header with close button
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.addClassName("fullscreen-header");
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.END);
        headerLayout.setPadding(true);
        headerLayout.setSpacing(false);

        Button closeButton = new Button("×", event -> {
            fullScreenDialog.close();
        });
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClassName("fullscreen-image-close");
        headerLayout.add(closeButton);

        // Center layout for image
        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.addClassName("fullscreen-image-layout");
        centerLayout.setSizeFull();
        centerLayout.setPadding(false);
        centerLayout.setSpacing(false);
        centerLayout.setAlignItems(Alignment.CENTER);
        centerLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        StreamResource fullImageResource = new StreamResource(
                imageEntity.getFileName(),
                () -> new ByteArrayInputStream(imageEntity.getData())
        );
        Image fullImage = new Image(fullImageResource, imageEntity.getFileName());
        fullImage.addClassName("fullscreen-image");
        centerLayout.add(fullImage);

        // Main dialog layout
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.add(headerLayout, centerLayout);
        dialogLayout.expand(centerLayout);

        fullScreenDialog.add(dialogLayout);
        fullScreenDialog.open();
    }

    private void handleDelete(ImageEntity imageEntity) {
        try {
            imageRepository.delete(imageEntity);
            Notification.show("Image deleted: " + imageEntity.getFileName());
            loadGallery();
        } catch (Exception e) {
            Notification.show("Failed to delete image: " + e.getMessage());
        }
    }
}