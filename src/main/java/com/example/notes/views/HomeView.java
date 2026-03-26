package com.example.notes.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | Vaadin Notes App")
@PermitAll
public class HomeView extends VerticalLayout {

    public HomeView() {
        add(new H2("Welcome to your dashboard 🚀"));
    }
}