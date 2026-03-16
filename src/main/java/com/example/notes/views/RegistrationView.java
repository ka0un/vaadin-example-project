package com.example.notes.views;

import com.example.notes.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Register | Vaadin Notes App")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private final UserService userService;

    public RegistrationView(UserService userService) {
        this.userService = userService;
        addClassName("registration-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 header = new H2("Sign Up");

        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");
        PasswordField confirmPasswordField = new PasswordField("Confirm Password");

        Button submitButton = new Button("Register", event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();
            String confirmPassword = confirmPasswordField.getValue();

            if (username.isBlank() || password.isBlank()) {
                Notification.show("Please fill in all fields.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                Notification.show("Passwords do not match.");
                return;
            }

            try {
                this.userService.registerUser(username, password);
                Notification.show("Registration successful! You can now log in.");
                getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage());
            }
        });
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(header, usernameField, passwordField, confirmPasswordField, submitButton,
                new RouterLink("Already have an account? Log in", LoginView.class));
    }
}
