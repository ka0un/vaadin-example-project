package com.example.notes.views;

import com.example.notes.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Register | Image Gallery")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public RegisterView(UserService userService) {
        addClassName("register-view");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Background gradient
        getStyle()
                .set("background", "linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%)")
                .set("min-height", "100vh");

        // Card container
        Div card = new Div();
        card.getStyle()
                .set("background", "rgba(255, 255, 255, 0.05)")
                .set("backdrop-filter", "blur(20px)")
                .set("-webkit-backdrop-filter", "blur(20px)")
                .set("border", "1px solid rgba(255, 255, 255, 0.1)")
                .set("border-radius", "24px")
                .set("padding", "48px 40px")
                .set("box-shadow", "0 25px 50px rgba(0, 0, 0, 0.3)")
                .set("max-width", "420px")
                .set("width", "100%")
                .set("text-align", "center");

        // Icon
        Div iconWrapper = new Div();
        iconWrapper.getStyle()
                .set("width", "80px")
                .set("height", "80px")
                .set("border-radius", "20px")
                .set("background", "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin", "0 auto 24px auto")
                .set("font-size", "36px");
        iconWrapper.setText("✨");

        // Title
        H1 title = new H1("Create Account");
        title.getStyle()
                .set("color", "white")
                .set("font-size", "28px")
                .set("font-weight", "700")
                .set("margin", "0 0 8px 0")
                .set("font-family", "'Inter', 'Segoe UI', sans-serif");

        Paragraph subtitle = new Paragraph("Join and start uploading your images");
        subtitle.getStyle()
                .set("color", "rgba(255, 255, 255, 0.6)")
                .set("margin", "0 0 32px 0")
                .set("font-size", "14px");

        // Form fields
        TextField username = new TextField("Username");
        username.setWidthFull();
        username.setPlaceholder("Choose a username");
        username.getElement().getThemeList().add("dark");
        username.setRequired(true);
        username.setMinLength(3);

        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        password.setPlaceholder("Create a strong password");
        password.getElement().getThemeList().add("dark");
        password.setRequired(true);
        password.setMinLength(6);

        PasswordField confirmPassword = new PasswordField("Confirm Password");
        confirmPassword.setWidthFull();
        confirmPassword.setPlaceholder("Repeat your password");
        confirmPassword.getElement().getThemeList().add("dark");
        confirmPassword.setRequired(true);

        // Wrap fields in a layout for spacing
        Div formFields = new Div(username, password, confirmPassword);
        formFields.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px")
                .set("width", "100%")
                .set("margin-bottom", "24px");

        // Register button
        Button registerButton = new Button("Create Account");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none")
                .set("border-radius", "12px")
                .set("height", "48px")
                .set("font-size", "16px")
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        registerButton.addClickListener(e -> {
            if (username.getValue().trim().isEmpty()) {
                Notification.show("Please enter a username", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (password.getValue().length() < 6) {
                Notification.show("Password must be at least 6 characters", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show("Passwords do not match!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                userService.registerUser(username.getValue().trim(), password.getValue());
                Notification.show("Account created successfully! Please login.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate("login");
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Login link
        Div loginLink = new Div();
        loginLink.getStyle()
                .set("margin-top", "24px")
                .set("text-align", "center");

        Paragraph loginText = new Paragraph("Already have an account?");
        loginText.getStyle()
                .set("color", "rgba(255, 255, 255, 0.5)")
                .set("margin", "0 0 8px 0")
                .set("font-size", "14px");

        Anchor loginAnchor = new Anchor("login", "Sign In");
        loginAnchor.getStyle()
                .set("color", "#667eea")
                .set("text-decoration", "none")
                .set("font-weight", "600")
                .set("font-size", "14px");

        loginLink.add(loginText, loginAnchor);

        card.add(iconWrapper, title, subtitle, formFields, registerButton, loginLink);
        add(card);
    }
}
