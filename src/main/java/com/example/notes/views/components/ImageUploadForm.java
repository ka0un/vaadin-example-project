package com.example.notes.views.components;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;

/**
 * Reusable upload form component for image + caption drafts.
 */
public class ImageUploadForm extends VerticalLayout {

    private static final int MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final TextArea captionField = new TextArea();
    private final Image uploadPreview = new Image();
    private final MemoryBuffer memoryBuffer = new MemoryBuffer();
    private final Upload upload = new Upload(memoryBuffer);

    private byte[] pendingImageData;
    private String pendingImageName;
    private String pendingImageMimeType;

    public ImageUploadForm(Consumer<ImageUploadData> onSave) {
        addClassName("upload-card");
        setWidthFull();
        setMaxWidth("460px");
        setPadding(true);
        setSpacing(true);

        configureCaptionField();
        configureUpload();
        configureUploadPreview();

        Button saveButton = new Button("Save to Gallery", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(click -> saveCurrentDraft(onSave));

        Button resetButton = new Button("Clear", VaadinIcon.CLOSE_SMALL.create());
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(click -> clearForm());

        HorizontalLayout actionBar = new HorizontalLayout(saveButton, resetButton);
        actionBar.setWidthFull();
        actionBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        add(captionField, upload, uploadPreview, actionBar);
    }

    private void configureCaptionField() {
        captionField.setPlaceholder("Add a caption...");
        captionField.setWidthFull();
        captionField.setMaxLength(1000);
        captionField.setClearButtonVisible(true);
        captionField.setMinHeight("100px");
    }

    private void configureUpload() {
        upload.setAcceptedFileTypes(ALLOWED_MIME_TYPES.toArray(String[]::new));
        upload.setMaxFiles(1);
        upload.setMaxFileSize(MAX_FILE_SIZE_BYTES);
        upload.setDropAllowed(true);
        upload.setWidthFull();

        upload.addSucceededListener(event -> onUploadSucceeded(event, memoryBuffer));
        upload.addFileRejectedListener(event -> showError(event.getErrorMessage()));
        upload.addFailedListener(event -> {
            clearPendingImage();
            uploadPreview.setVisible(false);
            showError("Upload failed. Please try again.");
        });
    }

    private void configureUploadPreview() {
        uploadPreview.setAlt("Selected image preview");
        uploadPreview.setVisible(false);
        uploadPreview.addClassName("upload-preview");
        uploadPreview.setWidthFull();
    }

    private void onUploadSucceeded(SucceededEvent event, MemoryBuffer buffer) {
        byte[] uploadedBytes;
        try {
            uploadedBytes = buffer.getInputStream().readAllBytes();
        } catch (IOException ioException) {
            clearPendingImage();
            uploadPreview.setVisible(false);
            showError("Could not read uploaded image.");
            return;
        }

        if (uploadedBytes.length == 0) {
            clearPendingImage();
            uploadPreview.setVisible(false);
            showError("Uploaded file is empty.");
            return;
        }

        if (uploadedBytes.length > MAX_FILE_SIZE_BYTES) {
            clearPendingImage();
            uploadPreview.setVisible(false);
            showError("File is too large. Maximum size is 5 MB.");
            return;
        }

        String mimeType = event.getMIMEType();
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            clearPendingImage();
            uploadPreview.setVisible(false);
            showError("Unsupported file type. Please upload JPEG, PNG, GIF, or WEBP.");
            return;
        }

        pendingImageData = uploadedBytes;
        pendingImageName = event.getFileName();
        pendingImageMimeType = mimeType;

        StreamResource previewResource = new StreamResource(
                "preview-" + UUID.randomUUID(),
                () -> new ByteArrayInputStream(pendingImageData)
        );
        previewResource.setContentType(pendingImageMimeType);
        uploadPreview.setSrc(previewResource);
        uploadPreview.setVisible(true);
    }

    private void saveCurrentDraft(Consumer<ImageUploadData> onSave) {
        String caption = captionField.getValue() == null ? "" : captionField.getValue().trim();
        if (caption.length() > 1000) {
            showError("Caption is too long. Maximum 1000 characters.");
            return;
        }

        boolean hasImage = pendingImageData != null && pendingImageData.length > 0;
        if (!hasImage) {
            showWarning("Please upload an image before saving.");
            return;
        }

        ImageUploadData data = new ImageUploadData(
                caption,
                pendingImageName,
                pendingImageMimeType,
                pendingImageData.clone()
        );
        onSave.accept(data);
        clearForm();
    }

    public void clearForm() {
        captionField.clear();
        upload.clearFileList();
        uploadPreview.setVisible(false);
        clearPendingImage();
    }

    private void clearPendingImage() {
        pendingImageData = null;
        pendingImageName = null;
        pendingImageMimeType = null;
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3200, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showWarning(String message) {
        Notification notification = Notification.show(message, 2800, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }
}
