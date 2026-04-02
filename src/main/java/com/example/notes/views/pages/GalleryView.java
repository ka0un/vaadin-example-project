package com.example.notes.views.pages;

import com.example.notes.data.dto.ImageThumbnailDto;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.example.notes.views.components.ImageCard;
import com.example.notes.views.components.ImageDialog;
import com.example.notes.views.layouts.MainLayout;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Gallery | Vaadin Notes App")
@PermitAll
@CssImport("./styles/sharedCss.css")
public class GalleryView extends VerticalLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(GalleryView.class);

    private final ImageService imageService;
    private final User currentUser;
    private List<ImageThumbnailDto> images;
    private int currentIndex = -1;
    private String format = "all";
    private int sort = 0;

    private final Div galleryContainer = new Div();
    private final HorizontalLayout toolbar = new HorizontalLayout();
    private Registration resizeListenerRegistration;

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

        toolbar.add(addButton, rightControls);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.expand(rightControls);

        toolbar.getStyle().set("flex-wrap", "wrap");

        addButton.getStyle().set("flex-grow", "0");

        rightControls.getStyle()
                .set("display", "flex")
                .set("gap", "10px")
                .set("flex-wrap", "wrap")
                .set("justify-content", "flex-end")
                .set("flex-grow", "1");

        return toolbar;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        removeResizeListener();

        attachEvent.getUI().getPage().retrieveExtendedClientDetails(
                details -> updateToolbarLayout(details.getWindowInnerWidth()));
        resizeListenerRegistration = attachEvent.getUI().getPage()
                .addBrowserWindowResizeListener(
                        event -> updateToolbarLayout(event.getWidth()));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        removeResizeListener();
        super.onDetach(detachEvent);
    }

    private void removeResizeListener() {
        if (resizeListenerRegistration != null) {
            resizeListenerRegistration.remove();
            resizeListenerRegistration = null;
        }
    }

    private void updateToolbarLayout(int windowWidth) {
        if (windowWidth < 600) {
            toolbar.getStyle().set("flex-direction", "column");
            toolbar.getStyle().set("align-items", "stretch");
        } else {
            toolbar.getStyle().set("flex-direction", "row");
            toolbar.getStyle().set("align-items", "center");
        }
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

        FileBuffer buffer = new FileBuffer();
        Upload upload = new Upload(buffer);

        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg");
        int maxFileSizeInBytes = 10 * 1024 * 1024;
        upload.setMaxFileSize(maxFileSizeInBytes);
        upload.setMaxFiles(1);

        upload.setDropLabel(new Span("Drag & drop image here"));

        upload.addSucceededListener(event -> {
            File tempUpload = getTempUploadFile(buffer);

            try (InputStream inputStream = buffer.getInputStream()) {
                imageService.saveImage(
                        event.getFileName(),
                        inputStream,
                        event.getContentLength(),
                        currentUser
                );

                dialog.close();
                refreshGallery();

            } catch (Exception e) {
                LOGGER.error("Failed to save uploaded image: {}", event.getFileName(), e);
                showUploadError("Upload failed. The image could not be saved.");
            } finally {
                deleteTempUploadFile(tempUpload, event.getFileName());
            }
        });

        upload.addFailedListener(event -> {
            LOGGER.warn("Upload failed for file: {}", event.getFileName(), event.getReason());
            deleteTempUploadFile(getTempUploadFile(buffer), event.getFileName());
            showUploadError("Upload failed. Check your connection and try again.");
        });

        upload.addFileRejectedListener(
                event -> showUploadError("Upload rejected: " + event.getErrorMessage()));

        Button uploadButton = new Button("Upload Image");
        upload.setUploadButton(uploadButton);

        upload.setWidthFull();
        upload.setHeightFull();

        return upload;
    }

    private File getTempUploadFile(FileBuffer buffer) {
        if (buffer.getFileData() == null) {
            return null;
        }
        return buffer.getFileData().getFile();
    }

    private void deleteTempUploadFile(File tempUpload, String fileName) {
        if (tempUpload == null) {
            return;
        }

        try {
            java.nio.file.Files.deleteIfExists(tempUpload.toPath());
        } catch (IOException exception) {
            LOGGER.warn("Failed to delete temporary upload file for {}", fileName, exception);
        }
    }

    private void showUploadError(String message) {
        Notification notification = Notification.show(
                message,
                3000,
                Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
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

        for (int i = 0; i < images.size(); i++) {
            ImageThumbnailDto image = images.get(i);
            int index = i;
            galleryContainer.add(new ImageCard(image, () -> openImageDialog(index)));
        }
    }

    private void openImageDialog(int index){

        if (index < 0 || index >= images.size()) return;

        currentIndex = index;

        Dialog dialog = new ImageDialog(images,currentIndex,imageService,currentUser,this::refreshGallery);

        dialog.open();
    }

}
