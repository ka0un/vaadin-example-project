package com.example.notes.views;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.NoteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Notes | Vaadin Notes App")
@PermitAll
public class NotesView extends VerticalLayout {

    private final NoteService noteService;
    private final User currentUser;
    private final VerticalLayout notesList = new VerticalLayout();
    private final TextField noteField = new TextField();

    public NotesView(NoteService noteService, UserRepository userRepository, AuthenticationContext authContext) {
        this.noteService = noteService;

        // Fetch current user
        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        configureNoteField();
        updateNotesList();

        add(notesList, createFormLayout());
    }

    private void configureNoteField() {
        noteField.setPlaceholder("Enter a new note...");
        noteField.setWidth("300px");
        noteField.setClearButtonVisible(true);
    }

    private HorizontalLayout createFormLayout() {
        Button addButton = new Button("Add", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(click -> {
            String content = noteField.getValue();
            if (!content.isBlank()) {
                noteService.saveNote(new Note(content, currentUser));
                noteField.clear();
                updateNotesList();
            }
        });

        HorizontalLayout formLayout = new HorizontalLayout(noteField, addButton);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        return formLayout;
    }

    private void updateNotesList() {
        notesList.removeAll();
        noteService.getNotes(currentUser).forEach(note -> {
            Span noteText = new Span(note.getContent());
            Button deleteButton = new Button(VaadinIcon.TRASH.create(), click -> {
                noteService.deleteNote(note);
                updateNotesList();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            HorizontalLayout noteLayout = new HorizontalLayout(noteText, deleteButton);
            noteLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
            noteLayout.setWidthFull();
            noteLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

            notesList.add(noteLayout);
        });
        notesList.setWidth("400px");
    }
}
