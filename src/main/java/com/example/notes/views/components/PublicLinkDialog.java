package com.example.notes.views.components;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.NoteShareToken;
import com.example.notes.service.NoteService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Dialog that manages the public share link for a note.
 * The link is accessible by anyone (no login required).
 * Users can generate a new link or deactivate the existing one.
 */
public class PublicLinkDialog extends Dialog {

    private final Note note;
    private final NoteService noteService;
    private final VerticalLayout body = new VerticalLayout();

    public PublicLinkDialog(Note note, NoteService noteService) {
        this.note = note;
        this.noteService = noteService;

        setHeaderTitle("Public Link — " + note.getTitle());
        setWidth("440px");
        setDraggable(true);

        body.setSpacing(true);
        body.setPadding(true);
        body.setAlignItems(FlexComponent.Alignment.STRETCH);
        add(body);

        Button closeBtn = new Button("Close", click -> close());
        getFooter().add(closeBtn);

        refreshContent();
    }

    private void refreshContent() {
        body.removeAll();

        Paragraph description = new Paragraph(
                "A public link lets anyone view this note — no login required. "
                + "You can revoke the link at any time.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin", "0");

        Button generateBtn = new Button("Generate Public Link", VaadinIcon.LINK.create(), click -> {
            NoteShareToken token = noteService.createPublicShareLink(note);
            // Fetch the current browser URL via callback
            UI.getCurrent().getPage().fetchCurrentURL(url -> {
                String baseUrl = url.getProtocol() + "://" + url.getHost()
                        + (url.getPort() > 0 ? ":" + url.getPort() : "");
                showLink(token.getShareUrl(baseUrl));
            });
        });
        generateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        body.add(description, generateBtn);
    }

    private void showLink(String url) {
        body.removeAll();

        Span label = new Span("Your public link:");
        label.getStyle().set("font-weight", "500");

        TextField urlField = new TextField();
        urlField.setValue(url);
        urlField.setReadOnly(true);
        urlField.setWidthFull();

        // Copy to clipboard using the browser Clipboard API via JavaScript
        Button copyBtn = new Button("Copy Link", VaadinIcon.COPY.create(), click -> {
            UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", url);
            Notification.show("Link copied to clipboard!", 2500, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        copyBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        Button revokeBtn = new Button("Revoke Link", VaadinIcon.CLOSE_CIRCLE.create(), click -> {
            noteService.deactivatePublicShareLink(note);
            Notification.show("Public link deactivated.", 2500, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            refreshContent();
        });
        revokeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        body.add(label, urlField, copyBtn, revokeBtn);
    }
}
