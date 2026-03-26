package com.example.notes.views.components;

import com.example.notes.service.ImageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ImageGalleryToolbar extends Div {

    private final Button selectAllBtn = new Button("Select All", new Icon(VaadinIcon.CHECK_SQUARE_O));
    private final Button deleteSelectedBtn = new Button("Delete Selected", new Icon(VaadinIcon.TRASH));


    private final Set<Long> selectedIds;
    private final Map<Long, Div> cardMap;
    private final ImageService imageService;
    private final Consumer<List<Long>> onDeleted;

    public ImageGalleryToolbar(
            Set<Long> selectedIds,
            Map<Long, Div> cardMap,
            ImageService imageService,
            Consumer<List<Long>> onDeleted
    ) {
        this.selectedIds = selectedIds;
        this.cardMap = cardMap;
        this.imageService = imageService;
        this.onDeleted = onDeleted;

        addClassName("gallery-toolbar");
        setVisible(false);
        buildToolbar();
    }

    private void buildToolbar() {
        selectAllBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        selectAllBtn.addClassName("toolbar-btn");
        deleteSelectedBtn.addClassName("toolbar-btn");

        selectAllBtn.addClickListener(e -> {
            boolean selectAll = selectedIds.size() < cardMap.size();
            selectedIds.clear();
            cardMap.forEach((id, card) -> card.removeClassName("image-card--selected"));

            if (selectAll) {
                // use cardMap keys
                selectedIds.addAll(cardMap.keySet());
                cardMap.forEach((id, card) -> card.addClassName("image-card--selected"));
            }
            refresh();
        });

        deleteSelectedBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        deleteSelectedBtn.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Delete Images");
            dialog.setText("Are you sure you want to delete " + selectedIds.size() + " image(s)? This cannot be undone.");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(confirmEvent -> {
                List<Long> ids = new ArrayList<>(selectedIds);
                // single batch delete — one transaction
                imageService.deleteImagesByIds(ids);
                Notification.show(ids.size() + " image(s) deleted");
                onDeleted.accept(ids);
            });

            dialog.open();
        });

        add(selectAllBtn, deleteSelectedBtn);
    }

    public void refresh() {
        int count = selectedIds.size();
        setVisible(count > 0);
        deleteSelectedBtn.setText("Delete Selected (" + count + ")");

        boolean allSelected = !cardMap.isEmpty() && count == cardMap.size();
        selectAllBtn.setText(allSelected ? "Deselect All" : "Select All");
        selectAllBtn.setIcon(new Icon(allSelected ? VaadinIcon.CHECK_SQUARE : VaadinIcon.CHECK_SQUARE_O));
    }
}