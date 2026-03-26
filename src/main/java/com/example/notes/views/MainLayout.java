package com.example.notes.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.spring.security.AuthenticationContext;

public class MainLayout extends AppLayout {

    private final transient AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Vaadin Notes App");
        logo.addClassNames(com.vaadin.flow.theme.lumo.LumoUtility.FontSize.LARGE, com.vaadin.flow.theme.lumo.LumoUtility.Margin.MEDIUM);
        logo.getStyle().set("color", "white");

        Button logout = new Button("Log out", e -> authContext.logout());
        logout.getStyle().set("background-color", "white");
        logout.getStyle().set("color", "#6366f1"); // Indigo text to match navbar
        logout.getStyle().set("font-weight", "600");
        logout.getStyle().set("border-radius", "8px");

        DrawerToggle toggle = new DrawerToggle();
        toggle.getStyle().set("background-color", "white");
        toggle.getStyle().set("color", "#6366f1");
        toggle.getStyle().set("border-radius", "8px");
        toggle.getStyle().set("width", "40px");
        toggle.getStyle().set("height", "40px");
        toggle.getStyle().set("margin-right", "16px");

        HorizontalLayout header = new HorizontalLayout(toggle, logo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(com.vaadin.flow.theme.lumo.LumoUtility.Padding.Vertical.NONE, com.vaadin.flow.theme.lumo.LumoUtility.Padding.Horizontal.MEDIUM);

        // Distinct, beautiful navigation bar color (Indigo-500)
        header.getStyle().set("background-color", "#6366f1");
        header.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.15)");

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink notesLink = new RouterLink("My Notes", NotesView.class);
        styleMenuLink(notesLink);

        RouterLink galleryLink = new RouterLink("Gallery", GalleryView.class);
        styleMenuLink(galleryLink);

        VerticalLayout drawerContent = new VerticalLayout(notesLink, galleryLink);
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.getStyle().set("background-color", "#312e81"); // Indigo-900 (rich dark sidebar)
        drawerContent.getStyle().set("padding-top", "24px");

        // Force the app layout's internal drawer components to match the deep indigo theme
        getElement().getStyle().set("--vaadin-app-layout-drawer-background", "#312e81");
        getElement().getStyle().set("--vaadin-app-layout-drawer-border-color", "transparent");

        addToDrawer(drawerContent);
    }

    private void styleMenuLink(RouterLink link) {
        link.getStyle().set("color", "#e0e7ff"); // Light indigo text
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("font-weight", "600");
        link.getStyle().set("padding", "12px 24px");
        link.getStyle().set("display", "block");
        link.getStyle().set("width", "100%");
        link.getStyle().set("box-sizing", "border-box");
        link.getStyle().set("transition", "background-color 0.2s, color 0.2s");
        
        // Interactive hover states for a premium feel
        link.getElement().addEventListener("mouseover", e -> {
            link.getStyle().set("background-color", "rgba(255,255,255,0.1)");
            link.getStyle().set("color", "white");
        });
        link.getElement().addEventListener("mouseout", e -> {
            link.getStyle().set("background-color", "transparent");
            link.getStyle().set("color", "#e0e7ff");
        });
    }
}
