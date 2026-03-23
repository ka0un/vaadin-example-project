package com.example.notes.views.components;

import com.example.notes.data.entity.ImageEntity;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;

public class ImageCard extends VerticalLayout {

    private final ImageService imageService;
    private final ImageEntity imageEntity;
    private final Runnable onDeleteCallback;

    public ImageCard(ImageEntity imageEntity, ImageService imageService, Runnable onDeleteCallback) {
        this.imageEntity = imageEntity;
        this.imageService = imageService;
        this.onDeleteCallback = onDeleteCallback;

        configureCard();
    }

    private void configureCard() {
        // Card container styling
        setPadding(false);
        setSpacing(false);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setWidth("240px");

        getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.1)")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease")
                .set("cursor", "pointer");

        // Hover effect via JS
        getElement().addEventListener("mouseover", e ->
                getStyle()
                        .set("transform", "translateY(-4px)")
                        .set("box-shadow", "0 8px 24px rgba(0,0,0,0.15)")
        );
        getElement().addEventListener("mouseout", e ->
                getStyle()
                        .set("transform", "translateY(0)")
                        .set("box-shadow", "0 4px 16px rgba(0,0,0,0.1)")
        );


        // Load and display image from disk as bytes
        byte[] imageBytes = imageService.loadImageAsBytes(imageEntity);

        // Create a StreamResource so Vaadin can serve the image
        StreamResource resource = new StreamResource(
                imageEntity.getFileName(),
                () -> new ByteArrayInputStream(imageBytes)
        );

        // Create the image component
        Image image = new Image(resource, imageEntity.getFileName());
        image.setWidth("240px");
        image.setHeight("180px");
        image.getStyle()
                .set("object-fit", "cover")  // crop image nicely to fit
                .set("display", "block")
                .set("cursor", "zoom-in");

        // Click on image to open full view dialog
        image.addClickListener(e -> openImageDialog(resource));

        // Bottom info section
        Div infoSection = new Div();
        infoSection.getStyle()
                .set("padding", "12px")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        // File name
        Paragraph fileName = new Paragraph(imageEntity.getFileName());
        fileName.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("color", "#333")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("max-width", "180px");

        // Upload date
        String formattedDate = imageEntity.getUploadedAt()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        Paragraph uploadDate = new Paragraph("📅 " + formattedDate);
        uploadDate.getStyle()
                .set("margin", "0")
                .set("font-size", "11px")
                .set("color", "#999");

        // Delete button
        Button deleteButton = new Button(VaadinIcon.TRASH.create(), click -> deleteImage());
        deleteButton.addThemeVariants(
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL
        );

        // Row with name + delete
        Div bottomRow = new Div();
        bottomRow.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("margin-top", "8px");

        bottomRow.add(new Div(fileName, uploadDate), deleteButton);
        infoSection.add(bottomRow);

        add(image, infoSection);

    }

    private void openImageDialog(StreamResource resource) {
        Dialog dialog = new Dialog();
        dialog.setWidth("auto");
        dialog.setMaxWidth("90vw");
        dialog.setMaxHeight("90vh");
        dialog.setCloseOnOutsideClick(true);
        dialog.getElement().getStyle()
                .set("padding", "0")
                .set("border-radius", "16px")
                .set("overflow", "hidden");

        // Full size image inside dialog
        Image fullImage = new Image(resource, imageEntity.getFileName());
        fullImage.setMaxWidth("85vw");
        fullImage.setMaxHeight("80vh");
        fullImage.getStyle()
                .set("object-fit", "contain")
                .set("display", "block")
                .set("border-radius", "12px 12px 0 0");

        // Bottom bar inside dialog
        Div dialogBottom = new Div();
        dialogBottom.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("padding", "12px 16px")
                .set("background", "white")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        // File name and date
        Paragraph name = new Paragraph(imageEntity.getFileName());
        name.getStyle()
                .set("margin", "0")
                .set("font-weight", "600")
                .set("font-size", "14px")
                .set("color", "#333");

        String formattedDate = imageEntity.getUploadedAt()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        Paragraph date = new Paragraph("📅 " + formattedDate);
        date.getStyle()
                .set("margin", "2px 0 0 0")
                .set("font-size", "12px")
                .set("color", "#999");

        Div fileInfo = new Div(name, date);

        // Close button
        Button closeButton = new Button("✕ Close", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.getStyle().set("color", "#666");

        dialogBottom.add(fileInfo, closeButton);

        // Dark background container
        Div container = new Div();
        container.getStyle()
                .set("background", "#1a1a2e")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center");

        container.add(fullImage, dialogBottom);
        dialog.add(container);
        dialog.open();
    }

    private void deleteImage() {
        try {
            imageService.deleteImage(imageEntity);

            // Show success message
            Notification n = Notification.show("🗑️ Image deleted!");
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            n.setDuration(3000);

            // Refresh the gallery
            onDeleteCallback.run();

        } catch (Exception e) {
            Notification n = Notification.show("❌ Failed to delete image!");
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            n.setDuration(3000);
        }
    }
}

