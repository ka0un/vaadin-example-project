package com.example.notes.views;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.NoteService;
import com.example.notes.views.components.GalleryCard;
import com.example.notes.views.components.ImageUploadData;
import com.example.notes.views.components.ImageUploadForm;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.PermitAll;

@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Image Gallery | Vaadin Notes App")
@PermitAll
@CssImport("./styles/gallery.css")
public class ImageGalleryView extends VerticalLayout {

    private final NoteService noteService;
    private final User currentUser;
    private final FlexLayout gallery = new FlexLayout();

    public ImageGalleryView(NoteService noteService, UserRepository userRepository, AuthenticationContext authContext) {
        this.noteService = noteService;

        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        addClassName("gallery-view");
        setPadding(true);
        setSpacing(true);

        configureGallery();

        ImageUploadForm uploadForm = new ImageUploadForm(this::saveUploadedImage);

        refreshGallery();

        add(
                createPageHeader(),
                uploadForm,
                gallery
        );
    }

    private H3 createPageHeader() {
        H3 title = new H3("Image Gallery");
        title.addClassName("gallery-title");
        title.getStyle().set("margin", "0");
        return title;
    }

    private void configureGallery() {
        gallery.setWidthFull();
        gallery.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        gallery.setJustifyContentMode(FlexLayout.JustifyContentMode.CENTER);
        gallery.setAlignItems(Alignment.START);
        gallery.addClassName("gallery-grid");
    }

    private void saveUploadedImage(ImageUploadData data) {
        try {
            Note note = new Note(data.caption(), data.imageName(), data.imageMimeType(), data.imageData(), currentUser);
            noteService.saveNote(note);
            refreshGallery();
            showSuccess("Image saved to gallery.");
        } catch (RuntimeException runtimeException) {
            showError("Could not save image right now. Please try again.");
        }
    }

    private void deleteImage(Note note) {
        try {
            noteService.deleteNote(note);
            refreshGallery();
        } catch (RuntimeException runtimeException) {
            showError("Delete failed. Please refresh and try again.");
        }
    }

    private void refreshGallery() {
        List<Note> notes = noteService.getNotes(currentUser);
        updateGallery(notes);
    }

    private void updateGallery(List<Note> notes) {
        gallery.removeAll();
        notes.stream()
                .filter(Note::hasImage)
                .forEach(note -> gallery.add(new GalleryCard(note, () -> deleteImage(note))));
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 2200, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3200, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
