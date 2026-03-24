package com.example.notes.views;

import com.example.notes.data.entity.ImageMetadata;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("")
@PageTitle("Gallery | Image Upload")
@PermitAll
public class ImageUploadView extends VerticalLayout {

    private final ImageService imageService;
    private FlexLayout galleryGrid;
    private Div statsBar;
    private Span imageCountSpan;

    public ImageUploadView(ImageService imageService) {
        this.imageService = imageService;

        addClassName("gallery-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        // Main background
        getStyle()
                .set("background", "linear-gradient(180deg, #0a0a1a 0%, #1a1a2e 50%, #16213e 100%)")
                .set("min-height", "100vh")
                .set("overflow-y", "auto")
                .set("font-family", "'Inter', 'Segoe UI', system-ui, sans-serif");

        // Build layout
        add(createHeader(), createUploadSection(), createStatsBar(), createGallerySection());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        refreshGallery();
    }

    // ── HEADER ────────────────────────────────────────────────────────
    private Div createHeader() {
        Div header = new Div();
        header.getStyle()
                .set("width", "100%")
                .set("padding", "24px 48px")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("border-bottom", "1px solid rgba(255,255,255,0.06)")
                .set("box-sizing", "border-box");

        // Logo area
        Div logoArea = new Div();
        logoArea.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "14px");

        Div logoIcon = new Div();
        logoIcon.setText("📸");
        logoIcon.getStyle()
                .set("font-size", "28px")
                .set("width", "48px")
                .set("height", "48px")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border-radius", "14px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Div logoText = new Div();
        H2 appName = new H2("Image Gallery");
        appName.getStyle()
                .set("color", "white")
                .set("font-size", "22px")
                .set("font-weight", "700")
                .set("margin", "0")
                .set("letter-spacing", "-0.5px");

        Paragraph appTagline = new Paragraph("Upload & Showcase Your Photos");
        appTagline.getStyle()
                .set("color", "rgba(255,255,255,0.4)")
                .set("font-size", "12px")
                .set("margin", "2px 0 0 0");

        logoText.add(appName, appTagline);
        logoArea.add(logoIcon, logoText);

        // User actions
        Div userActions = new Div();
        userActions.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");

        Button logoutBtn = new Button("Logout");
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        logoutBtn.getStyle()
                .set("color", "rgba(255,255,255,0.5)")
                .set("font-size", "14px")
                .set("cursor", "pointer");
        logoutBtn.addClickListener(e ->
                logoutBtn.getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

        userActions.add(logoutBtn);

        header.add(logoArea, userActions);
        return header;
    }

    // ── UPLOAD SECTION ────────────────────────────────────────────────
    private Div createUploadSection() {
        Div section = new Div();
        section.getStyle()
                .set("width", "100%")
                .set("max-width", "900px")
                .set("margin", "32px auto")
                .set("padding", "0 24px")
                .set("box-sizing", "border-box");

        // Upload card with dashed border
        Div uploadCard = new Div();
        uploadCard.getStyle()
                .set("background", "rgba(255, 255, 255, 0.03)")
                .set("border", "2px dashed rgba(102, 126, 234, 0.4)")
                .set("border-radius", "20px")
                .set("padding", "40px")
                .set("text-align", "center")
                .set("transition", "all 0.3s ease")
                .set("cursor", "pointer");

        // Upload icon
        Div uploadIconDiv = new Div();
        uploadIconDiv.getStyle()
                .set("width", "72px")
                .set("height", "72px")
                .set("border-radius", "18px")
                .set("background", "linear-gradient(135deg, rgba(102,126,234,0.2) 0%, rgba(118,75,162,0.2) 100%)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin", "0 auto 20px auto")
                .set("font-size", "32px");
        uploadIconDiv.setText("☁️");

        H3 uploadTitle = new H3("Drop your images here");
        uploadTitle.getStyle()
                .set("color", "white")
                .set("font-size", "20px")
                .set("font-weight", "600")
                .set("margin", "0 0 8px 0");

        Paragraph uploadSubtitle = new Paragraph("PNG, JPG or GIF • Max 10MB per file • Up to 10 files at once");
        uploadSubtitle.getStyle()
                .set("color", "rgba(255,255,255,0.4)")
                .set("font-size", "13px")
                .set("margin", "0 0 24px 0");

        // Vaadin Upload component
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp");
        upload.setMaxFiles(10);
        upload.setMaxFileSize(10 * 1024 * 1024); // 10 MB
        upload.setDropAllowed(true);

        // Style the upload button
        Button uploadButton = new Button("Choose Files");
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        uploadButton.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none")
                .set("border-radius", "12px")
                .set("padding", "12px 32px")
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("cursor", "pointer");
        upload.setUploadButton(uploadButton);

        upload.setDropLabel(new Span(""));
        upload.getStyle().set("width", "100%");

        // Handle upload
        upload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                String mimeType = event.getMIMEType();
                byte[] data = buffer.getInputStream(fileName).readAllBytes();

                imageService.saveImage(fileName, mimeType, data);

                Notification notification = Notification.show(
                        "✅ " + fileName + " uploaded successfully!",
                        3000, Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                refreshGallery();

            } catch (IOException e) {
                Notification.show("❌ Upload failed: " + e.getMessage(),
                        4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFailedListener(event ->
                Notification.show("❌ Upload failed: " + event.getReason().getMessage(),
                        4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR));

        upload.addFileRejectedListener(event ->
                Notification.show("⚠️ " + event.getErrorMessage(),
                        4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR));

        uploadCard.add(uploadIconDiv, uploadTitle, uploadSubtitle, upload);
        section.add(uploadCard);
        return section;
    }

    // ── STATS BAR ─────────────────────────────────────────────────────
    private Div createStatsBar() {
        statsBar = new Div();
        statsBar.getStyle()
                .set("width", "100%")
                .set("max-width", "900px")
                .set("margin", "0 auto 8px auto")
                .set("padding", "0 24px")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center");

        H3 galleryTitle = new H3("Your Gallery");
        galleryTitle.getStyle()
                .set("color", "white")
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("margin", "0");

        imageCountSpan = new Span("0 images");
        imageCountSpan.getStyle()
                .set("color", "rgba(255,255,255,0.4)")
                .set("font-size", "14px")
                .set("background", "rgba(255,255,255,0.06)")
                .set("padding", "6px 16px")
                .set("border-radius", "20px");

        statsBar.add(galleryTitle, imageCountSpan);
        return statsBar;
    }

    // ── GALLERY SECTION ───────────────────────────────────────────────
    private Div createGallerySection() {
        Div section = new Div();
        section.getStyle()
                .set("width", "100%")
                .set("max-width", "900px")
                .set("margin", "0 auto")
                .set("padding", "16px 24px 48px 24px")
                .set("box-sizing", "border-box")
                .set("flex", "1");

        galleryGrid = new FlexLayout();
        galleryGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        galleryGrid.getStyle()
                .set("gap", "20px")
                .set("justify-content", "flex-start");

        section.add(galleryGrid);
        return section;
    }

    // ── REFRESH GALLERY ───────────────────────────────────────────────
    private void refreshGallery() {
        galleryGrid.removeAll();

        List<ImageMetadata> images = imageService.getUserImages();
        imageCountSpan.setText(images.size() + (images.size() == 1 ? " image" : " images"));

        if (images.isEmpty()) {
            Div emptyState = new Div();
            emptyState.getStyle()
                    .set("width", "100%")
                    .set("text-align", "center")
                    .set("padding", "60px 20px");

            Div emptyIcon = new Div();
            emptyIcon.setText("🖼️");
            emptyIcon.getStyle()
                    .set("font-size", "64px")
                    .set("margin-bottom", "20px")
                    .set("opacity", "0.4");

            H3 emptyTitle = new H3("No images yet");
            emptyTitle.getStyle()
                    .set("color", "rgba(255,255,255,0.5)")
                    .set("font-size", "18px")
                    .set("font-weight", "500")
                    .set("margin", "0 0 8px 0");

            Paragraph emptyDesc = new Paragraph("Upload your first image to get started!");
            emptyDesc.getStyle()
                    .set("color", "rgba(255,255,255,0.3)")
                    .set("font-size", "14px")
                    .set("margin", "0");

            emptyState.add(emptyIcon, emptyTitle, emptyDesc);
            galleryGrid.add(emptyState);
            return;
        }

        for (ImageMetadata image : images) {
            galleryGrid.add(createImageCard(image));
        }
    }

    // ── IMAGE CARD ────────────────────────────────────────────────────
    private Div createImageCard(ImageMetadata image) {
        Div card = new Div();
        card.getStyle()
                .set("width", "calc(33.333% - 14px)")
                .set("min-width", "200px")
                .set("background", "rgba(255, 255, 255, 0.04)")
                .set("border", "1px solid rgba(255, 255, 255, 0.08)")
                .set("border-radius", "16px")
                .set("overflow", "hidden")
                .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
                .set("cursor", "pointer")
                .set("position", "relative");

        // Hover effect via JS
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-4px)")
                    .set("box-shadow", "0 20px 40px rgba(102, 126, 234, 0.15)")
                    .set("border-color", "rgba(102, 126, 234, 0.3)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "none")
                    .set("border-color", "rgba(255, 255, 255, 0.08)");
        });

        // Image thumbnail
        Div imageWrapper = new Div();
        imageWrapper.getStyle()
                .set("width", "100%")
                .set("height", "200px")
                .set("overflow", "hidden")
                .set("position", "relative")
                .set("background", "rgba(0,0,0,0.2)");

        StreamResource resource = new StreamResource(
                image.getOriginalFileName(),
                () -> new ByteArrayInputStream(image.getImageData())
        );

        Image img = new Image(resource, image.getOriginalFileName());
        img.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("object-fit", "cover")
                .set("transition", "transform 0.4s ease");

        imageWrapper.add(img);

        // Overlay with actions (visible on hover)
        Div overlay = new Div();
        overlay.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("right", "0")
                .set("bottom", "0")
                .set("background", "linear-gradient(180deg, transparent 40%, rgba(0,0,0,0.7) 100%)")
                .set("opacity", "0")
                .set("transition", "opacity 0.3s ease")
                .set("display", "flex")
                .set("align-items", "flex-end")
                .set("justify-content", "center")
                .set("padding", "12px")
                .set("gap", "8px");

        imageWrapper.getElement().addEventListener("mouseenter", e ->
                overlay.getStyle().set("opacity", "1"));
        imageWrapper.getElement().addEventListener("mouseleave", e ->
                overlay.getStyle().set("opacity", "0"));

        // Expand button
        Button expandBtn = new Button(new Icon(VaadinIcon.EXPAND_FULL));
        expandBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        expandBtn.getStyle()
                .set("color", "white")
                .set("background", "rgba(255,255,255,0.15)")
                .set("backdrop-filter", "blur(10px)")
                .set("border-radius", "10px")
                .set("min-width", "36px")
                .set("height", "36px");
        expandBtn.addClickListener(e -> openImageDialog(image));

        // Delete button
        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        deleteBtn.getStyle()
                .set("color", "#ff6b6b")
                .set("background", "rgba(255,107,107,0.15)")
                .set("backdrop-filter", "blur(10px)")
                .set("border-radius", "10px")
                .set("min-width", "36px")
                .set("height", "36px");
        deleteBtn.addClickListener(e -> confirmDelete(image));

        overlay.add(expandBtn, deleteBtn);
        imageWrapper.add(overlay);

        // Card info section
        Div info = new Div();
        info.getStyle()
                .set("padding", "14px 16px")
                .set("border-top", "1px solid rgba(255,255,255,0.06)");

        // File name
        Paragraph fileName = new Paragraph(image.getOriginalFileName());
        fileName.getStyle()
                .set("color", "rgba(255,255,255,0.9)")
                .set("font-size", "13px")
                .set("font-weight", "500")
                .set("margin", "0 0 6px 0")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        // Meta info row
        Div metaRow = new Div();
        metaRow.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center");

        Span sizeSpan = new Span(image.getFormattedFileSize());
        sizeSpan.getStyle()
                .set("color", "rgba(255,255,255,0.35)")
                .set("font-size", "11px");

        Span dateSpan = new Span(image.getUploadedAt()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateSpan.getStyle()
                .set("color", "rgba(255,255,255,0.35)")
                .set("font-size", "11px");

        metaRow.add(sizeSpan, dateSpan);

        // Description (if exists)
        if (image.getDescription() != null && !image.getDescription().isEmpty()) {
            Paragraph desc = new Paragraph(image.getDescription());
            desc.getStyle()
                    .set("color", "rgba(255,255,255,0.5)")
                    .set("font-size", "12px")
                    .set("margin", "8px 0 0 0")
                    .set("font-style", "italic");
            info.add(fileName, metaRow, desc);
        } else {
            info.add(fileName, metaRow);
        }

        card.add(imageWrapper, info);

        // Click on card opens dialog
        card.addClickListener(e -> openImageDialog(image));

        return card;
    }

    // ── FULL-SIZE IMAGE DIALOG ────────────────────────────────────────
    private void openImageDialog(ImageMetadata image) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("90vw");
        dialog.setMaxWidth("1000px");

        dialog.getElement().getStyle()
                .set("--lumo-base-color", "transparent");

        // Dialog content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("background", "rgba(20, 20, 40, 0.98)")
                .set("border-radius", "20px")
                .set("overflow", "hidden")
                .set("border", "1px solid rgba(255,255,255,0.1)");

        // Header
        HorizontalLayout dialogHeader = new HorizontalLayout();
        dialogHeader.setWidthFull();
        dialogHeader.setPadding(true);
        dialogHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        dialogHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        dialogHeader.getStyle()
                .set("border-bottom", "1px solid rgba(255,255,255,0.08)")
                .set("padding", "16px 24px");

        Div titleArea = new Div();
        H3 dialogTitle = new H3(image.getOriginalFileName());
        dialogTitle.getStyle()
                .set("color", "white")
                .set("font-size", "16px")
                .set("margin", "0");
        Span dialogMeta = new Span(image.getFormattedFileSize() + " • " +
                image.getUploadedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
        dialogMeta.getStyle()
                .set("color", "rgba(255,255,255,0.4)")
                .set("font-size", "12px");
        titleArea.add(dialogTitle, dialogMeta);

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle().set("color", "rgba(255,255,255,0.5)");
        closeBtn.addClickListener(e -> dialog.close());

        dialogHeader.add(titleArea, closeBtn);

        // Image
        Div imageContainer = new Div();
        imageContainer.getStyle()
                .set("width", "100%")
                .set("max-height", "70vh")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background", "rgba(0,0,0,0.3)")
                .set("overflow", "hidden");

        StreamResource resource = new StreamResource(
                image.getOriginalFileName(),
                () -> new ByteArrayInputStream(image.getImageData())
        );

        Image fullImage = new Image(resource, image.getOriginalFileName());
        fullImage.getStyle()
                .set("max-width", "100%")
                .set("max-height", "70vh")
                .set("object-fit", "contain");

        imageContainer.add(fullImage);

        // Footer with description & actions
        Div footer = new Div();
        footer.getStyle()
                .set("padding", "20px 24px")
                .set("border-top", "1px solid rgba(255,255,255,0.08)")
                .set("display", "flex")
                .set("gap", "12px")
                .set("align-items", "flex-end");

        TextField descField = new TextField();
        descField.setPlaceholder("Add a description...");
        descField.setWidthFull();
        descField.getElement().getThemeList().add("dark");
        if (image.getDescription() != null) {
            descField.setValue(image.getDescription());
        }

        Button saveDescBtn = new Button("Save");
        saveDescBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveDescBtn.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border-radius", "10px")
                .set("min-width", "80px");
        saveDescBtn.addClickListener(e -> {
            imageService.updateDescription(image.getId(), descField.getValue());
            Notification.show("✅ Description saved!", 2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshGallery();
        });

        Button deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.getStyle()
                .set("border-radius", "10px")
                .set("min-width", "100px");
        deleteBtn.addClickListener(e -> {
            dialog.close();
            confirmDelete(image);
        });

        footer.add(descField, saveDescBtn, deleteBtn);

        content.add(dialogHeader, imageContainer, footer);
        dialog.add(content);
        dialog.open();
    }

    // ── DELETE CONFIRMATION ───────────────────────────────────────────
    private void confirmDelete(ImageMetadata image) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Image");
        confirmDialog.setText("Are you sure you want to delete \"" + image.getOriginalFileName() + "\"? This action cannot be undone.");
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");

        confirmDialog.addConfirmListener(e -> {
            if (imageService.deleteImage(image.getId())) {
                Notification.show("🗑️ Image deleted", 2000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                refreshGallery();
            } else {
                Notification.show("❌ Failed to delete image", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        confirmDialog.open();
    }
}