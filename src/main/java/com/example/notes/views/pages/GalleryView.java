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
import com.vaadin.flow.component.combobox.ComboBox;
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
        add(createToolbar(), createGallerySection());

        refreshGallery();

    }

    private Component createToolbar() {

        // 🔹 Add button
        Button addButton = new Button("Add",FontAwesome.Solid.PLUS.create());
        addButton.getStyle()
                .set("background-color", "#007bff")
                .set("color", "white");

        addButton.addClickListener(e -> {
            openUploadDialog();
        });

        // 🔹 Format filter
        ComboBox<String> formatFilter = new ComboBox<>();
        formatFilter.setItems("jpg", "jpeg", "png");
        formatFilter.setPlaceholder("Image Format");
        formatFilter.setClearButtonVisible(true);

        formatFilter.addValueChangeListener(e -> {
            // TODO: filter logic
        });

        // 🔹 Sort combo
        ComboBox<String> sortBy = new ComboBox<>();
        sortBy.setItems("Size", "Uploaded Date");
        sortBy.setPlaceholder("Sort By");

        sortBy.addValueChangeListener(e -> {
            // TODO: sorting logic
        });

        // 🔹 Right side container
        HorizontalLayout rightControls = new HorizontalLayout(formatFilter, sortBy);
        rightControls.setSpacing(true);

        // 🔹 Main toolbar
        HorizontalLayout toolbar = new HorizontalLayout(addButton, rightControls);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.expand(rightControls); // pushes controls to right

        // 🔹 Responsive behavior
        toolbar.getStyle().set("flex-wrap", "wrap");

        // Make children responsive
        addButton.getStyle().set("flex-grow", "0");

        rightControls.getStyle()
                .set("display", "flex")
                .set("gap", "10px")
                .set("flex-wrap", "wrap")
                .set("justify-content", "flex-end")
                .set("flex-grow", "1");

        // 🔹 Mobile tweak (stack vertically)
        toolbar.getElement().executeJs("""
        const toolbar = this;
        function updateLayout() {
            if (window.innerWidth < 600) {
                toolbar.style.flexDirection = 'column';
                toolbar.style.alignItems = 'stretch';
            } else {
                toolbar.style.flexDirection = 'row';
                toolbar.style.alignItems = 'center';
            }
        }
        updateLayout();
        window.addEventListener('resize', updateLayout);
    """);

        return toolbar;
    }

    private void openUploadDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("50vw");
        dialog.setHeight("75vh");

        Component uploadComponent = createUploadSection(dialog);

        dialog.add(uploadComponent);
        dialog.open();
    }

    // 🔹 Upload section
    private Component createUploadSection(Dialog dialog) {

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg");
        int maxFileSizeInBytes = 10 * 1024 * 1024;
        upload.setMaxFileSize(maxFileSizeInBytes);
        upload.setMaxFiles(1);

        upload.setDropLabel(new Span("Drag & drop image here"));

        upload.addSucceededListener(event -> {
            try {
                imageService.saveImage(
                        event.getFileName(),
                        buffer.getInputStream(),
                        event.getContentLength(),
                        currentUser
                );

                dialog.close();          // 🔹 close immediately after upload
                refreshGallery();        // 🔹 refresh UI

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button uploadButton = new Button("Upload Image");
        upload.setUploadButton(uploadButton);

        upload.setWidthFull();
        upload.setHeightFull();

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