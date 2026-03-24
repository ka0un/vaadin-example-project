package com.example.notes.views;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.example.notes.service.NoteService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.security.PermitAll;

import java.io.IOException;
import java.util.List;

@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Gallery | Vaadin Notes App")
@PermitAll
public class GalleryView extends VerticalLayout {

    private final ImageService imageService;
    private final User currentUser;

    private Div galleryContainer = new Div();

    public GalleryView(ImageService imageService,
                       UserRepository userRepository,
                       AuthenticationContext authContext) {

        this.imageService = imageService;

        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createGallerySection());

        refreshGallery();
    }

    // 🔹 Upload section
    private Component createUploadSection() {

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg");
        upload.setMaxFiles(1);

        upload.addSucceededListener(event -> {
            try {
                imageService.saveImage(
                        event.getFileName(),
                        buffer.getInputStream(),
                        event.getContentLength(),
                        currentUser
                );
                refreshGallery();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button uploadButton = new Button("Upload Image");
        upload.setUploadButton(uploadButton);

        upload.setHeight("200px");
        return upload;
    }

    // 🔹 Gallery section
    private Component createGallerySection() {

        galleryContainer.getStyle().set("display", "grid");
        galleryContainer.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(250px, 1fr))");
        galleryContainer.getStyle().set("gap", "16px");
        galleryContainer.setWidthFull();

        return galleryContainer;
    }

    // 🔹 Refresh gallery
    private void refreshGallery() {

        galleryContainer.removeAll();

        List<Image> images = imageService.getAllImagesByUser(currentUser);

        galleryContainer.add(createUploadSection());
        for (Image img : images) {
            galleryContainer.add(createImageCard(img));
        }
    }

    private Component createImageCard(Image img) {

        Div card = new Div();
        card.setWidthFull();
        card.setHeight("200px");

        card.getStyle()
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("cursor", "pointer");

        // Image
        com.vaadin.flow.component.html.Image image =
                new com.vaadin.flow.component.html.Image(
                        "/" + img.getFileName(),
                        img.getFileName()
                );

        image.setWidthFull();
        image.setHeightFull();

        image.getStyle()
                .set("object-fit", "cover")
                .set("transition", "transform 0.3s ease"); // smooth animation

        // 🔥 Hover effect (zoom)
        card.getElement().addEventListener("mouseover",
                e -> image.getStyle().set("transform", "scale(1.08)"));

        card.getElement().addEventListener("mouseout",
                e -> image.getStyle().set("transform", "scale(1)"));

        // 🔥 Click → open dialog
        card.addClickListener(e -> openImageDialog(img));

        card.add(image);

        return card;
    }

    private void openImageDialog(Image img) {

        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(Alignment.CENTER);

        // Image (bigger preview)
        com.vaadin.flow.component.html.Image fullImage =
                new com.vaadin.flow.component.html.Image(
                        "/" + img.getFileName(),
                        img.getFileName()
                );

        fullImage.setMaxWidth("100%");
        fullImage.setMaxHeight("400px");
        fullImage.getStyle().set("object-fit", "contain");

        // Metadata
        Span name = new Span("Name: " + img.getFileName());
        Span size = new Span("Size: " + formatFileSize(img.getFileSize()));
        Span dimensions = new Span("Resolution: " + img.getWidth() + "x" + img.getHeight());
        Span format = new Span("Format: " + img.getFormat().toUpperCase());

        VerticalLayout meta = new VerticalLayout(name, size, dimensions, format);
        meta.setSpacing(false);
        meta.setPadding(false);

        // Buttons
        Button cropBtn = new Button("Crop");
        Button deleteBtn = new Button("Delete");

        deleteBtn.getStyle().set("color", "red");

        // 🔥 Delete logic
        deleteBtn.addClickListener(e -> {
            try {
                imageService.deleteImage(img,currentUser);
            } catch (IOException ex) {
                Notification.show("The image does not exist.");
                return;
            }
            dialog.close();
            refreshGallery();
        });

        HorizontalLayout actions = new HorizontalLayout(cropBtn, deleteBtn);

        layout.add(fullImage, meta, actions);
        dialog.add(layout);

        dialog.open();
    }

    // 🔹 Helper: format file size
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return String.format("%.2f MB", size / (1024.0 * 1024));
    }
}