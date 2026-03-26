package com.example.notes.views.components;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserImage;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ImageUploadComponent extends HorizontalLayout {

    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final List<UserImage> pendingImages = new ArrayList<>();
    private final ImageService imageService;
    private final User currentUser;
    private final Consumer<List<UserImage>> onImagesSaved;

    // loader
    private final ProgressBar progressBar = new ProgressBar();
    private final Span progressLabel = new Span();

    public ImageUploadComponent(ImageService imageService, User currentUser, Consumer<List<UserImage>> onImagesSaved) {
        this.imageService = imageService;
        this.currentUser = currentUser;
        this.onImagesSaved = onImagesSaved;

        setWidthFull();
        setWidthFull();
        setPadding(false);
        setMargin(false);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(buildUploadCard());
    }

    private Div buildUploadCard() {
        Upload upload = new Upload(buffer);
        upload.addClassName("upload-box");
        upload.setWidthFull();

        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        upload.setMaxFiles(10);
        upload.setDropAllowed(true);

        Button saveButton = new Button("Save Images", new Icon(VaadinIcon.CLOUD_UPLOAD_O));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setVisible(false);

        // progress bar setup
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setWidthFull();
        progressLabel.addClassName("upload-progress-label");
        progressLabel.setVisible(false);

        Div uploadCard = new Div();
        uploadCard.addClassName("upload-card");
        uploadCard.add(upload, progressBar, progressLabel, saveButton);

        // stage 1 — show loader while file is being read into memory
        upload.addStartedListener(event -> {
            showLoader("Reading " + event.getFileName() + "...");
        });

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            String mimeType = event.getMIMEType();

            if (mimeType == null || !mimeType.startsWith("image/")) {
                hideLoader();
                Notification.show("Only image files are allowed");
                return;
            }

            try (InputStream inputStream = buffer.getInputStream(fileName)) {
                byte[] imageBytes = inputStream.readAllBytes();

                UserImage pending = new UserImage(fileName, mimeType, imageBytes, currentUser);
                pendingImages.add(pending);

                saveButton.setVisible(true);
                saveButton.setText("Save " + pendingImages.size() + " Image(s)");
            } catch (Exception ex) {
                Notification.show("Failed to read uploaded image");
            } finally {
                hideLoader();
            }
        });

        upload.addFailedListener(event -> hideLoader());

        upload.addFileRejectedListener(event -> {
            hideLoader();
            Notification.show("File rejected: " + event.getErrorMessage());
        });

        upload.addFileRemovedListener(event -> {
            pendingImages.removeIf(img -> img.getFileName().equals(event.getFileName()));
            if (pendingImages.isEmpty()) {
                saveButton.setVisible(false);
                saveButton.setText("Save Images");
            } else {
                saveButton.setText("Save " + pendingImages.size() + " Image(s)");
            }
        });

        // stage 2 — show loader while saving to server
        saveButton.addClickListener(e -> {
            if (pendingImages.isEmpty()) return;

            saveButton.setEnabled(false);
            saveButton.setVisible(false);
            showLoader("Saving " + pendingImages.size() + " image(s) to server...");

            try {
                List<UserImage> saved = imageService.saveAll(new ArrayList<>(pendingImages));
                onImagesSaved.accept(saved);
                Notification.show(saved.size() + " image(s) saved successfully");
                pendingImages.clear();
            } catch (Exception ex) {
                Notification.show("Failed to save images");
            } finally {
                hideLoader();
                saveButton.setEnabled(true);
                saveButton.setText("Save Images");
                saveButton.setIcon(new Icon(VaadinIcon.CLOUD_UPLOAD_O));
                upload.getElement().callJsFunction("clearFiles");
            }
        });

        return uploadCard;
    }

    private void showLoader(String message) {
        progressLabel.setText(message);
        progressLabel.setVisible(true);
        progressBar.setVisible(true);
    }

    private void hideLoader() {
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
        progressLabel.setText("");
    }
}