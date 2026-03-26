package com.example.notes.views;

import com.example.notes.data.entity.Image;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.StreamResource;

/**
 * A reusable card component that displays a single image
 * with its filename and a delete button.
 */
public class ImageCard extends Div {

    public ImageCard(Image image, ImageService imageService, Runnable onDelete) {

        // Card container styling
        getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("overflow", "hidden")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
                .set("display", "flex")
                .set("flex-direction", "column");

        // Image element
        StreamResource resource = imageService.getImageResource(image);
        com.vaadin.flow.component.html.Image img =
                new com.vaadin.flow.component.html.Image(resource, image.getOriginalFileName());
        img.getStyle()
                .set("width", "100%")
                .set("height", "180px")
                .set("object-fit", "cover")
                .set("display", "block");

        // Bottom info bar
        Div info = new Div();
        info.getStyle()
                .set("padding", "8px 12px")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center");

        // Truncated filename label
        Span name = new Span(image.getOriginalFileName());
        name.getStyle()
                .set("font-size", "12px")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap")
                .set("max-width", "140px");

        // Delete button — calls back to parent to refresh gallery
        Button deleteBtn = new Button("Delete");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteBtn.addClickListener(e -> {
            imageService.deleteImage(image.getId());
            Notification.show("Image deleted.")
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            onDelete.run(); // tells GalleryView to refresh
        });

        info.add(name, deleteBtn);
        add(img, info);
    }
}