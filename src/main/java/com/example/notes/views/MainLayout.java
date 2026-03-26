package com.example.notes.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;

public class MainLayout extends AppLayout {

    private final transient AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        applyGlobalBackground();
        createHeader();
    }

    private void applyGlobalBackground() {
        getStyle().set("background", "transparent");
        getElement().executeJs(
                """
                document.documentElement.style.background = 'linear-gradient(180deg, #c9dfff 0%, #b5d3ff 100%)';
                document.body.style.background = 'linear-gradient(180deg, #c9dfff 0%, #b5d3ff 100%)';
                document.body.style.backgroundAttachment = 'fixed';
                document.body.style.backgroundSize = 'cover';
                document.body.style.minHeight = '100vh';
                document.body.style.margin = '0';

                if (this.shadowRoot) {
                    const navbar = this.shadowRoot.querySelector('[part="navbar"]');
                    const content = this.shadowRoot.querySelector('[part="content"]');
                    if (navbar) {
                        navbar.style.background = 'transparent';
                    }
                    if (content) {
                        content.style.background = 'transparent';
                    }
                }
                """
        );
    }

    private void createHeader() {
        Span brand = new Span("Vaadin Notes App");
        brand.getStyle()
                .set("font-size", "1.6rem")
                .set("font-weight", "800")
                .set("letter-spacing", "0.2px")
                .set("color", "#102a43");

        RouterLink notesLink = new RouterLink("My Notes", NotesView.class);
        RouterLink imagesLink = new RouterLink("Images", ImageView.class);
        styleNavLink(notesLink);
        styleNavLink(imagesLink);

        HorizontalLayout navLinks = new HorizontalLayout(notesLink, imagesLink);
        navLinks.setSpacing(true);
        navLinks.getStyle().set("gap", "0.6rem");

        Button logout = new Button("Log out", e -> authContext.logout());
        logout.getStyle()
                .set("border-radius", "999px")
                .set("padding", "0.52rem 1.1rem")
                .set("border", "1px solid #d6dce6")
                .set("background", "#ffffff")
                .set("font-weight", "700")
                .set("color", "#1f3a56")
                .set("cursor", "pointer");

        HorizontalLayout header = new HorizontalLayout(brand, navLinks, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(brand);
        header.setWidthFull();
        header.getStyle()
                .set("padding", "0.9rem 1.2rem")
                .set("margin", "0.55rem 0.75rem 0.35rem")
                .set("border", "1px solid #dce3ee")
                .set("border-radius", "18px")
                .set("background", "linear-gradient(180deg, #ffffff 0%, #f7f9fc 100%)")
                .set("box-shadow", "0 10px 28px rgba(16, 42, 67, 0.08)");

        addToNavbar(header);
    }

    private void styleNavLink(RouterLink link) {
        link.getStyle()
                .set("text-decoration", "none")
                .set("border", "1px solid #dbe5f0")
                .set("border-radius", "999px")
                .set("padding", "0.45rem 1rem")
                .set("font-weight", "700")
                .set("color", "#1f4d84")
                .set("background", "#f8fbff");
    }
}
