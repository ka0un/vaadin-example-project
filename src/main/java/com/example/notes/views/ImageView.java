package com.example.notes.views;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.security.CurrentUserService;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.FileRejectedEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Route(value = "images", layout = MainLayout.class)
@PageTitle("Image Gallery")
@PermitAll
public class ImageView extends VerticalLayout {

    private final ImageService imageService;
    private final CurrentUserService userService;

    private final VerticalLayout galleryLayout = new VerticalLayout();
    private final VerticalLayout emptyStateCard = new VerticalLayout();
    private final FlexLayout imageGrid = new FlexLayout();

    public ImageView(ImageService imageService, CurrentUserService userService) {
        this.imageService = imageService;
        this.userService = userService;

        User currentUser = userService.getCurrentUser();

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
            .set("background", "transparent")
                .set("padding", "2rem")
                .set("box-sizing", "border-box");

        VerticalLayout shell = new VerticalLayout();
        shell.setWidthFull();
        shell.setMaxWidth("1000px");
        shell.setSpacing(true);
        shell.setPadding(false);
        shell.getStyle().set("margin", "0 auto");

        // Use MultiFileMemoryBuffer to allow batch uploads for better UX
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/*");
        upload.setMaxFileSize(50 * 1024 * 1024);
        upload.setMaxFiles(20);
        upload.setDropLabel(new Span("Drag and drop your files anywhere inside this area"));

        Button uploadButton = new Button("Upload Images", new Icon(VaadinIcon.UPLOAD_ALT));
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(uploadButton);
        upload.setWidthFull();
        upload.getStyle().set("--vaadin-upload-button-display", "inline-flex");

        upload.addFileRejectedListener(this::showFileRejectedMessage);

        upload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                imageService.saveImage(fileName, buffer.getInputStream(fileName), currentUser);
                Notification.show("Uploaded: " + fileName, 1800, Notification.Position.TOP_CENTER);
                refreshGallery(currentUser);
            } catch (IOException | IllegalArgumentException e) {
                Notification.show("Upload failed: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });

        shell.add(createHeader(), createUploadPanel(upload), createGallerySection());
        add(shell);

        refreshGallery(currentUser);
    }

    private Component createHeader() {
        H2 title = new H2("My Gallery");
        title.getStyle().set("margin", "0").set("font-size", "2.2rem").set("font-weight", "800").set("color", "#18212f");

        Paragraph subtitle = new Paragraph("Manage and organize your visual assets in one place.");
        subtitle.getStyle().set("margin", "0").set("color", "#5b6675").set("font-size", "1.05rem");

        VerticalLayout header = new VerticalLayout(title, subtitle);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("gap", "0.35rem");
        return header;
    }

    private Component createUploadPanel(Upload upload) {
        VerticalLayout uploadPanel = new VerticalLayout();
        uploadPanel.setWidthFull();
        uploadPanel.setPadding(true);
        uploadPanel.setSpacing(true);
        uploadPanel.add(upload);
        uploadPanel.getStyle()
                .set("border", "1px dashed #cdd5e2")
                .set("border-radius", "22px")
                .set("background", "linear-gradient(135deg, #f8faff 0%, #edf3ff 100%)")
                .set("box-shadow", "0 10px 24px rgba(9, 30, 66, 0.05)")
                .set("padding", "1.5rem");
        return uploadPanel;
    }

    private Component createGallerySection() {
        galleryLayout.setWidthFull();
        galleryLayout.setPadding(false);
        galleryLayout.setSpacing(false);
        galleryLayout.getStyle().set("gap", "1rem");

        emptyStateCard.removeAll();
        Icon emptyIcon = VaadinIcon.PICTURE.create();
        emptyIcon.getStyle().set("color", "#2563eb").set("width", "40px").set("height", "40px");

        Span emptyTitle = new Span("No images uploaded yet");
        emptyTitle.getStyle().set("font-size", "1.8rem").set("font-weight", "700").set("color", "#111827");

        Paragraph emptySubtitle = new Paragraph("Upload your first image to start building your library.");
        emptySubtitle.getStyle().set("margin", "0").set("font-size", "1rem").set("color", "#5b6675");

        emptyStateCard.add(emptyIcon, emptyTitle, emptySubtitle);
        emptyStateCard.setWidthFull();
        emptyStateCard.setAlignItems(Alignment.CENTER);
        emptyStateCard.setJustifyContentMode(JustifyContentMode.CENTER);
        emptyStateCard.getStyle()
                .set("border", "1px dashed #cdd5e2")
                .set("border-radius", "20px")
                .set("background", "rgba(255,255,255,0.78)")
                .set("min-height", "270px")
                .set("padding", "2rem");

        imageGrid.setWidthFull();
        imageGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        imageGrid.getStyle().set("gap", "1rem");

        galleryLayout.add(emptyStateCard, imageGrid);
        return galleryLayout;
    }

    private void showFileRejectedMessage(FileRejectedEvent event) {
        Notification.show(event.getErrorMessage(), 3000, Notification.Position.TOP_CENTER);
    }

    private void refreshGallery(User user) {
        imageGrid.removeAll();

        List<Image> images = imageService.getUserImages(user);
        boolean hasImages = !images.isEmpty();
        emptyStateCard.setVisible(!hasImages);
        imageGrid.setVisible(hasImages);

        if (!hasImages) return;

        for (Image img : images) {
            // 1. Create the Resource to load the image from disk
            StreamResource resource = new StreamResource(img.getFileName(), () -> {
                try {
                    return new FileInputStream(img.getFilePath());
                } catch (Exception e) {
                    return InputStream.nullInputStream();
                }
            });

            // 2. Setup the Image Component with styling for a nice card look
            com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(resource, "uploaded");
            image.setWidthFull();
            image.setHeight("190px");
            image.getStyle().set("object-fit", "cover").set("border-radius", "14px");

            Span name = new Span(formatDisplayName(img.getFileName()));
            name.getStyle().set("font-size", "0.9rem").set("font-weight", "600")
                         .set("color", "#1f2937").set("word-break", "break-word");

            // --- START NEW DELETE BUTTON LOGIC ---
            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH), e -> {
                imageService.deleteImage(img); // Tell the service to remove file + DB record
                refreshGallery(user);          // Reload the grid immediately
                Notification.show("Image deleted successfully", 2000, Notification.Position.TOP_CENTER);
            });

            // Styling the button
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.setTooltipText("Delete Image");
            deleteBtn.getStyle().set("margin-left", "auto"); // Aligns it to the right
            // --- END NEW DELETE BUTTON LOGIC ---

            // 3. Assemble the Card
            // We add the deleteBtn to the Layout
            VerticalLayout imageCard = new VerticalLayout(image, name, deleteBtn); 
            imageCard.setPadding(true);
            imageCard.setSpacing(true);
            imageCard.setWidth("220px");
            imageCard.getStyle()
                    .set("background", "rgba(255,255,255,0.9)")
                    .set("border", "1px solid #dde4ef")
                    .set("border-radius", "18px")
                    .set("box-shadow", "0 6px 16px rgba(9, 30, 66, 0.06)");

            imageGrid.add(imageCard);
        }
    }

                private String formatDisplayName(String fileName) {
                int separator = fileName.indexOf('_');
                if (separator >= 0 && separator < fileName.length() - 1) {
                    return fileName.substring(separator + 1);
                }
                return fileName;
    }
}