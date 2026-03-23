package com.example.notes.views;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.NoteService;
import com.example.notes.service.UserGroupService;
import com.example.notes.views.components.NoteCard;
import com.example.notes.views.components.NoteForm;
import com.example.notes.views.components.PublicLinkDialog;
import com.example.notes.views.components.ShareDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "", layout = MainLayout.class)
@PageTitle("My Notes | Vaadin Notes App")
@RolesAllowed("USER")
public class NotesView extends VerticalLayout {

    private final NoteService noteService;
    private final User currentUser;
    
    private final VerticalLayout notesContainer = new VerticalLayout();
    private final NoteForm noteForm = new NoteForm();
    private final VerticalLayout sidebar = new VerticalLayout();

    private final UserGroupService groupService;
    private final AuthenticationContext authContext;

    public NotesView(NoteService noteService, UserGroupService groupService, AuthenticationContext authContext, UserRepository userRepository) {
        this.noteService = noteService;
        this.groupService = groupService;
        this.authContext = authContext;
        // Fetch current user
        String username = authContext.getPrincipalName()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        setupLayout();
        configureForm();
        noteForm.setNote(new Note()); // give binder an empty Note to write into
        refreshNotes();
    }

    private void setupLayout() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("notes-view");

        // Responsive header
        H2 header = new H2("My Notebook");
        header.getStyle().set("margin-top", "0");
        
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(true);

        // Responsive behavior: stack on small screens, side-by-side on large
        notesContainer.setWidthFull();
        notesContainer.setPadding(false);
        notesContainer.setSpacing(true);
        notesContainer.addClassName("notes-list");

        sidebar.setWidth("350px");
        sidebar.setMinWidth("300px");
        sidebar.setPadding(true);
        sidebar.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        sidebar.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        sidebar.addClassName("note-sidebar");

        sidebar.add(new H2("New Note"), noteForm);

        mainContent.add(notesContainer, sidebar);
        mainContent.setFlexGrow(1, notesContainer);
        mainContent.setFlexGrow(0, sidebar);

        add(header, mainContent);
    }

    private void configureForm() {
        noteForm.setOnSave(note -> {
            note.setUser(currentUser);
            noteService.saveNote(note);
            noteForm.setNote(new Note()); // reset binder with a fresh Note
            noteForm.clear();
            refreshNotes();
            Notification.show("Note saved!");
        });
        
        noteForm.setOnCancel(() -> {
            noteForm.setNote(new Note()); // reset binder on cancel too
            noteForm.clear();
        });
    }

    private void refreshNotes() {
        notesContainer.removeAll();
        noteService.getAllAccessibleNotes(currentUser).forEach(note -> {
            NoteCard card = new NoteCard(
                note, 
                currentUser,
                this::deleteNote,
                this::openShareDialog,
                this::openPublicLinkDialog,
                this::openEditDialog
            );
            notesContainer.add(card);
        });
        
        if (notesContainer.getComponentCount() == 0) {
            notesContainer.add(new Section(new H2("No notes yet. Create one on the right!")));
        }
    }

    private void deleteNote(Note note) {
        noteService.deleteNote(note);
        refreshNotes();
        Notification.show("Note deleted");
    }

    private void openShareDialog(Note note) {
        new ShareDialog(note, noteService, groupService, currentUser).open();
    }

    private void openPublicLinkDialog(Note note) {
        new PublicLinkDialog(note, noteService).open();
    }

    private void openEditDialog(Note note) {
        Dialog editDialog = new Dialog();
        editDialog.setHeaderTitle("Edit Note");
        
        NoteForm editForm = new NoteForm();
        editForm.setNote(note);
        editForm.setOnSave(updatedNote -> {
            noteService.saveNote(updatedNote);
            refreshNotes();
            editDialog.close();
        });
        editForm.setOnCancel(editDialog::close);
        
        editDialog.add(editForm);
        editDialog.open();
    }
}
