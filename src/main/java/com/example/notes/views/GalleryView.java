package com.example.notes.views;

import com.example.notes.data.entity.ImageRecord;
import com.example.notes.service.ImageService;
import com.example.notes.views.components.ImageCard;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Div;
import jakarta.annotation.security.PermitAll;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Image Gallery")
@PermitAll // Secures the page so only logged-in users can see it
public class GalleryView extends VerticalLayout {

    private final ImageService imageService;
    private final Div galleryContainer = new Div();

    public GalleryView(ImageService imageService) {
        this.imageService = imageService;

        H2 header = new H2("My Image Gallery");

        // 1. Configure the Upload Component
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        int maxFileSizeInBytes = 5 * 1024 * 1024; // 5MB limit
        upload.setMaxFileSize(maxFileSizeInBytes);

        // 2. Handle Errors (Wrong file type, too large)
        upload.addFileRejectedListener(event -> {
            Notification error = Notification.show(event.getErrorMessage());
            error.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // 3. Handle Successful Uploads
        upload.addSucceededListener(event -> {
            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] bytes = inputStream.readAllBytes();

                this.imageService.saveImage(event.getFileName(), event.getMIMEType(), bytes);

                Notification.show("Image uploaded successfully!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                loadImages(); // Refresh the gallery instantly
            } catch (IOException e) {
                Notification.show("Failed to process file", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // 4. Setup Responsive CSS Grid
        galleryContainer.setWidthFull();
        galleryContainer.getStyle().set("display", "grid");
        // This line makes it responsive to screen sizes automatically
        galleryContainer.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(250px, 1fr))");
        galleryContainer.getStyle().set("gap", "16px");

        add(header, upload, galleryContainer);
        setSizeFull();

        loadImages(); // Load on startup
    }

    private void loadImages() {
        galleryContainer.removeAll();
        List<ImageRecord> images = imageService.getAllImages();
        for (ImageRecord image : images) {
            galleryContainer.add(new ImageCard(image));
        }
    }
}