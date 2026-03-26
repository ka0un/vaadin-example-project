package com.example.notes.components;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;

import java.io.IOException;
import java.io.InputStream;

/**
 * A reusable component that encapsulates file upload logic,
 * limits accepted file types and sizes, and gracefully handles upload errors.
 */
public class ImageUploadComponent extends VerticalLayout {

    private static final int MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB limit

    private final MultiFileMemoryBuffer buffer;
    private final Upload upload;
    private UploadSuccessListener uploadSuccessListener;

    // Custom functional interface to emit success events back to the parent view
    public interface UploadSuccessListener {
        void onUploadSuccess(String fileName, String mimeType, byte[] data);
    }

    public ImageUploadComponent() {
        setPadding(false);
        setSpacing(false);
        setWidthFull(); // Fill grid cell width

        this.buffer = new MultiFileMemoryBuffer();
        this.upload = new Upload(buffer);

        // Style as a card to match ImageCardComponent
        upload.setWidthFull();
        upload.setHeight("250px");
        upload.addClassNames(com.vaadin.flow.theme.lumo.LumoUtility.Background.BASE, com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius.LARGE);
        upload.getStyle().set("border", "2px dashed var(--lumo-contrast-50pct)");
        upload.getStyle().set("display", "flex");
        upload.getStyle().set("align-items", "center");
        upload.getStyle().set("justify-content", "center");
        upload.getStyle().set("box-sizing", "border-box");
        upload.getStyle().set("transition", "border-color 0.2s, background-color 0.2s");

        // Hover effect to indicate interactivity
        upload.getElement().addEventListener("mouseover", e -> upload.getStyle().set("border-color", "var(--lumo-primary-color)"));
        upload.getElement().addEventListener("mouseout", e -> upload.getStyle().set("border-color", "var(--lumo-contrast-50pct)"));

        configureUploadComponent();
        attachUploadListeners();

        add(upload);
    }

    /**
     * Registers a callback that fires when an image is successfully uploaded and
     * read into memory.
     */
    public void setUploadSuccessListener(UploadSuccessListener listener) {
        this.uploadSuccessListener = listener;
    }

    private void configureUploadComponent() {
        Button uploadButton = new Button("Upload Images");
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        // Wrapping the button to force the internal flexbox layout to break accurately to the next line
        Div buttonWrapper = new Div(uploadButton);
        buttonWrapper.getStyle().set("width", "100%");
        buttonWrapper.getStyle().set("display", "flex");
        buttonWrapper.getStyle().set("justify-content", "center");
        buttonWrapper.getStyle().set("margin-bottom", "12px");
        
        upload.setUploadButton(buttonWrapper);
        
        Span dropLabel = new Span("Drop Files Here");
        upload.setDropLabel(dropLabel);

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        upload.setMaxFiles(20);
        upload.setMaxFileSize(MAX_FILE_SIZE_BYTES);
    }

    private void attachUploadListeners() {
        // Handle successful uploads
        upload.addSucceededListener(event -> handleSuccessfulUpload(event.getFileName(), event.getMIMEType()));

        // Gracefully handle file rejections (e.g., wrong type, too large)
        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();
            showErrorNotification("Upload failed: " + errorMessage);
        });

        // Handle mid-upload failures
        upload.addFailedListener(event -> {
            showErrorNotification("An error occurred while uploading: " + event.getFileName());
        });

        // Clear the default uploaded file list gracefully once uploads complete
        upload.addAllFinishedListener(event -> upload.clearFileList());
    }

    private void handleSuccessfulUpload(String fileName, String mimeType) {
        try {
            InputStream inputStream = buffer.getInputStream(fileName);
            byte[] data = inputStream.readAllBytes();

            if (uploadSuccessListener != null) {
                uploadSuccessListener.onUploadSuccess(fileName, mimeType, data);
            }
        } catch (IOException e) {
            showErrorNotification("System error: Could not read uploaded file data.");
            e.printStackTrace(); // Log internally for debugging
        }
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
