package com.example.notes.views;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserGroup;
import com.example.notes.service.NoteService;
import com.example.notes.service.UserGroupService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import java.util.Optional;

@Route(value = "groups", layout = MainLayout.class)
@PageTitle("Manage Groups | My Notes")
@RolesAllowed("USER")
public class GroupManagementView extends VerticalLayout {

    private final UserGroupService groupService;
    private final NoteService noteService;
    private final User currentUser;

    private final Grid<UserGroup> groupGrid = new Grid<>(UserGroup.class, false);
    private final VerticalLayout detailsLayout = new VerticalLayout();
    private UserGroup selectedGroup;

    public GroupManagementView(UserGroupService groupService, NoteService noteService, AuthenticationContext authContext, com.example.notes.data.repository.UserRepository userRepository) {
        this.groupService = groupService;
        this.noteService = noteService;
        
        String username = authContext.getPrincipalName().orElseThrow();
        this.currentUser = userRepository.findByUsername(username).orElseThrow();

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("My Groups"));

        setupCreateGroupHeader();
        setupSplitLayout();
        refreshGroups();
    }

    private void setupCreateGroupHeader() {
        TextField nameField = new TextField("New Group Name");
        Button createBtn = new Button("Create Group", VaadinIcon.PLUS.create(), click -> {
            String name = nameField.getValue();
            if (name == null || name.isBlank()) return;
            groupService.createUserGroup(name, currentUser);
            nameField.clear();
            refreshGroups();
            Notification.show("Group created!");
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout header = new HorizontalLayout(nameField, createBtn);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        add(header);
    }

    private void setupSplitLayout() {
        groupGrid.addColumn(UserGroup::getName).setHeader("Group Name").setAutoWidth(true);
        groupGrid.addComponentColumn(group -> {
            boolean isOwner = group.getOwner().equals(currentUser);
            return new Span(isOwner ? "Owner" : "Member");
        }).setHeader("Role").setAutoWidth(true);

        groupGrid.asSingleSelect().addValueChangeListener(event -> {
            selectedGroup = event.getValue();
            showGroupDetails();
        });

        HorizontalLayout split = new HorizontalLayout(groupGrid, detailsLayout);
        split.setSizeFull();
        split.setFlexGrow(1, groupGrid);
        split.setFlexGrow(1, detailsLayout);
        add(split);
    }

    private void showGroupDetails() {
        detailsLayout.removeAll();
        if (selectedGroup == null) {
            detailsLayout.add(new Span("Select a group to see details"));
            return;
        }

        boolean isOwner = selectedGroup.getOwner().equals(currentUser);
        detailsLayout.add(new H2("Group: " + selectedGroup.getName()));

        if (isOwner) {
            setupMemberAddition();
        }

        Grid<User> memberGrid = new Grid<>(User.class, false);
        memberGrid.addColumn(User::getUsername).setHeader("Username");
        if (isOwner) {
            memberGrid.addComponentColumn(user -> {
                Button removeBtn = new Button(VaadinIcon.TRASH.create(), click -> {
                    groupService.removeUserFromGroup(selectedGroup, user);
                    showGroupDetails();
                });
                removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                return removeBtn;
            }).setHeader("Action");
        }
        memberGrid.setItems(selectedGroup.getMembers());
        detailsLayout.add(memberGrid);

        if (isOwner) {
            Button deleteBtn = new Button("Delete Group", VaadinIcon.CLOSE_CIRCLE.create(), click -> {
                groupService.deleteUserGroup(selectedGroup);
                selectedGroup = null;
                refreshGroups();
                showGroupDetails();
                Notification.show("Group deleted.");
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            detailsLayout.add(deleteBtn);
        }
    }

    private void setupMemberAddition() {
        ComboBox<User> userSearch = new ComboBox<>("Add Member");
        userSearch.setPlaceholder("Search by username...");
        userSearch.setItemLabelGenerator(User::getUsername);
        userSearch.setItems(query -> noteService.searchUsers(query.getFilter().orElse("")).stream());

        Button addBtn = new Button("Add", click -> {
            User selected = userSearch.getValue();
            if (selected == null) return;
            try {
                groupService.addUserToGroup(selectedGroup, selected.getUsername());
                userSearch.clear();
                showGroupDetails();
            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
        });
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout row = new HorizontalLayout(userSearch, addBtn);
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        detailsLayout.add(row);
    }

    private void refreshGroups() {
        groupGrid.setItems(groupService.getUserGroups(currentUser));
    }
}
