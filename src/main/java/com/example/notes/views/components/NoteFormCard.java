package com.example.notes.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;

public class NoteFormCard extends VerticalLayout {

    private final TextArea noteField = new TextArea("Note");
    private final MemoryBuffer imageBuffer = new MemoryBuffer();
    private final Upload imageUpload = new Upload(imageBuffer);

    private final Image formImagePreview = new Image();
    private final Span imageStatus = new Span("No image selected");
    private final Button removeImageButton = new Button("Remove image");

    private final Button saveButton = new Button("Add Note", VaadinIcon.PLUS.create());
    private final Button cancelEditButton = new Button("Cancel Edit");

    public NoteFormCard(int maxImageSizeBytes) {
        configureLayout();
        configureNoteField();
        configureUpload(maxImageSizeBytes);
        configurePreview();
        configureButtons();
    }

    private void configureLayout() {
        H3 formTitle = new H3("Create Note");
        formTitle.getStyle().set("margin", "0");

        HorizontalLayout actionButtons = new HorizontalLayout(saveButton, cancelEditButton);
        actionButtons.setSpacing(true);
        actionButtons.setWidthFull();
        actionButtons.getStyle().set("flex-wrap", "wrap");

        add(
                formTitle,
                noteField,
                imageUpload,
                imageStatus,
                formImagePreview,
                removeImageButton,
                actionButtons
        );

        setWidthFull();
        setSpacing(true);
        setPadding(true);
        getStyle()
                .set("border", "1px solid #dcdcdc")
                .set("border-radius", "12px")
                .set("background", "#ffffff")
                .set("min-height", "470px");
    }

    private void configureNoteField() {
        noteField.setPlaceholder("Write your note here...");
        noteField.setWidthFull();
        noteField.setMaxLength(1000);
        noteField.setMinHeight("140px");
        noteField.setClearButtonVisible(true);
    }

    private void configureUpload(int maxImageSizeBytes) {
        imageUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg", "image/gif");
        imageUpload.setMaxFileSize(maxImageSizeBytes);
        imageUpload.setDropLabel(new Span("Drag and drop an image here, or use the upload button"));
        imageUpload.setWidthFull();
    }

    private void configurePreview() {
        formImagePreview.setWidth("220px");
        formImagePreview.setMaxWidth("100%");
        formImagePreview.setVisible(false);

        removeImageButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        removeImageButton.setVisible(false);
    }

    private void configureButtons() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelEditButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelEditButton.setVisible(false);
    }

    public TextArea getNoteField() {
        return noteField;
    }

    public MemoryBuffer getImageBuffer() {
        return imageBuffer;
    }

    public Upload getImageUpload() {
        return imageUpload;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getCancelEditButton() {
        return cancelEditButton;
    }

    public Button getRemoveImageButton() {
        return removeImageButton;
    }

    public void setImageStatusText(String text) {
        imageStatus.setText(text);
    }

    public void showPreview(byte[] imageBytes, String fileName) {
        StreamResource resource = new StreamResource(
                fileName != null ? fileName : "preview-image",
                () -> new ByteArrayInputStream(imageBytes)
        );
        formImagePreview.setSrc(resource);
        formImagePreview.setAlt(fileName != null ? fileName : "image");
        formImagePreview.setVisible(true);
    }

    public void hidePreview() {
        formImagePreview.setVisible(false);
        formImagePreview.setSrc("");
    }

    public void showRemoveImageButton(boolean visible) {
        removeImageButton.setVisible(visible);
    }

    public void setEditMode(boolean editing) {
        if (editing) {
            saveButton.setText("Update Note");
            saveButton.setIcon(VaadinIcon.CHECK.create());
            cancelEditButton.setVisible(true);
        } else {
            saveButton.setText("Add Note");
            saveButton.setIcon(VaadinIcon.PLUS.create());
            cancelEditButton.setVisible(false);
        }
    }
}