package com.example.notes.views.components;

import com.example.notes.data.entity.Image;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.ByteArrayInputStream;

/**
 * A gallery card component for a single image.
 * Displays the thumbnail, a delete button, and a favourite indicator badge.
 */
public class ImageGalleryItem extends Composite<Div> {

    public ImageGalleryItem(Image image, ImageService imageService,
                            Runnable onDelete, Runnable onClick) {

        // --- Thumbnail ---
        StreamResource resource = new StreamResource(
                image.getId() + "_" + image.getName(),
                () -> new ByteArrayInputStream(image.getData()));

        com.vaadin.flow.component.html.Image img =
                new com.vaadin.flow.component.html.Image(resource, image.getName());
        img.setWidth("100%");
        img.setHeight("200px");
        img.getStyle()
                .set("object-fit", "cover")
                .set("cursor", "pointer")
                .set("display", "block");
        img.addClassNames(LumoUtility.BorderRadius.MEDIUM);

        img.addClickListener(e -> {
            if (onClick != null) onClick.run();
        });

        // --- Delete button (top-right overlay) ---
        Button deleteButton = new Button(VaadinIcon.TRASH.create(), click -> {
            imageService.deleteImage(image);
            Notification.show("Image deleted.");
            if (onDelete != null) onDelete.run();
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        deleteButton.addClassNames(LumoUtility.Position.ABSOLUTE);
        deleteButton.getStyle().set("top", "4px").set("right", "4px");

        // --- Favourite indicator (top-left, only shown when marked favourite) ---
        Icon heartIcon = VaadinIcon.HEART.create();
        heartIcon.setColor("var(--lumo-error-color)");
        heartIcon.setSize("18px");
        heartIcon.getStyle().set("position", "absolute").set("top", "6px").set("left", "6px");
        heartIcon.setVisible(image.isFavorite());

        // --- Image name label at bottom ---
        Div nameLabel = new Div();
        nameLabel.setText(image.getName());
        nameLabel.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY,
                LumoUtility.Padding.Horizontal.XSMALL, LumoUtility.Padding.Vertical.XSMALL);
        nameLabel.getStyle()
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("width", "100%");

        // --- Container ---
        Div container = getContent();
        container.getStyle().set("position", "relative");
        container.setWidth("200px");
        container.addClassNames(
                LumoUtility.Margin.SMALL,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.BorderRadius.MEDIUM,
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.SMALL);
        container.getStyle().set("overflow", "hidden");

        container.add(img, deleteButton, heartIcon, nameLabel);
    }
}
