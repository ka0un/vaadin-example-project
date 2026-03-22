package com.example.notes.views;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.NoteService;
import com.example.notes.views.components.NoteFormCard;
import com.example.notes.views.components.NoteItemCard;
import com.example.notes.views.components.NotesListCard;
//import com.vaadin.flow.component.flexlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.io.InputStream;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Notes | Vaadin Notes App")
@PermitAll
public class NotesView extends com.vaadin.flow.component.orderedlayout.VerticalLayout {

    private static final int MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024;

    private final NoteService noteService;
    private final User currentUser;

    private final NoteFormCard formCard = new NoteFormCard(MAX_IMAGE_SIZE_BYTES);
    private final NotesListCard notesListCard = new NotesListCard();

    private Note editingNote;

    private byte[] uploadedImageData;
    private String uploadedImageName;
    private String uploadedImageType;
    private boolean removeExistingImage;

    public NotesView(NoteService noteService,
                     UserRepository userRepository,
                     AuthenticationContext authContext) {
        this.noteService = noteService;

        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        configureLayout();
        configureEvents();

        add(createHeader(), createContentLayout());

        updateNotesList();
    }

    private void configureLayout() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
    }

    private void configureEvents() {
        formCard.getSaveButton().addClickListener(event -> saveOrUpdateNote());
        formCard.getCancelEditButton().addClickListener(event -> resetForm());
        formCard.getRemoveImageButton().addClickListener(event -> handleRemoveImage());

        formCard.getImageUpload().addSucceededListener(event -> {
            try (InputStream inputStream = formCard.getImageBuffer().getInputStream()) {
                uploadedImageData = inputStream.readAllBytes();
                uploadedImageName = event.getFileName();
                uploadedImageType = event.getMIMEType();
                removeExistingImage = false;

                formCard.showPreview(uploadedImageData, uploadedImageName);
                formCard.setImageStatusText("Selected image: " + uploadedImageName);
                formCard.showRemoveImageButton(true);

                showSuccess("Image uploaded successfully.");
            } catch (IOException e) {
                clearPendingUpload();
                showError("Failed to read uploaded image.");
            }
        });

        formCard.getImageUpload().addFileRejectedListener(event ->
                showError("Image rejected: " + event.getErrorMessage())
        );
    }

    private H3 createHeader() {
        H3 header = new H3("My Notes");
        header.getStyle()
                .set("margin", "0")
                .set("margin-bottom", "16px");
        return header;
    }

    private FlexLayout createContentLayout() {
        FlexLayout contentLayout = new FlexLayout(formCard, notesListCard);
        contentLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        contentLayout.setWidthFull();
        contentLayout.setJustifyContentMode(FlexLayout.JustifyContentMode.CENTER);
        contentLayout.setAlignItems(FlexLayout.Alignment.START);
        contentLayout.getStyle().set("gap", "16px");

        formCard.setWidth("min(520px, 100%)");
        notesListCard.setWidth("min(520px, 100%)");

        return contentLayout;
    }

    private void saveOrUpdateNote() {
        String content = formCard.getNoteField().getValue() != null
                ? formCard.getNoteField().getValue().trim()
                : "";

        if (content.isBlank()) {
            showError("Note content cannot be empty.");
            return;
        }

        Note noteToSave = editingNote != null ? editingNote : new Note();
        noteToSave.setContent(content);
        noteToSave.setUser(currentUser);

        if (uploadedImageData != null) {
            noteToSave.setImageData(uploadedImageData);
            noteToSave.setImageName(uploadedImageName);
            noteToSave.setImageType(uploadedImageType);
        } else if (removeExistingImage) {
            noteToSave.setImageData(null);
            noteToSave.setImageName(null);
            noteToSave.setImageType(null);
        }

        noteService.saveNote(noteToSave);

        showSuccess(editingNote != null ? "Note updated successfully." : "Note added successfully.");

        resetForm();
        updateNotesList();
    }

    private void updateNotesList() {
        notesListCard.setNotes(
                noteService.getNotes(currentUser),
                this::loadNoteForEditing,
                this::deleteNote
        );
    }

    private void loadNoteForEditing(Note note) {
        editingNote = note;
        formCard.getNoteField().setValue(note.getContent());

        clearPendingUpload();
        removeExistingImage = false;

        if (note.hasImage()) {
            formCard.showPreview(note.getImageData(), note.getImageName());
            formCard.setImageStatusText("Current image: " + safeFileName(note.getImageName()));
            formCard.showRemoveImageButton(true);
        } else {
            formCard.hidePreview();
            formCard.setImageStatusText("No image selected");
            formCard.showRemoveImageButton(false);
        }

        formCard.setEditMode(true);
    }

    private void deleteNote(Note note) {
        noteService.deleteNote(note);

        if (editingNote != null && editingNote.getId().equals(note.getId())) {
            resetForm();
        }

        updateNotesList();
        showSuccess("Note deleted successfully.");
    }

    private void resetForm() {
        editingNote = null;
        formCard.getNoteField().clear();

        clearPendingUpload();
        removeExistingImage = false;

        formCard.getImageUpload().clearFileList();
        formCard.hidePreview();
        formCard.setImageStatusText("No image selected");
        formCard.showRemoveImageButton(false);
        formCard.setEditMode(false);
    }

    private void handleRemoveImage() {
        uploadedImageData = null;
        uploadedImageName = null;
        uploadedImageType = null;
        removeExistingImage = true;

        formCard.getImageUpload().clearFileList();
        formCard.hidePreview();
        formCard.setImageStatusText("Image will be removed when you save the note.");
        formCard.showRemoveImageButton(false);
    }

    private void clearPendingUpload() {
        uploadedImageData = null;
        uploadedImageName = null;
        uploadedImageType = null;
    }

    private String safeFileName(String fileName) {
        return fileName == null || fileName.isBlank() ? "image" : fileName;
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 2500, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}