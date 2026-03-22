package com.example.notes.views.components;

import com.example.notes.data.entity.Note;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class NotesListCard extends VerticalLayout {

    private final VerticalLayout notesList = new VerticalLayout();

    public NotesListCard() {
        H3 notesTitle = new H3("Saved Notes");
        notesTitle.getStyle().set("margin", "0");

        notesList.setWidthFull();
        notesList.setSpacing(true);
        notesList.setPadding(true);

        Scroller scroller = new Scroller(notesList);
        scroller.setWidthFull();
        scroller.setHeight("420px");

        add(notesTitle, scroller);

        setWidthFull();
        setSpacing(true);
        setPadding(true);
        getStyle()
                .set("border", "1px solid #dcdcdc")
                .set("border-radius", "12px")
                .set("background", "#ffffff")
                .set("min-height", "470px");
    }

    public void setNotes(List<Note> notes,
                         NoteItemCard.EditListener editListener,
                         NoteItemCard.DeleteListener deleteListener) {
        notesList.removeAll();

        if (notes.isEmpty()) {
            Span emptyState = new Span("No notes yet. Create your first note.");
            emptyState.getStyle().set("color", "gray");
            notesList.add(emptyState);
            return;
        }

        for (Note note : notes) {
            notesList.add(new NoteItemCard(note, editListener, deleteListener));
        }
    }
}