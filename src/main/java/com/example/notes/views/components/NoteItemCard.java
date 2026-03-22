package com.example.notes.views.components;

import com.example.notes.data.entity.Note;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;

import java.io.ByteArrayInputStream;

public class NoteItemCard extends Div {

    public interface EditListener {
        void onEdit(Note note);
    }

    public interface DeleteListener {
        void onDelete(Note note);
    }

    public NoteItemCard(Note note, EditListener editListener, DeleteListener deleteListener) {
        setWidthFull();
        getStyle()
                .set("border", "1px solid #dcdcdc")
                .set("border-radius", "12px")
                .set("padding", "16px")
                .set("background", "#ffffff")
                .set("margin-bottom", "12px");

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSpacing(true);
        contentLayout.setPadding(false);
        contentLayout.setWidthFull();

        Span noteContent = new Span(note.getContent());
        noteContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("font-size", "15px");

        contentLayout.add(noteContent);

        if (note.hasImage()) {
            StreamResource imageResource = new StreamResource(
                    note.getImageName() != null ? note.getImageName() : "note-image",
                    () -> new ByteArrayInputStream(note.getImageData())
            );

            Image noteImage = new Image(imageResource, "Attached image");
            noteImage.setWidth("180px");
            noteImage.setMaxWidth("100%");
            noteImage.getStyle()
                    .set("border-radius", "8px")
                    .set("margin-top", "8px")
                    .set("cursor", "pointer");

            // Open large preview when user clicks the thumbnail
            noteImage.addClickListener(event -> openImagePreviewDialog(imageResource, note.getImageName()));

            contentLayout.add(noteImage);
        }

        Button editButton = new Button("Edit", VaadinIcon.EDIT.create(), event -> editListener.onEdit(note));
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create(), event -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Delete Note");
            dialog.setText("Are you sure you want to delete this note? This action cannot be undone.");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(confirmEvent -> deleteListener.onDelete(note));
            dialog.open();
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonRow = new HorizontalLayout(editButton, deleteButton);
        buttonRow.setSpacing(true);
        buttonRow.getStyle().set("flex-wrap", "wrap");

        contentLayout.add(buttonRow);
        add(contentLayout);
    }

    private void openImagePreviewDialog(StreamResource imageResource, String imageName) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(imageName != null && !imageName.isBlank() ? imageName : "Image Preview");
        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setWidth("800px");
        dialog.setMaxWidth("95vw");

        Image previewImage = new Image(imageResource, "Preview image");
        previewImage.setWidthFull();
        previewImage.getStyle()
                .set("max-height", "75vh")
                .set("object-fit", "contain")
                .set("display", "block");

        dialog.add(previewImage);

        Button closeButton = new Button("Close", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(closeButton);

        dialog.open();
    }
}