package com.example.notes.views.pages;

import com.example.notes.data.dto.ImageDto;
import com.example.notes.data.dto.ImageThumbnailDto;
import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.example.notes.views.components.ImageCard;
import com.example.notes.views.components.ImageDialog;
import com.example.notes.views.layouts.MainLayout;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.persistence.EntityNotFoundException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.security.PermitAll;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Gallery | Vaadin Notes App")
@PermitAll
@CssImport("./styles/sharedCss.css")
public class GalleryView extends VerticalLayout {

    private final ImageService imageService;
    private final User currentUser;
    List<ImageThumbnailDto> images;
    private int currentIndex = -1;

    private final Div galleryContainer = new Div();

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
        int maxFileSizeInBytes = 10 * 1024 * 1024;
        upload.setMaxFileSize(maxFileSizeInBytes);
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

        upload.setHeight("350px");
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

        images = imageService.getAllImagesByUser();

        galleryContainer.add(createUploadSection());
        for (ImageThumbnailDto img : images) {
            galleryContainer.add(new ImageCard(img, () -> openImageDialog(images.indexOf(img))));
        }
    }

    private void openImageDialog(int index){

        if (index < 0 || index >= images.size()) return;

        currentIndex = index;

        Dialog dialog = new ImageDialog(images,currentIndex,imageService,currentUser,this::refreshGallery);

        dialog.open();
    }

}