package com.example.notes.views;

import com.example.notes.data.entity.ImageEntity;
import com.example.notes.data.repository.ImageRepository;
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

        Div uploadSection = new Div();
        uploadSection.getStyle().set("width", "100%");
        uploadSection.getStyle().set("max-width", "720px");
        uploadSection.getStyle().set("margin", "0 auto");
        uploadSection.getStyle().set("padding", "16px");
        uploadSection.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        uploadSection.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        uploadSection.getStyle().set("background", "var(--lumo-base-color)");

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
        previewImage.setWidth("350px");
        previewImage.setHeight("350px");
        previewImage.getStyle().set("object-fit", "contain");
        previewImage.getStyle().set("border", "1px solid #ccc");
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

        galleryDiv.getStyle().set("display", "flex");
        galleryDiv.getStyle().set("flex-wrap", "wrap");
        galleryDiv.getStyle().set("justify-content", "center");
        galleryDiv.getStyle().set("gap", "16px");
        galleryDiv.getStyle().set("padding", "10px 0");
        galleryDiv.getStyle().set("width", "100%");
        galleryDiv.getStyle().set("max-width", "960px");
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
        card.getStyle().set("border", "1px solid #ddd");
        card.getStyle().set("border-radius", "4px");
        card.getStyle().set("padding", "10px");
        card.getStyle().set("text-align", "center");
        card.getStyle().set("width", "220px");
        card.getStyle().set("background", "var(--lumo-base-color)");

        // Display image from byte array
        byte[] imageBytes = imageEntity.getData();
        StreamResource resource = new StreamResource(
                imageEntity.getFileName(),
                () -> new ByteArrayInputStream(imageBytes)
        );

        Image img = new Image(resource, imageEntity.getFileName());
        img.setWidth("100%");
        img.setHeight("200px");
        img.getStyle().set("object-fit", "cover");

        // File name label
        Div nameDiv = new Div(imageEntity.getFileName());
        nameDiv.getStyle().set("font-weight", "bold");
        nameDiv.getStyle().set("margin-top", "10px");
        nameDiv.getStyle().set("word-break", "break-word");

        // Delete button
        Button deleteButton = new Button("Delete", event -> handleDelete(imageEntity));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.getStyle().set("margin-top", "10px");
        deleteButton.getStyle().set("width", "100%");

        card.add(img, nameDiv, deleteButton);
        return card;
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