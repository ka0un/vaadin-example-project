package com.example.notes.views;

import com.example.notes.data.dto.ImageDto;
import com.example.notes.data.dto.ImageThumbnailDto;
import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
            galleryContainer.add(createImageCard(img, images.indexOf(img)));
        }
    }

    private Component createImageCard(ImageThumbnailDto img, int index) {

        Div card = new Div();
        card.setWidthFull();
        card.setHeight("350px");

        card.getStyle()
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("cursor", "pointer");

        // Image
        com.vaadin.flow.component.html.Image image = getImage(img);

        image.getStyle()
                .set("object-fit", "cover")
                .set("transition", "transform 0.3s ease"); // smooth animation

        // 🔥 Hover effect (zoom)
        card.getElement().addEventListener("mouseover",
                e -> image.getStyle().set("transform", "scale(1.08)"));

        card.getElement().addEventListener("mouseout",
                e -> image.getStyle().set("transform", "scale(1)"));

        // 🔥 Click → open dialog
        card.addClickListener(e -> openImageDialog(index));

        card.add(image);

        return card;
    }

    private com.vaadin.flow.component.html.@NonNull Image getImage(ImageThumbnailDto img) {
        StreamResource streamResource = new StreamResource(String.valueOf(img.getId()), () -> {
            try {
                return img.getResource().getInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(streamResource, "Thumbnail " + img.getId());

        image.getElement().setAttribute("loading", "lazy");

        image.setWidthFull();
        image.setHeightFull();
        return image;
    }

    private void openImageDialog(int index){

        if (index < 0 || index >= images.size()) return;

        currentIndex = index;

        ImageThumbnailDto imageThumbnailDto = images.get(index);
        Long imageId = imageThumbnailDto.getId();

        ImageDto imageDto = imageService.getImageForUser(imageId);
        Image imageEntity = imageDto.getImage();

        Dialog dialog = new Dialog();

        Div mainDiv = new Div();
        mainDiv.addClassName("responsive-main");

        Div divA = new Div();

        com.vaadin.flow.component.html.Image image;

        StreamResource streamResource = new StreamResource(String.valueOf(imageId), () -> {
            try {
                return imageDto.getImageResource().getInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        image = new com.vaadin.flow.component.html.Image(streamResource, String.valueOf(imageId));

        image.getStyle()
                .set("max-height", "500px")
                .set("width", "auto")
                .setMaxWidth("400px")
                .set("height", "auto")
                .set("display", "block");

        divA.add(image);

        Div divB = new Div();
        divB.getStyle()
                .setBackgroundColor("white")
                .set("max-width", "300px");

        VerticalLayout meta = new VerticalLayout(
                createMetaRow("Name", imageEntity.getFileName()),
                createMetaRow("Size", formatFileSize(imageEntity.getFileSize())),
                createMetaRow("Resolution", imageEntity.getWidth() + "x" + imageEntity.getHeight()),
                createMetaRow("Format", imageEntity.getFormat().toUpperCase())
        );
        meta.setPadding(false);


        Button cropBtn = new Button("Crop");
        Button deleteBtn = new Button("Delete");

        deleteBtn.getStyle().set("color", "red");
        deleteBtn.addClickListener(e -> {
            try {
                imageService.deleteImage(imageEntity.getId(), currentUser);

                dialog.close();
                refreshGallery();

                Notification.show("Image deleted successfully", 2000, Notification.Position.BOTTOM_START);

            } catch (AccessDeniedException ex) {
                Notification.show("You are not allowed to delete this image.");

            } catch (EntityNotFoundException ex) {
                Notification.show("Image not found.");

            } catch (IOException ex) {
                Notification.show("Failed to delete image file.");

            } catch (Exception ex) {
                Notification.show("Unexpected error occurred.");
            }
        });

        HorizontalLayout actions = new HorizontalLayout(cropBtn, deleteBtn);
        actions.getStyle()
                .setWidth("100%")
                .set("border-top", "1px solid rgba(0,0,0,0.3)")
                .set("padding-top", "10px")
                .set("margin-top", "10px");

        VerticalLayout metaAndActionsLayout = new VerticalLayout(meta, actions);

        divB.add(metaAndActionsLayout);

        mainDiv.add(divA, divB);

        Button leftBtn = new Button(FontAwesome.Solid.ARROW_LEFT.create());
        leftBtn.addClassName("arrow-btn");
        Button rightBtn = new Button(FontAwesome.Solid.ARROW_RIGHT.create());
        rightBtn.addClassName("arrow-btn");

        leftBtn.addClickListener(e -> {
            int prevIndex = currentIndex - 1;

            dialog.close();
            openImageDialog(prevIndex);
        });
        rightBtn.addClickListener(e -> {
            int nextIndex = currentIndex + 1;

            dialog.close();
            openImageDialog(nextIndex);
        });

        // Top navigation buttons
        HorizontalLayout navBar = new HorizontalLayout();
        navBar.setWidthFull();

        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");

        boolean hasPrevious = currentIndex > 0;
        boolean hasNext = currentIndex < images.size() - 1;

        if (hasPrevious) {
            navBar.add(leftBtn);
        }

        navBar.add(spacer); // always present

        if (hasNext) {
            navBar.add(rightBtn);
        }
        navBar.getStyle()
                .set("border-bottom", "1px solid rgba(0,0,0,0.1)")
                .set("padding-bottom", "8px")
                .set("margin-bottom", "10px");

// Wrapper (vertical now)
        VerticalLayout wrapper = new VerticalLayout(navBar, mainDiv);
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setWidthFull();

        dialog.add(wrapper);

        dialog.open();
    }

    private Component createMetaRow(String labelText, String valueText) {

        Span label = new Span(labelText);
        label.getStyle()
                .set("font-size", "12px")
                .set("color", "#777")
                .set("font-family", "Helvetica, Arial, sans-serif");

        Span value = new Span(valueText);
        value.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("font-family", "Helvetica, Arial, sans-serif");

        VerticalLayout row = new VerticalLayout(label, value);
        row.setSpacing(false);
        row.setPadding(false);

        return row;
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return String.format("%.2f MB", size / (1024.0 * 1024));
    }
}