package com.example.notes.views;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.NoteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("My Notes | Vaadin Notes App")
@PermitAll
@CssImport("./styles/notes.css")
public class NotesView extends VerticalLayout {

    private static final int NOTE_MAX_LENGTH = 1000;

    private final NoteService noteService;
    private final User currentUser;

    private final TextArea noteInput = new TextArea();
    private final VerticalLayout notesList = new VerticalLayout();

    public NotesView(NoteService noteService, UserRepository userRepository, AuthenticationContext authContext) {
        this.noteService = noteService;

        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        addClassName("notes-view");
        setPadding(true);
        setSpacing(true);

        configureNoteInput();
        configureNotesList();

        refreshNotes();

        add(
                createPageHeader(),
                createTextNotesSection()
        );
    }

    private H3 createPageHeader() {
        H3 title = new H3("My Notes");
        title.addClassName("notes-title");
        title.getStyle().set("margin", "0");
        return title;
    }

    private void configureNoteInput() {
        noteInput.setPlaceholder("Write a note...");
        noteInput.setWidthFull();
        noteInput.setMaxLength(NOTE_MAX_LENGTH);
        noteInput.setClearButtonVisible(true);
        noteInput.setMinHeight("100px");
    }

    private void configureNotesList() {
        notesList.setWidthFull();
        notesList.setPadding(false);
        notesList.setSpacing(true);
        notesList.addClassName("notes-list");
    }

    private VerticalLayout createTextNotesSection() {
        Button addNoteButton = new Button("Add Note", VaadinIcon.PLUS.create(), click -> addNote());
        addNoteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addNoteButton.setWidthFull();

        VerticalLayout section = new VerticalLayout(noteInput, addNoteButton, notesList);
        section.setWidthFull();
        section.setMaxWidth("800px");
        section.setPadding(true);
        section.setSpacing(true);
        section.addClassName("notes-section");
        return section;
    }

    private void addNote() {
        String content = noteInput.getValue() == null ? "" : noteInput.getValue().trim();

        if (content.isBlank()) {
            showWarning("Please enter a note before saving.");
            return;
        }

        if (content.length() > NOTE_MAX_LENGTH) {
            showError("Note is too long. Maximum 1000 characters.");
            return;
        }

        try {
            noteService.saveNote(new Note(content, currentUser));
            noteInput.clear();
            refreshNotes();
            showSuccess("Note added.");
        } catch (RuntimeException runtimeException) {
            showError("Could not save note right now. Please try again.");
        }
    }

    private void deleteNote(Note note) {
        try {
            noteService.deleteNote(note);
            refreshNotes();
        } catch (RuntimeException runtimeException) {
            showError("Delete failed. Please refresh and try again.");
        }
    }

    private void refreshNotes() {
        List<Note> notes = noteService.getNotes(currentUser);
        updateNotesList(notes);
    }

    private void updateNotesList(List<Note> notes) {
        notesList.removeAll();

        notes.stream()
                .filter(note -> !note.hasImage())
                .forEach(note -> notesList.add(createNoteRow(note)));
    }

    private HorizontalLayout createNoteRow(Note note) {
        Paragraph noteText = new Paragraph(note.getContent());
        noteText.getStyle().set("margin", "0");
        noteText.addClassName("note-content");

        Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create(), click -> deleteNote(note));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.setIconAfterText(false);

        HorizontalLayout row = new HorizontalLayout(noteText, deleteButton);
        row.setWidthFull();
        row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        row.expand(noteText);
        row.addClassName("note-row");
        return row;
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 2200, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showWarning(String message) {
        Notification notification = Notification.show(message, 2800, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3200, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
