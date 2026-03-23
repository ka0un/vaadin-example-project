package com.example.notes.views.components;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.SharedNote;
import com.example.notes.data.entity.SharedNoteWithGroup;
import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserGroup;
import com.example.notes.service.NoteService;
import com.example.notes.service.UserGroupService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import java.util.List;

/**
 * Dialog for sharing a note with specific users or groups.
 */
public class ShareDialog extends Dialog {

    private final Note note;
    private final NoteService noteService;
    private final UserGroupService groupService;
    private final User currentUser;

    private final VerticalLayout content = new VerticalLayout();
    private final VerticalLayout sharedListPanel = new VerticalLayout();

    public ShareDialog(Note note, NoteService noteService, UserGroupService groupService, User currentUser) {
        this.note = note;
        this.noteService = noteService;
        this.groupService = groupService;
        this.currentUser = currentUser;

        setHeaderTitle("Share: " + note.getTitle());
        setWidth("550px");
        setDraggable(true);

        initLayout();
        refreshSharedList();
    }

    private void initLayout() {
        Tabs tabs = new Tabs(new Tab("Users"), new Tab("Groups"));
        tabs.setWidthFull();

        Div tabContent = new Div();
        tabContent.setWidthFull();

        tabs.addSelectedChangeListener(event -> {
            tabContent.removeAll();
            if (tabs.getSelectedIndex() == 0) {
                tabContent.add(createUsersTab());
            } else {
                tabContent.add(createGroupsTab());
            }
        });

        // Initial tab
        tabContent.add(createUsersTab());

        content.add(tabs, tabContent, new H4("Currently shared with"), sharedListPanel);
        add(content);

        Button doneBtn = new Button("Done", click -> close());
        doneBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(doneBtn);
    }

    private VerticalLayout createUsersTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        ComboBox<User> userSearch = new ComboBox<>("Search User");
        userSearch.setPlaceholder("Username...");
        userSearch.setWidthFull();
        userSearch.setItemLabelGenerator(User::getUsername);
        userSearch.setItems(query -> noteService.searchUsers(query.getFilter().orElse("")).stream()
                .filter(u -> !u.equals(currentUser)));

        Select<SharedNote.SharePermission> permissionSelect = createPermissionSelect();

        Button shareBtn = new Button("Share", click -> {
            User selected = userSearch.getValue();
            if (selected == null) return;
            try {
                noteService.shareNoteWithUser(note, selected.getUsername(), permissionSelect.getValue());
                userSearch.clear();
                refreshSharedList();
                Notification.show("Shared with " + selected.getUsername());
            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
        });
        shareBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout row = new HorizontalLayout(userSearch, permissionSelect, shareBtn);
        row.setWidthFull();
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        row.setFlexGrow(1, userSearch);
        layout.add(row);
        return layout;
    }

    private VerticalLayout createGroupsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        ComboBox<UserGroup> groupSearch = new ComboBox<>("Select Group");
        groupSearch.setPlaceholder("Your groups...");
        groupSearch.setWidthFull();
        groupSearch.setItemLabelGenerator(UserGroup::getName);
        groupSearch.setItems(groupService.getUserGroups(currentUser));

        Select<SharedNote.SharePermission> permissionSelect = createPermissionSelect();

        Button shareBtn = new Button("Share with Group", click -> {
            UserGroup selected = groupSearch.getValue();
            if (selected == null) return;
            noteService.shareNoteWithGroup(note, selected, permissionSelect.getValue());
            groupSearch.clear();
            refreshSharedList();
            Notification.show("Shared with group " + selected.getName());
        });
        shareBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout row = new HorizontalLayout(groupSearch, permissionSelect, shareBtn);
        row.setWidthFull();
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        row.setFlexGrow(1, groupSearch);
        layout.add(row);
        return layout;
    }

    private Select<SharedNote.SharePermission> createPermissionSelect() {
        Select<SharedNote.SharePermission> select = new Select<>();
        select.setLabel("Permission");
        select.setItems(SharedNote.SharePermission.values());
        select.setValue(SharedNote.SharePermission.READ);
        select.setItemLabelGenerator(p -> p == SharedNote.SharePermission.EDIT ? "Edit" : "View");
        select.setWidth("120px");
        return select;
    }

    private void refreshSharedList() {
        sharedListPanel.removeAll();

        // 1. Show individual users
        List<SharedNote> sharedUsers = noteService.getSharedUsers(note);
        for (SharedNote sn : sharedUsers) {
            sharedListPanel.add(createSharedRow(sn.getSharedWithUser().getUsername(), sn.getPermission(), () -> {
                noteService.unshareNoteWithUser(note, sn.getSharedWithUser());
                refreshSharedList();
            }, false));
        }

        // 2. Show groups
        List<SharedNoteWithGroup> sharedGroups = noteService.getSharedGroups(note);
        for (SharedNoteWithGroup sng : sharedGroups) {
            sharedListPanel.add(createSharedRow("Group: " + sng.getUserGroup().getName(), sng.getPermission(), () -> {
                noteService.unshareNoteWithGroup(note, sng.getUserGroup());
                refreshSharedList();
            }, true));
        }

        if (sharedUsers.isEmpty() && sharedGroups.isEmpty()) {
            sharedListPanel.add(new Paragraph("Not shared with anyone yet."));
        }
    }

    private HorizontalLayout createSharedRow(String info, SharedNote.SharePermission perm, Runnable onRemove, boolean isGroup) {
        Span infoSpan = new Span(info);
        if (isGroup) infoSpan.getStyle().set("color", "var(--lumo-primary-color)");
        
        Span permBadge = new Span(perm == SharedNote.SharePermission.EDIT ? "Edit" : "View");
        permBadge.getElement().getThemeList().add("badge");
        if (perm == SharedNote.SharePermission.EDIT) permBadge.getElement().getThemeList().add("success");

        Button removeBtn = new Button(VaadinIcon.CLOSE.create(), click -> {
            onRemove.run();
            Notification.show("Removed share");
        });
        removeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout row = new HorizontalLayout(infoSpan, permBadge, removeBtn);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        return row;
    }
}
