package com.example.notes.views.pages;

import com.example.notes.data.dto.ImageThumbnailDto;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.example.notes.views.components.ImageCard;
import com.example.notes.views.components.ImageDialog;
import com.example.notes.views.layouts.MainLayout;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
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
    private String format = "all";
    private int sort = 0;

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

        ComboBox<String> formatFilter = new ComboBox<>();
        formatFilter.setItems("All","jpg", "jpeg", "png");
        formatFilter.setPlaceholder("Image Format");
        formatFilter.setClearButtonVisible(false);
        formatFilter.setAllowCustomValue(false);

        formatFilter.addValueChangeListener(e -> {
            format = e.getValue();
            refreshGallery();
        });

        ComboBox<String> sortBy = new ComboBox<>();
        List<String> list =  new ArrayList<>(List.of("Newest to Oldest","Oldest to Newest","Largest to Smallest","Smallest to Largest"));
        sortBy.setItems(list);
        sortBy.setPlaceholder("Sort By");
        sortBy.setClearButtonVisible(false);
        sortBy.setAllowCustomValue(false);

        sortBy.addValueChangeListener(e -> {
            sort = list.indexOf(e.getValue());
            refreshGallery();
        });

        HorizontalLayout rightControls = new HorizontalLayout(formatFilter, sortBy);
        rightControls.setSpacing(true);

        HorizontalLayout toolbar = new HorizontalLayout(addButton, rightControls);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.expand(rightControls); // pushes controls to right

        toolbar.getStyle().set("flex-wrap", "wrap");

        addButton.getStyle().set("flex-grow", "0");

        rightControls.getStyle()
                .set("display", "flex")
                .set("gap", "10px")
                .set("flex-wrap", "wrap")
                .set("justify-content", "flex-end")
                .set("flex-grow", "1");

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

                dialog.close();
                refreshGallery();

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

    private Component createGallerySection() {

        galleryContainer.getStyle().set("display", "grid");
        galleryContainer.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(250px, 1fr))");
        galleryContainer.getStyle().set("gap", "16px");
        galleryContainer.setWidthFull();

        return galleryContainer;
    }

    private void refreshGallery() {

        galleryContainer.removeAll();

        images = imageService.getAllImagesByUser(format,sort);

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