package com.example.notes.components;

import com.example.notes.data.entity.ImageEntity;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

/**
 * A reusable UI component that displays an image in a styled card format.
 * Includes interactive selection logic for bulk operations.
 */
public class ImageCardComponent extends VerticalLayout {

    private final ImageEntity imageEntity;
    private final Consumer<ImageCardComponent> onToggleSelection;
    private boolean selected = false;
    
    private final Image image;
    private final Icon checkIcon;

    public ImageCardComponent(ImageEntity imageEntity, Consumer<ImageCardComponent> onToggleSelection) {
        this.imageEntity = imageEntity;
        this.onToggleSelection = onToggleSelection;

        addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.MEDIUM);
        setPadding(false);
        setSpacing(false);
        
        // Structural CSS properties
        getStyle().set("overflow", "hidden");
        setWidthFull();
        getStyle().set("position", "relative");
        getStyle().set("cursor", "pointer");
        getStyle().set("transition", "transform 0.2s, box-shadow 0.2s, border 0.2s");
        
        // Make the entire card clickable to toggle selection
        getElement().addEventListener("click", e -> toggleSelection());

        // Stream resource for loading the image
        StreamResource resource = new StreamResource(imageEntity.getFilename(), 
                () -> new ByteArrayInputStream(imageEntity.getData()));
        
        image = new Image(resource, imageEntity.getFilename());
        image.setWidth("100%");
        image.setHeight("250px");
        image.getStyle().set("object-fit", "cover");
        image.getStyle().set("transition", "opacity 0.2s");

        // Selected checkmark overlay (hidden by default)
        checkIcon = VaadinIcon.CHECK_CIRCLE.create();
        checkIcon.setColor("var(--lumo-primary-color)");
        checkIcon.setSize("40px");
        checkIcon.getStyle().set("position", "absolute");
        checkIcon.getStyle().set("top", "10px");
        checkIcon.getStyle().set("right", "10px");
        checkIcon.getStyle().set("background", "white");
        checkIcon.getStyle().set("border-radius", "50%");
        checkIcon.setVisible(false);

        add(image, checkIcon);
    }

    public void toggleSelection() {
        this.selected = !this.selected;
        updateSelectionVisuals();
        onToggleSelection.accept(this);
    }

    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
        updateSelectionVisuals();
    }

    public ImageEntity getImageEntity() {
        return imageEntity;
    }

    private void updateSelectionVisuals() {
        if (selected) {
            getStyle().set("box-shadow", "0 0 0 4px var(--lumo-primary-color)");
            image.getStyle().set("opacity", "0.8");
            checkIcon.setVisible(true);
        } else {
            getStyle().set("box-shadow", "var(--lumo-box-shadow-m)");
            image.getStyle().set("opacity", "1");
            checkIcon.setVisible(false);
        }
    }
}
