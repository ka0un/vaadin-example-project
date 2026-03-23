package com.example.notes.views.components;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.function.Consumer;

public class NoteForm extends VerticalLayout {

    private final TextField titleField = new TextField("Title");
    private final TextArea contentField = new TextArea("Content");
    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");

    private final Binder<Note> binder = new Binder<>(Note.class);
    private Consumer<Note> onSave;
    private Runnable onCancel;

    public NoteForm() {
        addClassName("note-form");
        setWidthFull();
        setSpacing(true);
        setPadding(false);

        configureFields();
        initLayout();
        
        // Manually bind each field to the Note bean property
        binder.forField(titleField)
                .asRequired("Title is required")
                .bind(Note::getTitle, Note::setTitle);

        binder.forField(contentField)
                .asRequired("Content is required")
                .bind(Note::getContent, Note::setContent);
    }

    private void configureFields() {
        titleField.setPlaceholder("Enter note title...");
        titleField.setWidthFull();
        titleField.setClearButtonVisible(true);
        titleField.setRequired(true);
        titleField.setValueChangeMode(ValueChangeMode.EAGER);

        contentField.setPlaceholder("What's on your mind?");
        contentField.setWidthFull();
        contentField.setMinHeight("100px");
        contentField.setMaxHeight("300px");
        contentField.setClearButtonVisible(true);
        contentField.setRequired(true);
        contentField.setValueChangeMode(ValueChangeMode.EAGER);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveButton.setEnabled(false);
        binder.addValueChangeListener(e -> saveButton.setEnabled(binder.isValid()));
    }

    private void initLayout() {
        HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
        actions.setSpacing(true);
        actions.setPadding(true);

        add(titleField, contentField, actions);
    }

    public void setNote(Note note) {
        binder.setBean(note);
        if (note != null) {
            saveButton.setEnabled(binder.isValid());
        }
    }

    public void setOnSave(Consumer<Note> onSave) {
        this.onSave = onSave;
        saveButton.addClickListener(click -> {
            if (binder.validate().isOk()) {
                onSave.accept(binder.getBean());
            }
        });
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        cancelButton.addClickListener(click -> onCancel.run());
    }

    public void clear() {
        titleField.clear();
        contentField.clear();
        saveButton.setEnabled(false);
    }
}
