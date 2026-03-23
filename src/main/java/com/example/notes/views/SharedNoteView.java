package com.example.notes.views;

import com.example.notes.data.entity.Note;
import com.example.notes.service.NoteService;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Route("share/:token")
@SpringComponent
@UIScope
@AnonymousAllowed
@PageTitle("Shared Note | Vaadin Notes App")
public class SharedNoteView extends VerticalLayout implements BeforeEnterObserver {

    private final NoteService noteService;

    public SharedNoteView(NoteService noteService) {
        this.noteService = noteService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String token = event.getRouteParameters().get("token").orElse("");

        Optional<Note> noteOpt = noteService.getNoteByShareToken(token);

        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            displaySharedNote(note);
        } else {
            displayNotFound();
        }
    }

    private void displaySharedNote(Note note) {
        removeAll();

        VerticalLayout card = new VerticalLayout();
        card.setWidth("600px");
        card.setMaxWidth("90%");
        card.setPadding(true);
        card.setSpacing(true);
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.BoxShadow.MEDIUM,
            LumoUtility.Padding.LARGE
        );

        H1 title = new H1(note.getTitle());
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.Top.NONE);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        Span meta = new Span("Shared by " + note.getUser().getUsername() + " • " + note.getUpdatedAt().format(formatter));
        meta.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Paragraph content = new Paragraph(note.getContent());
        content.getStyle().set("white-space", "pre-wrap");
        content.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.LineHeight.MEDIUM);

        Button loginBtn = new Button("Create Your Own Notes", e -> getUI().ifPresent(ui -> ui.navigate(LoginView.class)));
        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        loginBtn.addClassName(LumoUtility.Margin.Top.LARGE);

        card.add(title, meta, content, loginBtn);
        add(card);
    }

    private void displayNotFound() {
        removeAll();

        VerticalLayout card = new VerticalLayout();
        card.setWidth("400px");
        card.setPadding(true);
        card.setAlignItems(Alignment.CENTER);
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.BoxShadow.MEDIUM
        );

        H3 errorTitle = new H3("Note Not Found");
        Paragraph errorMessage = new Paragraph("The link you followed may be incorrect, expired, or deactivated.");
        
        Button homeBtn = new Button("Go to App", e -> getUI().ifPresent(ui -> ui.navigate("")));
        homeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        card.add(errorTitle, errorMessage, homeBtn);
        add(card);
    }
}