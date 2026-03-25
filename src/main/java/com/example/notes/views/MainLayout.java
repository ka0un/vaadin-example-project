package com.example.notes.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
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
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);
        logo.getStyle().set("color", "#667eea");

        Button logout = new Button("Log out", e -> authContext.logout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);
        header.getStyle()
                .set("border-bottom", "1px solid #e0e0e0")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)");
        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.getStyle()
                .set("height", "100%")
                .set("padding-top", "16px");

        // Menu title
        Span menuTitle = new Span("MENU");
        menuTitle.getStyle()
                .set("font-size", "11px")
                .set("font-weight", "700")
                .set("color", "#999")
                .set("padding", "0 16px 8px 16px")
                .set("letter-spacing", "1px");

        Div notesItem = createNavItem("My Notes", NotesView.class);
        Div galleryItem = createNavItem("Image Gallery", ImageGalleryView.class);

        drawerLayout.add(menuTitle, notesItem, galleryItem);
        addToDrawer(drawerLayout);
    }

    private <T extends com.vaadin.flow.component.Component> Div createNavItem(
            String label, Class<T> navigationTarget) {

        RouterLink link = new RouterLink(navigationTarget);
        link.getStyle()
                .set("text-decoration", "none")
                .set("width", "100%")
                .set("display", "block");

        Div item = new Div();
        item.getStyle()
                .set("padding", "10px 16px")
                .set("border-radius", "10px")
                .set("margin", "2px 8px")
                .set("cursor", "pointer")
                .set("color", "#444")
                .set("font-size", "14px")
                .set("font-weight", "500")
                .set("transition", "background 0.2s ease");

        // Hover effect
        item.getElement().addEventListener("mouseover", e ->
                item.getStyle().set("background", "#ede9fe")
        );
        item.getElement().addEventListener("mouseout", e ->
                item.getStyle().set("background", "transparent")
        );

        Span labelSpan = new Span(label);
        item.add(labelSpan);
        link.add(item);

        Div wrapper = new Div(link);
        wrapper.setWidthFull();
        return wrapper;
    }

}
