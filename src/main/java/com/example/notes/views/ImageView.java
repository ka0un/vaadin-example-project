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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
    private final VerticalLayout imageGrid = new VerticalLayout();

    public ImageView(ImageService imageService, CurrentUserService userService) {
        this.imageService = imageService;
        this.userService = userService;

        User currentUser = userService.getCurrentUser();

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "transparent");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        VerticalLayout sidebar = createSidebar();
        VerticalLayout shell = new VerticalLayout();
        shell.setWidthFull();
        shell.setFlexGrow(1);
        shell.setSpacing(true);
        shell.setPadding(true);
        shell.getStyle().set("padding", "2rem");

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
        mainLayout.add(sidebar, shell);
        add(mainLayout);

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
        imageGrid.setPadding(false);
        imageGrid.setSpacing(false);
        imageGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(180px, 1fr))")
                .set("gap", "1.2rem");

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
            StreamResource resource = new StreamResource(img.getFileName(), () -> {
                try {
                    return new FileInputStream(img.getFilePath());
                } catch (Exception e) {
                    return InputStream.nullInputStream();
                }
            });

            com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(resource, "uploaded");
            image.setWidth("100%");
            image.setHeight("160px");
            image.getStyle().set("object-fit", "cover").set("border-radius", "14px");

            VerticalLayout imageCardContent = new VerticalLayout(image);
            imageCardContent.setPadding(false);
            imageCardContent.setSpacing(false);
            imageCardContent.setWidthFull();
            imageCardContent.getStyle().set("position", "relative");

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            deleteBtn.setTooltipText("Delete Image");
            deleteBtn.getStyle()
                    .set("position", "absolute")
                    .set("top", "8px")
                    .set("right", "8px")
                    .set("background", "rgba(255, 255, 255, 0.9)")
                    .set("border-radius", "50%")
                    .set("width", "36px")
                    .set("height", "36px")
                    .set("padding", "0")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("cursor", "pointer");

            deleteBtn.addClickListener(e -> {
                imageService.deleteImage(img);
                refreshGallery(user);
                Notification.show("Image deleted successfully", 2000, Notification.Position.TOP_CENTER);
            });

            imageCardContent.add(deleteBtn);

            VerticalLayout imageCard = new VerticalLayout(imageCardContent);
            imageCard.setPadding(true);
            imageCard.setSpacing(true);
            imageCard.setWidthFull();
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

    private VerticalLayout createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth("240px");
        sidebar.setPadding(true);
        sidebar.setSpacing(true);
        sidebar.getStyle()
                .set("background", "rgba(255,255,255,0.9)")
                .set("border-right", "1px solid #dde4ef")
                .set("padding", "1.5rem 1rem");

        // Upload section
        Button uploadBtn = new Button("Upload");
        uploadBtn.setWidthFull();
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        uploadBtn.getStyle()
                .set("font-weight", "600")
                .set("padding", "0.75rem");

        // Images section
        Button imagesBtn = new Button("Images");
        imagesBtn.setWidthFull();
        imagesBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        imagesBtn.getStyle()
                .set("padding", "0.75rem")
                .set("justify-content", "flex-start");

        // Favorites section
        Button favoritesBtn = new Button("Favorites");
        favoritesBtn.setWidthFull();
        favoritesBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        favoritesBtn.getStyle()
                .set("padding", "0.75rem")
                .set("justify-content", "flex-start");

        // Recently Deleted section
        Button recentlyDeletedBtn = new Button("Recently Deleted");
        recentlyDeletedBtn.setWidthFull();
        recentlyDeletedBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        recentlyDeletedBtn.getStyle()
                .set("padding", "0.75rem")
                .set("justify-content", "flex-start");

        sidebar.add(uploadBtn, imagesBtn, favoritesBtn, recentlyDeletedBtn);
        return sidebar;
    }
}