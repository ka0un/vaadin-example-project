package com.example.notes.views.components;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class NoteCard extends HorizontalLayout {

    private final Note note;
    private final User currentUser;
    private final Consumer<Note> onDelete;
    private final Consumer<Note> onShare;
    private final Consumer<Note> onPublicLink;
    private final Consumer<Note> onEdit;

    public NoteCard(Note note, User currentUser, 
                    Consumer<Note> onDelete, 
                    Consumer<Note> onShare, 
                    Consumer<Note> onPublicLink,
                    Consumer<Note> onEdit) {
        this.note = note;
        this.currentUser = currentUser;
        this.onDelete = onDelete;
        this.onShare = onShare;
        this.onPublicLink = onPublicLink;
        this.onEdit = onEdit;

        addClassName("note-card");
        setWidthFull();
        setSpacing(true);
        setPadding(true);
        setDefaultVerticalComponentAlignment(Alignment.START);
        setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Styling the card
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("box-shadow", "var(--lumo-box-shadow-xs)");
        getStyle().set("padding", "var(--lumo-space-m)");
        getStyle().set("margin-bottom", "var(--lumo-space-s)");
        getStyle().set("transition", "transform 0.2s, box-shadow 0.2s");
        
        // Hover effect using Vaadin's style API
        getElement().addEventListener("mouseenter", e -> {
            getStyle().set("transform", "translateY(-2px)");
            getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        });
        getElement().addEventListener("mouseleave", e -> {
            getStyle().set("transform", "none");
            getStyle().set("box-shadow", "var(--lumo-box-shadow-xs)");
        });

        initLayout();
    }

    private void initLayout() {
        boolean isOwner = note.getUser().equals(currentUser);

        VerticalLayout noteContent = new VerticalLayout();
        noteContent.setSpacing(false);
        noteContent.setPadding(false);

        Span titleText = new Span(note.getTitle());
        titleText.getStyle().set("font-weight", "600");
        titleText.getStyle().set("font-size", "var(--lumo-font-size-l)");
        titleText.getStyle().set("color", "var(--lumo-primary-text-color)");

        Span contentText = new Span(note.getContent());
        contentText.getStyle().set("white-space", "pre-wrap");
        contentText.getStyle().set("color", "var(--lumo-body-text-color)");
        contentText.getStyle().set("margin-top", "var(--lumo-space-s)");

        noteContent.add(titleText, contentText);

        if (!isOwner) {
            Span sharedBy = new Span("Shared by: " + note.getUser().getUsername());
            sharedBy.getStyle().set("font-size", "var(--lumo-font-size-xs)");
            sharedBy.getStyle().set("color", "var(--lumo-secondary-text-color)");
            sharedBy.getStyle().set("margin-top", "var(--lumo-space-xs)");
            noteContent.add(sharedBy);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");
        Span timestamp = new Span("Updated: " + note.getUpdatedAt().format(formatter));
        timestamp.getStyle().set("font-size", "var(--lumo-font-size-xxs)");
        timestamp.getStyle().set("color", "var(--lumo-tertiary-text-color)");
        timestamp.getStyle().set("margin-top", "var(--lumo-space-xs)");
        noteContent.add(timestamp);

        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);

        if (isOwner) {
            Button shareBtn = new Button(VaadinIcon.SHARE.create(), click -> onShare.accept(note));
            shareBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            shareBtn.setTooltipText("Share with user");

            Button publicLinkBtn = new Button(VaadinIcon.LINK.create(), click -> onPublicLink.accept(note));
            publicLinkBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            publicLinkBtn.setTooltipText("Public link");

            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), click -> onDelete.accept(note));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.setTooltipText("Delete note");

            actionButtons.add(shareBtn, publicLinkBtn, deleteBtn);
        } else {
            // Check for edit permission (simplified for now, logic inside NotesView)
            Button editBtn = new Button(VaadinIcon.EDIT.create(), click -> onEdit.accept(note));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editBtn.setTooltipText("Edit shared note");
            actionButtons.add(editBtn);
        }

        add(noteContent, actionButtons);
    }
}
