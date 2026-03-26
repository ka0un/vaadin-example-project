package com.example.notes.views;

import com.example.notes.data.entity.ImageItem;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;

import java.time.format.DateTimeFormatter;

/**
 * A self-contained card component that displays a single gallery image.
 * Shows the image thumbnail, original filename, caption, upload date,
 * and a delete button that fires a DeleteEvent when clicked.
 */
public class GalleryImageCard extends Div {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm");

    /**
     * Event fired when the user clicks the delete button on this card.
     */
    public static class DeleteEvent extends ComponentEvent<GalleryImageCard> {
        private final Long imageId;

        public DeleteEvent(GalleryImageCard source, Long imageId) {
            super(source, false);
            this.imageId = imageId;
        }

        public Long getImageId() { return imageId; }
    }

    /**
     * Event fired when the user clicks the card to preview the image.
     */
    public static class PreviewEvent extends ComponentEvent<GalleryImageCard> {
        private final ImageItem imageItem;

        public PreviewEvent(GalleryImageCard source, ImageItem imageItem) {
            super(source, false);
            this.imageItem = imageItem;
        }

        public ImageItem getImageItem() { return imageItem; }
    }

    public GalleryImageCard(ImageItem item) {
        addClassName("gallery-card");

        // Open preview on click
        getElement().addEventListener("click", e -> fireEvent(new PreviewEvent(this, item)))
                .setFilter("event.target.closest('.gallery-card__delete') == null");

        // ── Thumbnail ──────────────────────────────────────────────────────
        Image thumbnail = new Image("/images/" + item.getFilename(), item.getOriginalName());
        thumbnail.addClassName("gallery-card__thumb");
        thumbnail.setAlt(item.getOriginalName());

        // ── Delete button ──────────────────────────────────────────────────
        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.addClassName("gallery-card__delete");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY);
        deleteBtn.setAriaLabel("Delete image");
        deleteBtn.setTooltipText("Delete this image");
        deleteBtn.addClickListener(e -> fireEvent(new DeleteEvent(this, item.getId())));

        add(thumbnail, deleteBtn);
    }

    /**
     * Registers a listener for the delete event emitted by this card.
     */
    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }

    /**
     * Registers a listener for the preview event emitted by this card.
     */
    public Registration addPreviewListener(ComponentEventListener<PreviewEvent> listener) {
        return addListener(PreviewEvent.class, listener);
    }
}
