package com.example.notes.views.components;

import java.io.ByteArrayInputStream;

import com.example.notes.data.entity.Note;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;

/**
 * Reusable gallery card component for a single note.
 */
public class GalleryCard extends Div {

    public GalleryCard(Note note, Runnable onDelete) {
        addClassName("gallery-card");

        if (note.hasImage()) {
            Image image = new Image(createImageResource(note),
                    note.getImageName() != null ? note.getImageName() : "Uploaded image");
            image.addClassName("gallery-image");
            image.setWidthFull();
            add(image);
        }

        String caption = note.getContent() == null || note.getContent().isBlank()
                ? "(No caption)"
                : note.getContent();
        Paragraph captionText = new Paragraph(caption);
        captionText.addClassName("gallery-caption");

        Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create(), click -> onDelete.run());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);

        HorizontalLayout footer = new HorizontalLayout(deleteButton);
        footer.addClassName("gallery-footer");

        add(captionText, footer);
    }

    private StreamResource createImageResource(Note note) {
        String extension = "image/jpeg".equals(note.getImageMimeType()) ? ".jpg" : "";
        String resourceName = "note-" + note.getId() + extension;
        StreamResource resource = new StreamResource(resourceName,
                () -> new ByteArrayInputStream(note.getImageData()));

        if (note.getImageMimeType() != null && !note.getImageMimeType().isBlank()) {
            resource.setContentType(note.getImageMimeType());
        }

        return resource;
    }
}
