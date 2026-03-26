package com.example.notes.views;

import com.example.notes.data.entity.ImageItem;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageItemService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The Image Gallery view — lets authenticated users upload images and browse
 * their personal gallery in a responsive masonry-style grid.
 *
 * Route: /gallery
 */
@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Gallery | Vaadin Notes App")
@CssImport("./styles/gallery.css")
@PermitAll
public class ImageGalleryView extends VerticalLayout {

    private final ImageItemService imageItemService;
    private final User currentUser;

    /** The container that holds all gallery cards */
    private final Div galleryGrid = new Div();

    /** List of currently displayed images for lightbox navigation */
    private List<ImageItem> currentImages;

    /** Remembers the caption entered before all uploads in a batch */
    private String pendingCaption = "";

    public ImageGalleryView(ImageItemService imageItemService,
                            UserRepository userRepository,
                            AuthenticationContext authContext) {
        this.imageItemService = imageItemService;

        // ── Resolve the current user ─────────────────────────────────────────
        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        addClassName("gallery-view");

        add(buildHeader(), buildUploadSection(), buildGallerySection());
        refreshGallery();
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private Div buildHeader() {
        H2 title = new H2("My Image Gallery");
        title.addClassName("gallery-view__title");

        Paragraph subtitle = new Paragraph(
                "Upload and manage your images. Supports JPEG, PNG, GIF and WebP (max 10 MB each).");
        subtitle.addClassName("gallery-view__subtitle");

        Div header = new Div(title, subtitle);
        header.addClassName("gallery-view__header");
        return header;
    }

    // ── Upload Section ────────────────────────────────────────────────────────

    private Div buildUploadSection() {
        // Vaadin Upload with multi-file in-memory buffer
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        upload.setMaxFileSize(10 * 1024 * 1024); // 10 MB in bytes
        upload.setMaxFiles(20);
        upload.setDropAllowed(true);
        upload.addClassName("gallery-upload__dropzone");
        upload.setWidthFull();

        // Custom upload button
        Button uploadBtn = new Button("Choose Images", VaadinIcon.UPLOAD.create());
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(uploadBtn);

        // Custom drop label
        upload.setDropLabel(new Span("Drag & drop images here, or click to browse"));

        // ── Handle each successfully uploaded file ──────────────────────────
        upload.addSucceededListener(event -> {
            String filename  = event.getFileName();
            String mimeType  = event.getMIMEType();
            long   fileSize  = event.getContentLength();
            InputStream data = buffer.getInputStream(filename);

            try {
                // Pass empty string for caption since titles are removed
                imageItemService.saveImage(data, filename, mimeType, fileSize,
                        "", currentUser);
                refreshGallery();
                
                // CRITICAL: Clear the upload list so Vaadin doesn't leave the filename title on screen!
                upload.clearFileList();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
                upload.clearFileList();
            } catch (IOException e) {
                showError("Failed to save \"" + filename + "\": " + e.getMessage());
                upload.clearFileList();
            }
        });

        // Surface Vaadin-side rejections (wrong type, too large)
        upload.addFileRejectedListener(event ->
                showError("File rejected: " + event.getErrorMessage()));

        Div section = new Div();
        section.addClassName("gallery-upload");
        H3 sectionTitle = new H3("Upload Images");
        sectionTitle.addClassName("gallery-upload__label");
        section.add(sectionTitle, upload);
        return section;
    }

    // ── Gallery Section ───────────────────────────────────────────────────────

    private Div buildGallerySection() {
        galleryGrid.addClassName("gallery-grid");
        galleryGrid.setWidthFull();

        Div section = new Div(galleryGrid);
        section.addClassName("gallery-section");
        return section;
    }

    /**
     * Clears and repopulates the gallery grid from the database.
     * Called after every upload or delete.
     */
    private void refreshGallery() {
        galleryGrid.removeAll();
        currentImages = imageItemService.getImages(currentUser);

        if (currentImages.isEmpty()) {
            galleryGrid.add(buildEmptyState());
            return;
        }

        for (int i = 0; i < currentImages.size(); i++) {
            ImageItem item = currentImages.get(i);
            int index = i;
            GalleryImageCard card = new GalleryImageCard(item);
            card.addDeleteListener(event -> handleDelete(event.getImageId()));
            card.addPreviewListener(event -> openLightbox(index));
            galleryGrid.add(card);
        }
    }

    /** Opens a lightbox dialog to preview the image at the given index. */
    private void openLightbox(int index) {
        Dialog lightbox = new Dialog();
        lightbox.addClassName("lightbox-dialog");
        updateLightboxContent(lightbox, index);
        lightbox.open();
    }

    private void updateLightboxContent(Dialog lightbox, int index) {
        lightbox.removeAll();
        ImageItem item = currentImages.get(index);

        Div container = new Div();
        container.addClassName("lightbox-container");

        // Previous button
        if (currentImages.size() > 1) {
            Button prevBtn = new Button(VaadinIcon.CHEVRON_LEFT.create());
            prevBtn.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            prevBtn.addClassNames("lightbox-nav-button", "lightbox-prev");
            prevBtn.addClickListener(e -> updateLightboxContent(lightbox, (index - 1 + currentImages.size()) % currentImages.size()));
            container.add(prevBtn);
        }

        // Image
        Image image = new Image("/images/" + item.getFilename(), item.getOriginalName());
        image.addClassName("lightbox-image");
        container.add(image);

        // Next button
        if (currentImages.size() > 1) {
            Button nextBtn = new Button(VaadinIcon.CHEVRON_RIGHT.create());
            nextBtn.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            nextBtn.addClassNames("lightbox-nav-button", "lightbox-next");
            nextBtn.addClickListener(e -> updateLightboxContent(lightbox, (index + 1) % currentImages.size()));
            container.add(nextBtn);
        }

        lightbox.add(container);
    }

    /** Handles a delete request coming from a card component. */
    private void handleDelete(Long imageId) {
        try {
            imageItemService.deleteImage(imageId, currentUser);
            refreshGallery();
            Notification.show("Image deleted.", 2500, Notification.Position.BOTTOM_END);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Failed to delete image file: " + e.getMessage());
        }
    }

    /** Renders a friendly empty-state placeholder when the gallery has no images. */
    private Div buildEmptyState() {
        Div emptyState = new Div();
        emptyState.addClassName("gallery-empty");

        Span icon = new Span("🖼️");
        icon.addClassName("gallery-empty__icon");

        Paragraph text = new Paragraph("Your gallery is empty. Upload your first image above!");
        text.addClassName("gallery-empty__text");

        emptyState.add(icon, text);
        return emptyState;
    }

    /** Shows a styled error notification. */
    private void showError(String message) {
        Notification n = Notification.show(message, 4500, Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
