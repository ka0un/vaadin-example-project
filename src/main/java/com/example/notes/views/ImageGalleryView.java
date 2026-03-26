package com.example.notes.views;

import com.example.notes.data.entity.ImageEntity;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.example.notes.views.components.ImageCard;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Route(value = "gallery", layout = MainLayout.class)
@PageTitle(("Image Gallery | Vaadin Notes App"))
@PermitAll
public class ImageGalleryView extends VerticalLayout {

    private final ImageService imageService;
    private final User currentUser;

    // Gallery grid where image cards are displayed
    private final FlexLayout galleryGrid = new FlexLayout();

    // Max file size: 5MB in bytes
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public ImageGalleryView(ImageService imageService,
                            UserRepository userRepository,
                            AuthenticationContext authContext) {
        this.imageService = imageService;

        // Get currently logged in user
        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        // Page background
        getStyle()
                .set("background", "#f0f2f5")
                .set("min-height", "100vh")
                .set("padding", "32px");

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        configureGalleryGrid();

        add(createHeaderSection(), createUploadSection(), galleryGrid);

        // Load existing images on page open
        refreshGallery();
    }

    private Div createHeaderSection() {
        Div header = new Div();
        header.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border-radius", "16px")
                .set("padding", "32px")
                .set("margin-bottom", "24px")
                .set("width", "100%")
                .set("box-shadow", "0 4px 20px rgba(102, 126, 234, 0.4)");

        H2 title = new H2("🖼️ My Image Gallery");
        title.getStyle()
                .set("color", "white")
                .set("margin", "0")
                .set("font-size", "28px")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Upload and manage your personal image collection");
        subtitle.getStyle()
                .set("color", "rgba(255,255,255,0.8)")
                .set("margin", "8px 0 0 0")
                .set("font-size", "14px");

        header.add(title, subtitle);
        return header;
    }

    private Div createUploadSection() {

        // MemoryBuffer holds uploaded file data in memory
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        // Only allow image files
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");

        // Max file size 5MB
        upload.setMaxFileSize((int) MAX_FILE_SIZE);

        // Max 1 file at a time
        upload.setMaxFiles(1);

        upload.setDropLabel(new com.vaadin.flow.component.html.Span(
                "📂 Drop your image here or click to browse (JPG, PNG, GIF, WEBP — max 5MB)"
        ));

        // Style the upload component
        upload.getStyle()
                .set("width", "100%");

        // When upload succeeds
        upload.addSucceededListener(event -> {
            try {
                // Save image via service
                imageService.saveImage(
                        event.getFileName(),
                        event.getMIMEType(),
                        buffer.getInputStream(),
                        currentUser
                );

                // Show success notification
                Notification n = Notification.show("✅ Image uploaded successfully!");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                n.setDuration(3000);
                // Refresh gallery to show new image
                refreshGallery();
            } catch (Exception e) {
                Notification n = Notification.show("❌ Upload failed: " + e.getMessage());
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                n.setDuration(3000);
            }
        });

        // When upload fails (wrong file type, too large etc.)
        upload.addFileRejectedListener(event -> {
            Notification n = Notification.show("⚠️ " + event.getErrorMessage());
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            n.setDuration(4000);
        });

        // Wrap upload in a styled card
        Div uploadCard = new Div();
        uploadCard.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "24px")
                .set("margin-bottom", "24px")
                .set("width", "100%")
                .set("box-shadow", "0 2px 12px rgba(0,0,0,0.08)")
                .set("box-sizing", "border-box");

        Paragraph uploadTitle = new Paragraph("📤 Upload New Image");
        uploadTitle.getStyle()
                .set("font-weight", "700")
                .set("font-size", "16px")
                .set("margin", "0 0 16px 0")
                .set("color", "#333");

        uploadCard.add(uploadTitle, upload);
        return uploadCard;
    }

    private void configureGalleryGrid() {
        galleryGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        galleryGrid.getStyle()
                .set("gap", "20px")
                .set("width", "100%")
                .set("padding", "8px 0");
    }

    private void refreshGallery() {
        // Clear current gallery
        galleryGrid.removeAll();

        // Load all images for current user
        List<ImageEntity> images = imageService.getImages(currentUser);

        if (images.isEmpty()) {
            // Show empty state message
            Div emptyState = new Div();
            emptyState.getStyle()
                    .set("width", "100%")
                    .set("text-align", "center")
                    .set("padding", "60px 20px")
                    .set("background", "white")
                    .set("border-radius", "16px")
                    .set("box-shadow", "0 2px 12px rgba(0,0,0,0.08)");

            Paragraph emoji = new Paragraph("🖼️");
            emoji.getStyle()
                    .set("font-size", "48px")
                    .set("margin", "0 0 12px 0");

            Paragraph message = new Paragraph("No images yet!");
            message.getStyle()
                    .set("font-size", "18px")
                    .set("font-weight", "600")
                    .set("color", "#555")
                    .set("margin", "0 0 8px 0");

            Paragraph hint = new Paragraph("Upload your first image using the section above.");
            hint.getStyle()
                    .set("font-size", "14px")
                    .set("color", "#999")
                    .set("margin", "0");

            emptyState.add(emoji, message, hint);
            galleryGrid.add(emptyState);
            return;
        }

        // Create an ImageCard for each image
        images.forEach(image -> {
            ImageCard card = new ImageCard(image, imageService, this::refreshGallery);
            galleryGrid.add(card);
        });
    }
}
