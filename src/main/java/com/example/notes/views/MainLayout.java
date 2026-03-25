package com.example.notes.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

// Import views
import com.example.notes.views.NotesView;
import com.example.notes.views.ImageUploadView;

public class MainLayout extends AppLayout {

    private static final String NAVBAR_HEIGHT = "64px";
    private final transient AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        setPrimarySection(Section.DRAWER);
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        DrawerToggle drawerToggle = new DrawerToggle();
        H1 logo = new H1("Vaadin Notes App");
        logo.addClassNames(LumoUtility.Margin.MEDIUM, "app-logo");

        Button logout = new Button("Log out", e -> authContext.logout());
        logout.addClassName("logout-button");

        HorizontalLayout header = new HorizontalLayout(drawerToggle, logo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setHeight(NAVBAR_HEIGHT);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM, "app-header");

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setPadding(true);
        drawerContent.setSpacing(true);
        drawerContent.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.SMALL, "app-drawer-content");

        RouterLink notesLink = createSidebarLink("My Notes", NotesView.class);
        RouterLink imageUploadLink = createSidebarLink("Image Upload", ImageUploadView.class);

        drawerContent.add(notesLink, imageUploadLink);
        addToDrawer(drawerContent);
    }

    private RouterLink createSidebarLink(String text, Class<? extends com.vaadin.flow.component.Component> navigationTarget) {
        RouterLink link = new RouterLink(text, navigationTarget);
        link.addClassName("sidebar-link");
        return link;
    }
}