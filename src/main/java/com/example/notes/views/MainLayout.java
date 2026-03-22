package com.example.notes.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

@CssImport("./styles/layout.css")
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

        Button logout = new Button("Log out", e -> authContext.logout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setPadding(true);
        drawerContent.setSpacing(true);
        drawerContent.addClassName("nav-drawer");

        VerticalLayout notesLinkContainer = new VerticalLayout();
        notesLinkContainer.setPadding(false);
        notesLinkContainer.setSpacing(false);
        notesLinkContainer.add(VaadinIcon.FILE_TEXT.create());
        RouterLink notesLink = new RouterLink("My Notes", NotesView.class);
        notesLink.addClassName("nav-link");
        notesLinkContainer.add(notesLink);

        VerticalLayout galleryLinkContainer = new VerticalLayout();
        galleryLinkContainer.setPadding(false);
        galleryLinkContainer.setSpacing(false);
        galleryLinkContainer.add(VaadinIcon.PICTURE.create());
        RouterLink galleryLink = new RouterLink("Image Gallery", ImageGalleryView.class);
        galleryLink.addClassName("nav-link");
        galleryLinkContainer.add(galleryLink);

        drawerContent.add(notesLinkContainer, galleryLinkContainer);

        Scroller scroller = new Scroller(drawerContent);
        scroller.setClassName("drawer-scroller");
        addToDrawer(scroller);
    }
}
